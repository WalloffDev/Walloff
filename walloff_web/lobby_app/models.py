from django.contrib.auth.models import User
from django.db import models

class Lobby( models.Model ):
	
	name = models.CharField( max_length=35, primary_key=True, unique=True )
	_in_game = models.BooleanField( verbose_name='In game status', default=False )
	_map = models.CharField( max_length = 50 )
	size = models.CharField( max_length = 10 )
	moving_obstacles = models.BooleanField( default=False )
	obstacle_count = models.IntegerField( default = 0 )
	shrinkable = models.BooleanField( default=False )

	def __unicode__( self ):
		return self.name

	def set_data( self, name, ig, _map, size, mo, oc, shrink ):
		self.name = name
		self._in_game = ig
		self._map = _map
		self.size = size
		self.moving_obstacles = mo
		self.obstacle_count = oc
		self.shrinkable = shrink
		self.save( )

	def update( self, _map, size, mo, oc, shrink ):
                self._map = _map
                self.size = size
                self.moving_obstacles = mo
                self.obstacle_count = oc
                self.shrinkable = shrink
		self.save( )

class Player( models.Model ):

	# Django User
	django_user = models.OneToOneField( User )
	User._meta.get_field( 'username' )._unique = True

	# Player spec. info
	gcm_id = models.TextField( )
	pub_ip = models.CharField( max_length=15, default='0.0.0.0' )
	priv_ip = models.CharField( max_length=15, default='0.0.0.0' )
	pub_port = models.IntegerField( default=0 )
	priv_port = models.IntegerField( default=0 )
	lobby = models.ForeignKey( Lobby, blank=True, null=True )	

	# METHOD(S)
	def __unicode__( self ):
		return self.django_user.username

	def set_data( self, uname, passwd, gcmid ):
		new_user = User( )
		new_user.username = uname
		new_user.set_password( passwd )
		new_user.save( )
		self.django_user = new_user
		self.gcm_id = gcmid
		self.save( )

	def natural_key( self ):
		return ( self.django_user.username, self.pub_ip, self.priv_ip, self.pub_port, self.priv_port )

	def join_lobby( self, lob ):
		self.lobby = lob
		self.save( )	

	def leave_lobby( self ):
		self.lobby = None
		self.save( )

	def update_net_info( self, pub_ip, pub_port, priv_ip, priv_port ):
		self.pub_ip = pub_ip
		self.pub_port = pub_port
		self.priv_ip = priv_ip
		self.priv_port = priv_port
		self.save( )
