from django.db import models

ANSWER_TYPES = (
	( 'T', 'Text Input' ),
	( 'R', 'Radio List' ),
	( 'C', 'Checkbox List' ),
)

class Survey( models.Model ):
	name = models.CharField( verbose_name = 'name', max_length = 50 )
	description = models.CharField( max_length = 300 )
	is_active = models.BooleanField( verbose_name = 'is active?', default = True )	
	create_date = models.DateTimeField( auto_now_add = True )

	def __unicode__( self ):
		return self.name
	
class Choice( models.Model ):
	choice = models.CharField( max_length = 30 )
	survey = models.ForeignKey( Survey, blank=True )

	def __unicode__( self ):
		return self.choice

class Question( models.Model ):
	question = models.TextField( )
	ans_type = models.CharField( max_length= 18, choices = ANSWER_TYPES )
	choices = models.ManyToManyField( Choice, blank = True )
	survey = models.ForeignKey( Survey )
	
	def __unicode__( self ):
		return self.question

class Response( models.Model ):
	survey = models.ForeignKey( Survey )

	def __unicode__( self ):
		return str( self.pk )

class Answer( models.Model ):
	answer = models.TextField( )
	question = models.ForeignKey( Question )
	response = models.ForeignKey( Response )

	def __unicode__( self ):
		return self.answer
