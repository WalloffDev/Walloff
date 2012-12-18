# Request handler #

# Import(s) #
import threading, socket, json
from django.db import IntegrityError, DatabaseError
import constants
from lobby_app.models import *

class handler( threading.Thread ):
	
	# Member(s)
	tag = None
	socket = None
	r_addr = None
	cv_socket = None
	ready_flg = None
	killed_flg = None

	# MGMT methods #
	def __init__( self ):
		threading.Thread.__init__( self )
		self.cv_socket = threading.Condition( )
		self.ready_flg = threading.Event( )
		self.killed_flg = threading.Event( )
		self.ready_flg.set( )
		
	def is_ready( self ):
		return self.ready_flg.is_set( )

	def set_socket( self, socket, r_addr ):
		self.cv_socket.acquire( )
		self.socket = socket
		self.r_addr = r_addr
		self.cv_socket.notify( )
		self.cv_socket.release( )

	def get_npl( self ):
                npl = ''

		while 1:
                        npl += self.socket.recv( 1 )
                        if npl[ -1 ] == '\0':
                                npl = npl[ :-1 ]
                                return int( npl.split( 'NPL:' )[ 1 ] )

	def run( self ):
		self.tag = '[HANDLER:' + str( self.ident ) + ']: '

		while not self.killed_flg.is_set( ):
			self.cv_socket.acquire( )
			
			while not self.killed_flg.is_set( ) and self.socket is None:
				self.cv_socket.wait( constants.HANDLER_SLEEP_INTERVAL )

			if self.killed_flg.is_set( ): return

			print self.tag + 'servicing request from ' + str( self.r_addr )
			self.receive_and_parse( )
			self.reset( )

	def die( self ):
		self.killed_flg.set( )

	def reset( self ):
		self.socket = None
		self.r_addr = None
		self.cv_socket.release( )

	# Handler methods #
	def receive_and_parse( self ):
		print self.tag + 'getting npl'
		npl = self.get_npl( )
		print self.tag + 'getting payload: ' + str( npl )
		payload = self.socket.recv( int( npl ) )
		print self.tag + 'received payload: ' + str( len( payload ) )
		payload = json.loads( str( payload ) )
		tag = payload[ constants.tag ]

		# registration #
		if tag == constants.register:
			try:
				uname = payload[ constants.uname ]
				
				# check if username already exists
				existing_user = Player.objects.filter( django_user__username=str( uname ).lower( ) )
				if len( existing_user.all( ) ) > 0:
					raise IntegrityError
				
				# if not, create new user
				new_user = Player( )
				new_user.set_data( str( uname ).lower( ), str( payload[ constants.passwd ] ) )
				self.respond( constants.success ) # generate reg_id and return !!!
	
			except IntegrityError:
				self.respond( constants.failure, payload=constants.err_uname_exists )
			except BaseException as e:
				print self.tag + str( e )
				self.respond( constants.failure, payload=constants.err_unknown )

		elif tag == constants.unregister:
			print 'Unregistration received'
		elif tag == constants.create:
			try:
                                # Get info for new lobby and map
				uname = payload[ constants.uname ]
                                lname = payload[ constants.lname ]
                                mname = payload[ constants.mname ]
                                msize = payload[ constants.msize ]
                                mmoving_obstacles = payload[ constants.mmoving_obstacles ]
                                mnumber_obstacles = payload[ constants.mnumber_obstacles ]
                                mshrinkable = payload[ constants.mshrinkable ]

                                # Create the lobby and populate it with parsed info     
                                new_lobby = Lobby( )
                                new_lobby.set_data( lname, False, mname, msize, mmoving_obstacles, mnumber_obstacles, mshrinkable )

                                # get player by username ( auth_token ), add them to lobby
                                player = Player.objects.get( django_user__username = str( uname ).lower( ) )
                                player.join_lobby( new_lobby )
                                self.respond( constants.success )

                                # push lobby update
                                self.push_multiple( lobby=new_lobby, payload='test push message' )

                        except IntegrityError:
                                self.respond( constants.failure, payload=constants.err_lname_exists )
                        except BaseException as e:
                                print self.tag + str( e )
                                self.respond( constants.failure, payload=constants.err_unknown )			

		elif tag == constants.join:
			print 'Join lobby received'
		elif tag == constants.leave:
			print self.tag + 'Leave lobby received'

			try:
                                # Obtain the player that is currently leaving the lobby
                                uname = payload[ constants.uname ]
                                player = Player.objects.get( django_user__username = str( uname ).lower( ) )
                                lname = player.lobby

                                # Remove the player from the lobby
                                player.leave_lobby( )
                                self.respond( constants.success )

                                # Check number of remaining players
                                lobby = Lobby.objects.get( name=lname )
                                players = lobby.player_set

                                #if len( players.all( ) ) == 1:
                                        # Cancel game start, not enough players
                                #        self.server.timer_man.cancel_gs( m_lobby )

                                if len( players.all( ) ) == 0:
                                        # Delete empty lobby
                                        lobby.delete( )

                                #else:   # Notify remaining players
                                #        self.send_parrays( players=players.all( ), lobby=lobby )

                        except BaseException as e:
                                print self.tag + str( e )
                                self.respond( constants.failure, payload=constants.err_unknown )


		elif tag == constants.get_available_lobbies:
			print 'Get available lobbies received'
		elif tag == constants.update_map:
			print 'Map update received'
		else:
			print 'unrecognized message tag'

	def respond( self, status, payload='' ):
		if payload == '':
			response = json.dumps( { constants.status: status } )
		else:
			response = json.dumps( { constants.status: status, constants.payload: json.dumps( str( payload ) ) } )
                print self.tag + 'Sending: ' + str( response )
                self.socket.sendall( 'NPL:' + str( len( response ) ) + '\0' )
                self.socket.sendall( response )

	def push_multiple( self, lobby, payload ):
		
		try :
			players = Lobby.objects.get( name=lobby.name ).player_set
			temp_conn = socket.socket( socket.AF_INET, socket.SOCK_DGRAM )
			#temp_conn.setsockopt( socket.SOL_SOCKET, socket.SO_REUSEADDR, 1 )
			#temp_conn.bind( ( '', 8081 ) )

			for player in players.all( ):
				player = Player.objects.get( django_user__username=player.django_user.username )
				print 'Trying to push ' + str( len( payload ) ) + ' byte message to ' + str( player.pub_ip ) + ':' + str( player.pub_port )
				temp_conn.sendto( str( payload ), ( str( player.pub_ip ), int( player.pub_port ) ) )
		except socket.error as e:
			print self.tag + ( e )
