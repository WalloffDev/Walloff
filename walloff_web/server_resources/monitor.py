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
			self.monitor_socket = socket.socket( socket.AF_INET, socket.SOCK_DGRAM )
			self.monitor_socket.setsockopt( socket.SOL_SOCKET, socket.SO_REUSEADDR, 1 )
			self.monitor_socket.settimeout( constants.MONITOR_TIMEOUT )
			self.monitor_socket.bind( ( '', constants.MONITOR_PORT ) )
			self.ready_flg.set( )
		except socket.error as e:
			print self.tag + str( e )

	def is_ready( self ):
		return self.ready_flg.is_set( )

	def receive_and_parse( self ):

		payload, r_addr = self.monitor_socket.recvfrom( constants.MONITOR_RECV_LEN )
		print self.tag + 'received ' + str( len( payload ) ) + ' byte heartbeat from ' + str( r_addr )
		payload = json.loads( payload )
		client = Player.objects.get( django_user__username=str( payload[ constants.uname ] ).lower( ) )
		client.update_net_info( r_addr[ 0 ], r_addr[ 1 ], str( payload[ constants.priv_ip ] ), str( payload[ constants.priv_port ] ) )

	def run( self ):
		try:
			print self.tag + 'listening for heartbeats...'
			
			while not self.killed_flg.is_set( ):
				try:
					self.receive_and_parse( )
				except socket.timeout:
					continue
				except socket.error:
					pass

		except BaseException as e:
			print self.tag + str( e )

	def die( self ):
		self.killed_flg.set( )
