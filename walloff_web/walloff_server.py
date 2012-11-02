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

# Constants
MAX_LOB_SIZE = 4

#Tags for what commands the server accepts
m_create = 'create'
m_join = 'join'
m_leave = 'leave'
m_get_available_lobbies = 'get_available_lobbies'
m_update_map = 'update_map' 
m_success = 'SUCCESS'
m_fail = 'FAIL'

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
				success( self )
			except:
				#create a json object and sen FAIL back. (possably issue a reason of the fail. i.e. map name already taken)
				fail( self )

		elif tag == m_join:
			try:
				# obtain player that is joining the lobby
				m_host = data[ 'player' ]
				m_lobby = data[ 'lobby' ]
				player = Player.objects.get( m_host )
				
				# check number of players currently in lobby
				players = Lobby.objects.get( m_lobby ).player_set
				if len( players.all( ) ) < MAX_LOB_SIZE:
		
					# add playet to lobby
					player.lobby = Lobby.objects.get( m_lobby )
					player.save( )
	
					# TODO: send success and player arrays
					# dts = json.dumps( str( players.all( ) ) ) 
					success( self )
				else:
					# TODO: notify host that lobby is no longer available
					fail( self )
			except:
				fail( self )
	
		elif tag == m_leave:
			try:
				#obtain the player that is currently leaving the lobby
				m_host = data[ 'player' ]
				player = Player.objects.get( m_host )
				
				#remove the player in the lobby by setting that player's lobby to nothing
				player.lobby = None
				player.save( )

				#send a success message
				success( self )

				#TODO: send updated player arrays to lobby members
			except:
				#send an error message
				fail( self )
						
		elif tag == m_get_available_lobbies:
			print m_get_available_lobbies
		elif tag == m_update_map:
			print m_update_map
		else:
			print 'Error: unrecognized tag'
	
	#def finish( ):
		
	def success( self ):
		content = { 'return' : m_success }
		content = json.dumps( content )
		self.write( content )

	def fail( self ):
		content = { 'return' : m_fail }
		content = json.dumps( content )
		self.write( content )

	def send_to_all( self ):
		print 'Sending out player arrays'


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
