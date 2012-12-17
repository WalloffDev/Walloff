# Game start scheduler #

# Import(s) #
import threading, socket
from collections import deque
import constants

class gs_scheduler( threading.Thread ):

	# Member(s) #
	tag = '[GS_SCHEDULER]: '
	ready_flg = None
	killed_flg = None
	timers = None
	cv_todos = None
	todos = None

	def __init__( self ):
		threading.Thread.__init__( self )
		self.ready_flg = threading.Event( )
		self.killed_flg = threading.Event( )
		self.cv_todos = threading.Condition( )
		self.timers = dict( )
		self.todos = deque( )
		self.ready_flg.set( )

	def is_ready( self ):
		return self.ready_flg.is_set( )

	def die( self ):
		self.killed_flg.set( )

	def schedule_gs( self, lobby ):
		print self.tag + 'scheduling gs for ' + str( lobby )
		######
	
	def cancel_gs( self, lobby ):
		print self.tag + 'canceling gs for ' + str( lobby )
		######

	def start_game( self, lobby ):
		print self.tag + 'starting game in ' + str( lobby )
		######

	def handle_todos( self ):
		print self.tag + 'handling todos...'
		###########

	def run( self ):
		print self.tag + 'up and running...'

		while not self.killed_flg.is_set( ):

			self.cv_todos.acquire( )

			while len( self.todos ) < 1 and not self.killed_flg.is_set( ):
				self.cv_todos.wait( constants.GSS_SLEEP_INTERVAL )

			if self.killed_flg.is_set( ): return

			self.handle_todos( )

			self.cv_todos.release( )
			
