from django.db import models

class Lobby( models.Model ):
	
	name = models.CharField( max_length=35 )
	_in_game = models.BooleanField( verbose_name='In game status', default=False )

	def __unicode__( self ):
		return self.name

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
	lobby = models.ForeignKey( Lobby )	

	# METHOD(S)
	def __unicode__( self ):
		return self.username
