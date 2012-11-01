# Helper Server for Walloff_Android

import socket, threading, SocketServer, datetime, os
from django.core.management import setup_environ
from walloff_web import settings

# Grab walloff_web settings for access to Django apps
setup_environ( settings )
from lobby_app.models import *

# Server settings
HOST = 'walloff.cslabs.clarkson.edu'
PORT = 8080

class iPlayer( ):

	def __init__( self, uname, pub_ip, pub_port, priv_ip, priv_port ):
		self.uname = uname
		self.pub_ip = pub_ip
		self.priv_ip = priv_ip
		self.pub_port = pub_port
		self.priv_port = priv_port
		self.in_game = False
	
	def get_info( self ):
		print 'Username: ' + str( self.uname ) + '\n\t' + \
			'Public: ' + str( self.pub_ip ) + ' ' + str( self.pub_port ) + \
			'Private: ' + str( self.priv_ip ) + ' ' + str( self.priv_port )

# Server
class RequestHandler( SocketServer.BaseRequestHandler ):
	
	#def setup( self ):

	def handle( self ):
		players = Player.objects.all( )
		print len( players )		
		print 'Connection established'
	
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
