from django import forms
from django.forms.models import modelformset_factory

from models import Answer

class AnswerForm( forms.ModelForm ):
	class Meta:
		model = Answer
		exclude = [ 'survey', 'response', ]
	# Change input fields here? #
	#answer = forms.TextField( )

class SurveyForm( forms.ModelForm ):
	class Meta:
		model = Answer
		exclude = [ 'survey', 'response', ]
	answer = forms.CharField( max_length = 255 )

def get_answer_formset( questions ):
	AnswerFormset = modelformset_factory( Answer, form = AnswerForm, can_delete = False, extra = len( questions ) )
	return AnswerFormset

def get_survey_formset( questions ):
	SurveyFormset = modelformset_factory( Answer, form = SurveyForm, can_delete = False, extra = len( questions ) )
