# Game start scheduler #

# Import(s) #
import threading, socket, time
from collections import deque
from lobby_app.models import *
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
		if self.timers.has_key( lobby.name ): return
		timer = threading.Timer( constants.GSS_GS_DELAY, self.start_game, [ lobby ] )
		self.timers.update( { lobby.name: timer } )
		timer.start( )
	
	def cancel_gs( self, lobby ):
		print self.tag + 'canceling gs for ' + str( lobby )
		self.timers[ lobby.name ].cancel( )
		del self.timers[ lobby.name ]

	def start_game( self, lobby ):
		print self.tag + 'starting game in ' + str( lobby ) + ' at ' + str( long( time.time( ) ) )
		lobby._in_game = True
		lobby.save( )
		self.push_multiple( lobby, payload={ constants.tag: constants.lobby_gs, 
			constants.payload: str( long( time.time( ) + constants.GSS_COUNTDOWN_TIME ) ) } )
		del self.timers[ lobby.name ]

	def add_todo( self, intent, lobby ):
		self.cv_todos.acquire( )
		self.todos.append( [ intent, lobby ] )
		self.cv_todos.notify( )
		self.cv_todos.release( )
	
	def handle_todos( self ):
		print self.tag + 'handling todos...'
		
		while self.todos:
			todo = self.todos.popleft( )
			if todo[ 0 ] == constants.T_SCHEDULE:
				self.schedule_gs( todo[ 1 ] )
			elif todo[ 0 ] == constants.T_CANCEL:
				self.cancel_gs( todo[ 1 ] )

	def push_multiple( self, lobby, payload ):
		
		try :
			players = Lobby.objects.get( name=lobby.name ).player_set
			temp_conn = socket.socket( socket.AF_INET, socket.SOCK_DGRAM )

			for player in players.all( ):
				player = Player.objects.get( django_user__username=player.django_user.username )
				print 'Trying to push ' + str( len( payload ) ) + ' byte message to ' + str( player.pub_ip ) + ':' + str( player.pub_port )
				temp_conn.sendto( str( payload ), ( str( player.pub_ip ), int( str( player.pub_port ) ) ) )

		except socket.error as e:
			print self.tag + ( e )

	def run( self ):
		print self.tag + 'up and running...'

		while not self.killed_flg.is_set( ):

			self.cv_todos.acquire( )

			while len( self.todos ) < 1 and not self.killed_flg.is_set( ):
				self.cv_todos.wait( constants.GSS_SLEEP_INTERVAL )

			if self.killed_flg.is_set( ): return

			self.handle_todos( )

			self.cv_todos.release( )
			
