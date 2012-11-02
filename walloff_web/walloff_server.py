# Helper Server for Walloff_Android

import socket, threading, SocketServer, datetime, os
import json
from django.core.management import setup_environ
from walloff_web import settings

# Grab walloff_web settings for access to Django apps
setup_environ( settings )
from lobby_app.models import *

# Server settings
HOST = 'walloff.cslabs.clarkson.edu'
PORT = 8080
MAX_BUF = 1024

#Tags for what commands the server accepts
m_create = 'create'
m_join = 'join'
m_leave = 'leave'
m_get_available_lobbies = 'get_available_lobbies'
m_update_map = 'update_map' 
m_Success = 'SUCCESS'

# Server
class RequestHandler( SocketServer.BaseRequestHandler ):
	
	#def setup( self ):

	def handle( self ):
		data = json.loads( str( self.request.recv( MAX_BUF ) ) )
		tag = data[ 'tag' ]
	
		if tag == m_create:
			try:
				m_name = data[ 'name' ]
				m_map = data[ 'map' ]
				m_size = data[ 'size' ]
				m_moving_obstacles = data[ 'moving_obstacles' ]
				m_number_obstacles = data[ 'number_obstacles' ]
				m_shrinkable = data[ 'shrinkable' ]
				m_host = data[ 'host' ]

				#create the table and populate it with the current player's req information and the parsed value				
				new_lobby = Lobby()
				new_lobby.set_data( m_name, False, m_map, m_size, m_moving_obstacles, m_number_obstacles, m_shrinkable )
				new_lobby.save()

				# get player by username ( auth_token ), add them to lobby
				player = Player.objects.get( m_host )
				player.lobby = new_lobby
				player.save()
				
				
				#creat a json object and send SUCCESS back
			except:
				#create a json object and sen FAIL back. (possably issue a reason of the fail. i.e. map name already taken)
		elif tag == m_join:
			print m_leave
		elif tag == m_leave:
			print m_leave
		elif tag == m_get_available_lobbies:
			print m_get_available_lobbies
		elif tag == m_update_map:
			print m_update_map
		else:
			print 'Error: unrecognized tag'
	
	#def finish( ):
		

class Server( SocketServer.ThreadingMixIn, SocketServer.TCPServer ):
	allow_reuse_address = True
	timeout = None

	def now( self ):
        	d = datetime.datetime.now( )
        	return d.strftime( "%d/%m/%y %H:%M:%S" )

# Main
if __name__ == "__main__":

	server = Server( ( HOST, PORT ), RequestHandler )
	print 'Walloff helper server now running: ' + server.now( ) 
	try:
		server.serve_forever( )
	except KeyboardInterrupt:
		pass
