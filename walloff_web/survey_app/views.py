from django.core.context_processors import csrf
from django.core.urlresolvers import reverse
from django.http import HttpResponseRedirect
from django.shortcuts import render_to_response, get_object_or_404, redirect
from django.template import RequestContext

from models import *

def index( request ):
	return render_to_response( 'index.html' )

def list_surveys( request ):
	surveys = Survey.objects.all( )

	responseParameters = { "all_surveys": surveys, }
	return render_to_response( 'survey_app/list_surveys.html', responseParameters,
		context_instance = RequestContext( request ) )

def take_survey( request, survey_id ):

	# Get csrf token
	response_params = { }
	response_params.update( csrf( request ) )

	# Grab the requested survey
	survey = get_object_or_404( Survey, pk = survey_id )

	# if survey !is_active -> Redirect to not_active.html
	if not survey.is_active:
		return render_to_response( 'survey_app/not_active.html' )

	response_params.update( { 'survey': survey, 'questions': survey.question_set.all( ) } )

	return render_to_response( 'survey_app/take_survey.html', response_params, 
		context_instance = RequestContext( request ) ) 

def submit_response( request, survey_id ):
	
	# Get question set from survey
	survey = get_object_or_404( Survey, pk = survey_id )
	questions = survey.question_set.all()

	# Create the response
	response = Response( )
	response.survey = survey
	response.save( )

	# Add the question/answer pairs to a response
	for question in questions:
		if question.ans_type == 'T':
			answer = Answer( )
			answer.response = response
			answer.question = question
			answer.answer = str( request.POST[ str( question.pk ) ] )
			answer.save( )
		elif question.ans_type == 'R':
			answer = Answer( )
			answer.response = response
			answer.question = question
			answer.answer = str( request.POST[ str( question.pk ) ] )
			answer.save( )
		
	return render_to_response( 'index.html' )
