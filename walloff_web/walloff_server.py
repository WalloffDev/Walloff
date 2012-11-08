# Lobby Manager Server for Walloff_Android

# Setup server environment --------------------------------------------------------------------------------------------#
from django.core.management import setup_environ
from walloff_web import settings 			# Grab walloff_web settings for access to Django apps
setup_environ( settings )				# Set server environment to Django instance environment
#----------------------------------------------------------------------------------------------------------------------#

# Imports -------------------------------------------------------------------------------------------------------------#
import socket, threading, SocketServer, datetime, os, json
from django.core import serializers
from django.db import IntegrityError, DatabaseError
from lobby_app.models import *
from gcm.gcm import *					# Python Google Cloud Messaging client
from gcm.constants import *
#----------------------------------------------------------------------------------------------------------------------#

# Server metadata ----------------------------------#
HOST = 'walloff.cslabs.clarkson.edu'
PORT = 8080
GCM_KEY = 'AIzaSyC5M-ip4-1pnm0gExQow1CS9xIPke7EvAw'
#---------------------------------------------------#

# Constants ----------------------------------------------------------------#
MAX_BUF = 1024		# Sets max buffer size to receive from request
MAX_LOB_SIZE = 4	# Sets maximum number of allowed players in a lobby
#---------------------------------------------------------------------------#

# Tags --------------------------------------------------------------------------------#
m_login = 'login'					# Create new user req.
m_create = 'create'					# Create new lobby req.
m_join = 'join'						# Join existing lobby req.
m_leave = 'leave'					# Leave current lobby req.
m_get_available_lobbies = 'get_lobbies'			# Query existing lobbies req.
m_update_map = 'update_map' 				# Lobby map update req.
m_success = 'SUCCESS'					# Server success msg to client
m_fail = 'FAIL'						# Server error msg to client
#--------------------------------------------------------------------------------------#

# Error messages ---------------------------------------#
err_unknown = 'unknown error'
err_uname_exists = 'username not available'
err_lname_exists = 'lobby name already exists'
err_lobb_unavail = 'lobby is no longer available'
#-------------------------------------------------------#

# Connection Handler ------------------------------------------------------------------------------#
class RequestHandler( SocketServer.BaseRequestHandler ):
	
	def handle( self ):

		data = json.loads( str( self.request.recv( MAX_BUF ) ) )
		tag = data[ 'tag' ]
	
		if tag == m_login:
				# Get info for new player
				m_uname = data[ 'uname' ]
				m_gcmid = data[ 'gcmid' ]
				try:
					# Check if username already exists
					p = Player.objects.filter( username=str(m_uname).lower() )
					if len( p.all() ) > 0:
						raise IntegrityError			

					# Create new user
					new_player = Player( )				
					new_player.set_data( str( m_uname ).lower( ), str( m_gcmid ) )
					print 'Created new player: ' + str( m_uname )
					self.success( )
		
					# Test GCM
					data = { 'msg' : 'test message' }
					players = [new_player]
					self.send( players=players, msg=data )					

				except IntegrityError:
					self.fail( err_uname_exists )
				except:
					print 'Error: m_login - unknown'
					self.fail( err_unknown )
				
		elif tag == m_create:

			try:
				# Get info for new lobby and map
				m_name = data[ 'name' ]
				m_map = data[ 'map' ]
				m_size = data[ 'size' ]
				m_moving_obstacles = data[ 'moving_obstacles' ]
				m_number_obstacles = data[ 'number_obstacles' ]
				m_shrinkable = data[ 'shrinkable' ]
				m_host = data[ 'host' ]

				# Create the lobby and populate it with parsed info	
				new_lobby = Lobby()
				new_lobby.set_data( m_name, False, m_map, m_size, m_moving_obstacles, m_number_obstacles, m_shrinkable )

				# get player by username ( auth_token ), add them to lobby
				player = Player.objects.get( username = m_host )
				player.join_lobby( new_lobby )
				self.success( )
			
			except IntegrityError:
				print 'Error: m_create - lobby name already exists'
				self.fail( err_lname_exists )
			#except:
			#	print 'Error: m_create - unknown'
			#	self.fail( err_unknown )

		elif tag == m_join:

			try:
				# Obtain player that is joining the lobby
				m_host = data[ 'player' ]
				m_lobby = data[ 'lobby' ]
				player = Player.objects.get( username = m_host )
				
				# Assure #players in lobby is less than MAX_LOB_SIZE
				players = Lobby.objects.get( name = m_lobby ).player_set
				if len( players.all( ) ) < MAX_LOB_SIZE:
		
					# Add player to lobby
					player.join( Lobby.objects.get( name = m_lobby ) )
	
					# TODO: send success and player arrays
					# dts = json.dumps( str( players.all( ) ) ) 
					# serializers.serialize( 'json', players.all( ), use_natural_keys=True )
					self.success( )
					self.send_to_all( )
				else:
					# Lobby no longer available
					self.fail( err_lobb_unavail )
			except:
				print 'Error: m_join - unknown'
				self.fail( err_unknown )
	
		elif tag == m_leave:

			try:
				# Obtain the player that is currently leaving the lobby
				m_host = data[ 'player' ]
				player = Player.objects.get( username = m_host )
				lobby = player.lobby
				
				# Remove the player from the lobby
				player.leave_lobby( )

				# TODO: Notify players
				self.success( )
				data = serializers.serialize( 'json', lobby.player_set.all( ) )
				self.send_to_all( lobby.player_set.all( ), data )
			except:
				print 'Error: m_leave - unknown'
				self.fail( err_unknown )
						
		elif tag == m_get_available_lobbies:
				
			try:
				# Return list of available lobbies
				lobbies = Lobby.objects.annotate( num_players = Count( 'player' ) ).exclude( num_players = MAX_LOB_SIZE )	
				data = serializers.serialize( 'json', lobbies.all( ) )
				self.request.sendall( data )

				self.success( )
			except:
				print 'Error: m_get_available_lobbies - unknown'
				self.fail( err_unknown )			

		elif tag == m_update_map:

			try:	
				# Get map update info
				m_name = data[ 'name' ]
				m_map = data[ 'map' ]
                                m_size = data[ 'size' ]
                                m_moving_obstacles = data[ 'moving_obstacles' ]
                                m_number_obstacles = data[ 'number_obstacles' ]
                                m_shrinkable = data[ 'shrinkable' ]	
			
				lobby = Lobby.objects.get( name = m_name )
				lobby.update( m_map, m_size, m_moving_obstacles, m_number_obstacles, m_shrinkable )

				# TODO: Notify players
				self.send_to_all( )
				self.success( )
			except:
				print 'Error: m_update_map - unknown'
				self.fail( err_unknown )

		else:
			# Possible foreign message received... ignore
			print 'Error: message received with an unrecognized tag, ignoring ...'
		
		return

	def success( self ):
		content = json.dumps( { 'return' : m_success } )
		self.request.sendall( content )

	def fail( self, msg ):
		content = json.dumps( { 'return' : m_fail, 'message' : msg } )
		self.request.sendall( content )

	def send( self, players, msg ):
		
		reg_ids = list( )
		for player in players:
			reg_ids.append( player.gcm_id )

		print 'Sending push notification ...'
		result = self.server.gcm.send_message( reg_ids=reg_ids, data=msg, retries=2 )

#-------------------------------------------------------------------------------------------------------------#

# Server -------------------------------------------------------------#
class Server( SocketServer.ThreadingMixIn, SocketServer.TCPServer ):
	allow_reuse_address = True
	timeout = None

	def now( self ):
        	d = datetime.datetime.now( )
        	return d.strftime( "%d/%m/%y %H:%M:%S" )
#---------------------------------------------------------------------#

# Main --------------------------------------------------------------#
if __name__ == "__main__":

	server = Server( ( HOST, PORT ), RequestHandler )
	server.gcm = gcm( GCM_KEY )
	print 'Walloff helper server now running: ' + server.now( ) 
	try:
		#while 1:
		#	server.handle_request( )
		server.serve_forever( )
	except KeyboardInterrupt:
		pass
#--------------------------------------------------------------------#
