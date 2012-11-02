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

class Player( models.Model ):

	#TODO:	# Implement Player password field, set, check, change, recover
		# Game statistics

	# CLASS VAR(S)
	first_name = models.CharField( max_length=512 )
	last_name = models.CharField( max_length=512 )
	username = models.CharField( max_length=50, primary_key=True, unique=True )
	_active = models.BooleanField( verbose_name='Player activation status', default=True )
	_create_date = models.DateTimeField( auto_now_add=True )
	pub_ip = models.CharField( max_length=15, default='0.0.0.0' )
	priv_ip = models.CharField( max_length=15, default='0.0.0.0' )
	pub_port = models.IntegerField( default=0 )
	priv_port = models.IntegerField( default=0 )
	lobby = models.ForeignKey( Lobby, blank=True, null=True )	

	# METHOD(S)
	def __unicode__( self ):
		return self.username
