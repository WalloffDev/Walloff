# Walloff Server #

# Setup server environment --------------------------------------------------------------------------------------------#
from django.core.management import setup_environ
from walloff_web import settings                        # Grab walloff_web settings for access to Django apps
setup_environ( settings )                               # Set server environment to Django instance environment
#----------------------------------------------------------------------------------------------------------------------#

# Import(s) #
import threading, socket, sys
from collections import deque
from django.db import IntegrityError, DatabaseError
from server_resources import constants, handlers, gs_scheduler, monitor, admin_listener

class mgmt( threading.Thread ):

	# Members
	tag = '[MGMT]: '
	server_socket = None
	handlers_queue = None
	monitor = None
	gs_scheduler = None
	admin_listener = None
	ready_flg = None
	killed_flg = None
	
	def __init__( self ):
		try:
			threading.Thread.__init__( self )
			self.ready_flg = threading.Event( )
			self.killed_flg = threading.Event( )
			self.server_socket = socket.socket( socket.AF_INET, socket.SOCK_STREAM )
			self.server_socket.settimeout( None )
			self.server_socket.setsockopt( socket.SOL_SOCKET, socket.SO_REUSEADDR, 1 )
			self.handlers_queue = deque( )
			self.init_handlers( )
			self.init_monitor( )
			self.init_gs_scheduler( )
			self.init_admin_listener( )
			self.ready_flg.set( )	
		except socket.error as e:
			print e

	def init_handlers( self ):
		for i in range( constants.HANDLER_BANK_SIZE ):
			handler = handlers.handler( self )
			self.handlers_queue.append( handler )
			handler.start( )

	def init_monitor( self ):
		self.monitor = monitor.monitor( )
		while not self.monitor.is_ready( ):
			pass
		self.monitor.start( )

	def init_gs_scheduler( self ):
		self.gs_scheduler = gs_scheduler.gs_scheduler( )
		while not self.gs_scheduler.is_ready( ):
			pass
		self.gs_scheduler.start( )

	def init_admin_listener( self ):
		self.admin_listener = admin_listener.admin_listener( self )
		self.admin_listener.start( )

	def is_ready( self ):
		return self.ready_flg.is_set( )

	def run( self ):

		# Listen for connections and delegate connection instances to handlers_queue
		self.server_socket.bind( ( '', constants.SERVER_PORT ) )
		self.server_socket.listen( constants.NUM_Q_CONNS )
		print self.tag + 'Walloff Server up and running @ ' + str( self.server_socket.getsockname( ) )

		while not self.killed_flg.is_set( ):
			try:
				socket_instance, r_addr = self.server_socket.accept( )

				if self.killed_flg.is_set( ): break				
				print self.tag + 'Connection established from ' + str( r_addr )
				handler = self.handlers_queue.popleft( )
				self.handlers_queue.append( handler )
				handler.set_socket( socket_instance, r_addr )

			except socket.error:
				pass

		print self.tag + 'Walloff Server shutting down...'
	
	def die( self ):
		for handler in self.handlers_queue:
			handler.die( )
		self.monitor.die( )
		self.gs_scheduler.die( )
		self.admin_listener.die( )
		self.killed_flg.set( )
		try:
			kill_soc = socket.socket( socket.AF_INET, socket.SOCK_STREAM ).connect( ( '', constants.SERVER_PORT ) )
		except socket.error:
			pass		
# Main #
try:
	threading.stack_size( constants.THREAD_SSIZE )
except BaseException as e:
	print e
manager = mgmt( )

while not manager.is_ready( ):
	pass

manager.start( )
