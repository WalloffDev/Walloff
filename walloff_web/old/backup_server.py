# TODO:
#	Need to implement player_die message handling --> when n-1 player_die message recvd record winning stat for last player etc...
#	implent in_game leave message - count as player_die and remove from lobby .. etc

# Lobby Manager Server for Walloff_Android

# Setup server environment --------------------------------------------------------------------------------------------#
from django.core.management import setup_environ
from walloff_web import settings 			# Grab walloff_web settings for access to Django apps
setup_environ( settings )				# Set server environment to Django instance environment
#----------------------------------------------------------------------------------------------------------------------#

# Imports -------------------------------------------------------------------------------------------------------------#
import socket, threading, SocketServer, datetime, os, time
from django.utils import simplejson
from django.core import serializers
from django.db import IntegrityError, DatabaseError
from django.db.models import Count
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
GS_DELAY = 15		# Game Start Delay
#---------------------------------------------------------------------------#

# Tags --------------------------------------------------------------------------------#
m_login = 'login'					# Create new user req.
m_create = 'create'					# Create new lobby req.
m_join = 'join'						# Join existing lobby req.
m_leave = 'leave'					# Leave current lobby req.
m_get_available_lobbies = 'get_lobbies'			# Query existing lobbies req.
m_update_map = 'update_map' 				# Lobby map update req.
m_lobby_update = 'lobby_update'				# Lobby update req.
m_gs = 'gs'						# Game Start req.
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
	
		# Get client net info
		client_addr = self.client_address
		client_priv_ip = data[ 'priv_ip' ]
		client_priv_port = data[ 'priv_port' ]

		print str( client_addr ) + ' ' + client_priv_ip + ' ' + str( client_priv_port )

		if tag == m_login:
				# Get info for new player
				m_uname = data[ 'uname' ]
				m_passwd = data[ 'passwd' ]
				m_gcmid = data[ 'gcmid' ]
				try:
					# Check if username already exists
					p = Player.objects.filter( django_user__username=str(m_uname).lower() )
					if len( p.all() ) > 0:
						raise IntegrityError			

					# If not, create new user
					new_player = Player( )				
					new_player.set_data( str( m_uname ).lower( ), str( m_passwd ), str( m_gcmid ) )	
					
					self.success( )

				except IntegrityError:
					self.fail( err_uname_exists )
				except BaseException as e:
					print e
					self.fail( err_unknown )
				
		elif tag == m_create:

			try:
				# Get info for new lobby and map
				m_name = data[ 'lname' ]
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
				player = Player.objects.get( django_user__username = str( m_host ).lower( ) )
				player.update_net_info( client_addr[ 0 ], client_addr[ 1 ], client_priv_ip, client_priv_port )
				player.join_lobby( new_lobby )
				self.success( )

				# send player array
				self.send_parrays( players=[ player ], lobby=new_lobby )	
					
			except IntegrityError:
				print 'Error: m_create - lobby name already exists'
				self.fail( err_lname_exists )
			except BaseException as e:
				print e
				self.fail( err_unknown )

		elif tag == m_join:			

			try:
				# Obtain player that is joining the lobby
				m_host = data[ 'uname' ]
				m_lobby = data[ 'lname' ]
				player = Player.objects.get( django_user__username = str( m_host ).lower( ) )
				
				# Assure #players in lobby is less than MAX_LOB_SIZE
				players = Lobby.objects.get( name = m_lobby ).player_set
				if len( players.all( ) ) < MAX_LOB_SIZE:
		
					# Add player to lobby
					lobby = Lobby.objects.get( name=m_lobby )
					player.update_net_info( client_addr[ 0 ], client_addr[ 1 ], client_priv_ip, client_priv_port )
					player.join_lobby( lobby )
					self.success( )

					# Send success and player arrays
					players = Lobby.objects.get( name=m_lobby ).player_set
					self.send_parrays( players=players.all( ), lobby=lobby )

					# Schedule game start
					self.server.timer_man.schedule_gs( lobby )

				else:
					# Lobby no longer available
					self.fail( err_lobb_unavail )
			except BaseException as e:
				print e
				self.fail( err_unknown )
	
		elif tag == m_leave:

			try:
				# Obtain the player that is currently leaving the lobby
				m_host = data[ 'uname' ]
				player = Player.objects.get( django_user__username = str( m_host ).lower( ) )
				m_lobby = player.lobby
				
				# Remove the player from the lobby
				player.leave_lobby( )
				self.success( )
		
				# Check number of remaining players
				lobby = Lobby.objects.get( name=m_lobby )
				players = lobby.player_set

				if len( players.all( ) ) == 1:
					# Cancel game start, not enough players
					self.server.timer_man.cancel_gs( m_lobby )

				if len( players.all( ) ) == 0:
					# Delete empty lobbies
					lobby.delete( )

				else:	# Notify remaining players
					self.send_parrays( players=players.all( ), lobby=lobby )

			except BaseException as e:
				print e
				self.fail( err_unknown )
						
		elif tag == m_get_available_lobbies:
				
			try:
				# Return list of available lobbies
				lobbies = Lobby.objects.annotate( num_players = Count( 'player' ) ).exclude( num_players = MAX_LOB_SIZE )	
				data = serializers.serialize( 'json', lobbies.all( ) )
				self.success( payload=data )

			except BaseException as e:
				print e
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
				#self.send_to_all( )
				self.success( )
			except:
				print 'Error: m_update_map - unknown'
				self.fail( err_unknown )

		else:
			# unrecognized message received... ignore
			print 'Error: message received with an unrecognized tag, ignoring ...'
	
		return

	def success( self, payload='' ):
		if payload == '':
			content = json.dumps( { 'return': m_success } )
		else:
			content = json.dumps( { 'return' : m_success, 'payload': json.loads( str(payload) ) } )
		print 'Sending: ' + str( content )
		self.request.sendall( 'NL:' + str( len( content ) ) + '\0' )
		self.request.sendall( content )

	def fail( self, msg ):
		content = json.dumps( { 'return' : m_fail, 'message' : msg } )
		self.request.sendall( content )

	def send_parrays( self, players, lobby ):
		
		data_dict = { }
		data_dict.update( { 'tag': m_lobby_update, 'lname': lobby.name } )
		for i in range( len( players ) ):
			data_dict.update( { i : { 'uname': players[ i ].django_user.username, 
						  'pub_ip': players[ i ].pub_ip,
						  'pub_port': players[ i ].pub_port,
						  'priv_ip': players[ i ].priv_ip,
						  'priv_port': players[ i ].priv_port } } )

		self.gcm_send( players=players, collapse_key='lobby_update', msg=data_dict )		

	def gcm_send( self, players, msg, collapse_key=None ):
		
		reg_ids = list( )
		for player in players:
			#reg_ids.append( Player.objects.get( django_user
			reg_ids.append( player.gcm_id )

		print 'Sending push notification ...'
		result = self.server.gcm.send_message( reg_ids=reg_ids, ck=collapse_key, data=msg, retries=2 )

#-------------------------------------------------------------------------------------------------------------#

# Server -------------------------------------------------------------#
class Server( SocketServer.ThreadingMixIn, SocketServer.TCPServer ):
	allow_reuse_address = True
	timeout = None

	def now( self ):
        	d = datetime.datetime.now( )
        	return d.strftime( "%d/%m/%y %H:%M:%S" )
#---------------------------------------------------------------------#

#---------------------------------------------------------------------#
class TimerManager( object ):

	def __init__( self, server ):
		self.timers = dict( )
		self.server = server

	def start_game( self, lobby ):
		print 'Starting game @ ' + lobby.name
		lobby._in_game = True
		lobby.save( )
		del self.timers[ lobby.name ]
		self.gcm_send( players=lobby.player_set.all( ), collapse_key='game_start', msg={ 'tag': m_gs, 'gs_delay': str( long( time.time( ) + GS_DELAY ) ) } )

	def schedule_gs( self, lobby ):
		if self.timers.has_key( lobby.name ): return	
		timer = threading.Timer( 15.0, self.start_game, [ lobby ] )
		timer.start( )
		self.timers.update( { lobby.name : timer } )
		print 'gs @ ' + lobby.name + ' scheduled'

	def cancel_gs( self, lobby ):
		if not self.timers.has_key( lobby.name ): return
		self.timers[ lobby.name ].cancel( )
		del self.timers[ lobby.name ]
		print 'gs @ ' + lobby.name + ' canceled'

	def gcm_send( self, players, msg, collapse_key=None ):
		
		reg_ids = list( )
		for player in players:
			reg_ids.append( player.gcm_id )

		print 'Sending push notification ...'
		result = self.server.gcm.send_message( reg_ids=reg_ids, ck=collapse_key, data=msg, retries=2 )

#---------------------------------------------------------------------#

# Main --------------------------------------------------------------#
if __name__ == "__main__":

	server = Server( ( HOST, PORT ), RequestHandler )
	server.gcm = gcm( GCM_KEY )
	server.timer_man = TimerManager( server )
	print 'Walloff helper server now running: ' + server.now( ) 
	try:
		server.serve_forever( )
	except KeyboardInterrupt:
		pass
#--------------------------------------------------------------------#
