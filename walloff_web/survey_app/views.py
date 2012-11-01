from django.core.urlresolvers import reverse
from django.http import HttpResponseRedirect
from django.shortcuts import render_to_response, get_object_or_404
from django.template import RequestContext

from models import Survey, Answer, Response
from forms import get_answer_formset

def index( request ):
	return render_to_response( 'index.html' )

#def proj_iters( request ):
#	return render_to_response( 'survey_app/iterations.html' )

def list_surveys( request ):
	surveys = Survey.objects.all( )

	responseParameters = { "all_surveys": surveys, }
	return render_to_response( 'survey_app/list_surveys.html', responseParameters,
		context_instance = RequestContext( request ) )

def take_survey( request, survey_id ):
	survey = get_object_or_404( Survey, pk = survey_id )
	
	# if survey !is_active -> Redirect to not_active.html
	if not survey.is_active:
		return render_to_response( 'survey_app/not_active.html' )
	
	questions = survey.question_set.all( )
	AnswerFormset = get_answer_formset( questions )
	formset = AnswerFormset( request.POST or None, queryset = Answer.objects.none( ) )

	if formset.is_valid( ):
		answers = formset.save( commit = False )
		response = Response( survey = survey )
		response.save( )
		for answer in answers:
			answer.response = response
			answer.save( )
		return HttpResponseRedirect( reverse( 'survey_app.views.list_surveys' ) )
	else: print 'Invalid Form data'

	# Change input fields here? #
	for index in range( len( questions ) ):
		question = questions[ index ]
		form = formset.forms[ index ]
		form.fields[ 'answer' ].label = question.question
		form.fields[ 'question' ].initial = question

	responseParameters = {
		"survey": survey,
		"formset": formset,
	}
	return render_to_response( 'survey_app/take_survey.html', responseParameters,
		context_instance = RequestContext( request ) )
