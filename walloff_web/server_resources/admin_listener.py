# Admin listener #

# Import(s)
import threading, sys
import constants

class admin_listener( threading.Thread ):
	
	# Member(s)
	tag = '[ADMIN_LISTENER]: '
	mgmt = None
	killed_flg = None

	def __init__( self, mgmt ):
		threading.Thread.__init__( self )
		self.mgmt = mgmt
		self.killed_flg = threading.Event( )

	def obey_admin( self ):
		command = ''

		while not self.killed_flg.is_set( ):
			command += sys.stdin.read( 1 )

			if constants.KILL_COMMAND in command:
				self.mgmt.die( )

			else: command = ''

	def run( self ):
		self.obey_admin( )

	def die( self ):
		self.killed_flg.set( )		
