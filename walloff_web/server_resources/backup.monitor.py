# Monitors heartbeats from Android clients #

# Import(s) #
import threading, socket, json
import constants
from lobby_app.models import *

class monitor( threading.Thread ):

	# Member(s)
	tag = '[MONITOR]: '
	monitor_socket = None
	ready_flg = None
	killed_flg = None

	def __init__( self ):
		try:
			threading.Thread.__init__( self )
			self.ready_flg = threading.Event( )
			self.killed_flg = threading.Event( )
			self.monitor_socket = socket.socket( socket.AF_INET, socket.SOCK_STREAM )
			self.monitor_socket.setblocking( 0 )
			self.monitor_socket.setsockopt( socket.SOL_SOCKET, socket.SO_REUSEADDR, 1 )
			self.ready_flg.set( )
		except socket.error as e:
			print self.tag + e

	def is_ready( self ):
		return self.ready_flg.is_set( )

	def get_npl( self, heartbeat_conn ):
		npl = ''
		
		while 1:
			npl += heartbeat_conn.recv( 1 )
			if npl[ -1 ] == '\0':
				npl = npl[ :-1 ]
				return npl.split( 'NPL:' )[ 1 ]

	def receive_and_parse( self, heartbeat_conn, r_addr ):
		npl = self.get_npl( heartbeat_conn )
		payload = heartbeat_conn.recv( int( npl ) )
		print self.tag + 'received ' + str( len( str( payload ) ) ) + ' byte heartbeat from ' + str( r_addr )
		payload = json.loads( payload )

		client = Player.objects.get( django_user__username=str( payload[ constants.uname ] ).lower( ) )
		client.update_net_info( r_addr[ 0 ], r_addr[ 1 ], str( payload[ constants.priv_ip ] ), str( payload[ constants.priv_port ] ) )

		heartbeat_conn.close( )

	def run( self ):
		try:
			self.monitor_socket.bind( ( '', constants.MONITOR_PORT ) )
			self.monitor_socket.listen( constants.MONITOR_NUM_QCONNS )
			print self.tag + 'listening for heartbeats...'
			
			while not self.killed_flg.is_set( ):
				try:
					heartbeat_conn, r_addr = self.monitor_socket.accept( )
					self.receive_and_parse( heartbeat_conn, r_addr )
				except socket.error:
					pass

		except BaseException as e:
			print self.tag + e		

	def die( self ):
		self.killed_flg.set( )
