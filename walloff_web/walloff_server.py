# Helper Server for Walloff_Android

# Setup environment
from django.core.management import setup_environ
from walloff_web import settings # Grab walloff_web settings for access to Django apps
setup_environ( settings )

# Imports
import socket, threading, SocketServer, datetime, os, json
from django.core import serializers
from lobby_app.models import *
from gcm import GCM

# Server metadata
HOST = 'walloff.cslabs.clarkson.edu'
PORT = 8080
#GCM_KEY = 'AIzaSyBDkOmbiJ_CLOs3JW8Gy-04PXm2LO7qx3E'
GCM_KEY = 'AIzaSyATBxZaCI6D3rSyZaHjwswkdcmIWHjztaE'

# Constants
MAX_BUF = 1024
MAX_LOB_SIZE = 4

# Tags
m_create = 'create'
m_join = 'join'
m_leave = 'leave'
m_get_available_lobbies = 'get_available_lobbies'
m_update_map = 'update_map' 
m_success = 'SUCCESS'
m_fail = 'FAIL'

# Connection Handler
class RequestHandler( SocketServer.BaseRequestHandler ):
	
	def handle( self ):
		data = json.loads( str( self.request.recv( MAX_BUF ) ) )
		tag = data[ 'tag' ]
	
		if tag == m_create:

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
			
			# Lobby name already in use
			except IntegrityError:
				print 'Error: lobby name is already in use'
				self.fail( )
			except:
				self.fail( )

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
					# TODO: notify host that lobby is no longer available
					self.fail( )
			except:
				self.fail( )
	
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
				self.fail( )
						
		elif tag == m_get_available_lobbies:
				
			try:
				# Return list of available lobbies
				lobbies = Lobby.objects.annotate( num_players = Count( 'player' ) ).exclude( num_players = MAX_LOB_SIZE )	
				data = serializers.serialize( 'json', lobbies.all( ) )
				self.request.sendall( data )

				self.success( )
			except:
				self.fail( )			

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
				self.fail( )

		else:
			# Possible foreign message received... ignore
			print 'Error: message received with an unrecognized tag, ignoring ...'
		
	def success( self ):
		content = json.dumps( { 'return' : m_success } )
		self.request.sendall( content )

	def fail( self ):
		content = json.dumps( { 'return' : m_fail } )
		self.request.sendall( content )

	def send_to_all( self, players, data ):		
		gcm_ids = []

		try:
			# Get player gcm_id's
			for player in players:
				gcm_ids.append( player.gcm_id )

			response = gcm.json_request( registration_ids = gcm_ids, data = data )

			# Check response
			print response			

		# TODO: catch exception and handle appropriately
		except:
			pass

class Server( SocketServer.ThreadingMixIn, SocketServer.TCPServer ):
	allow_reuse_address = True
	timeout = None

	def now( self ):
        	d = datetime.datetime.now( )
        	return d.strftime( "%d/%m/%y %H:%M:%S" )

# Main
if __name__ == "__main__":

	server = Server( ( HOST, PORT ), RequestHandler )
	server.gcm = GCM( GCM_KEY )
	print 'Walloff helper server now running: ' + server.now( ) 
	try:
		server.serve_forever( )
	except KeyboardInterrupt:
		pass
