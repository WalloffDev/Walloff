from django.db import models

ANSWER_TYPES = (
	( 'T', 'Text Input' ),
	( 'S', 'Select One Choice' ),
	( 'R', 'Radio List' ),
	( 'C', 'Checkbox List' ),
)

class Survey( models.Model ):
	name = models.CharField( verbose_name = 'name', max_length = 50 )
	description = models.CharField( max_length = 300 )
	create_date = models.DateTimeField( auto_now_add = True )
	is_active = models.BooleanField( verbose_name = 'is active?', default = True )	

	def __unicode__( self ):
		return self.name
	
	class Admin:
		search_fields = ( 'name', 'description', )
		list_display = ( 'name', )
		list_filter = ( 'name', 'description', )

class Question( models.Model ):
	survey = models.ForeignKey( Survey )
	question = models.TextField( )
	#ans_type = models.CharField( max_length= 18, choices = ANSWER_TYPES )
	add_date = models.DateTimeField( auto_now_add = True )

	def __unicode__( self ):
		return self.question
	
	class Admin:
		search_fields = ( 'survey', 'question', )
		list_display = ( 'survey', 'id', 'add_date', 'ans_type', )
		list_filter = ( 'survey', 'add_date', )

class Response( models.Model ):
	survey = models.ForeignKey( Survey )

class Answer( models.Model ):
	answer = models.TextField( )
	response = models.ForeignKey( Response )
	question = models.ForeignKey( Question )

	def __unicode__( self ):
		return self.answer
