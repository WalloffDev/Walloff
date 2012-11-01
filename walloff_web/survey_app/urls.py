from django.conf.urls.defaults import *
from django.contrib import admin, databrowse
from survey_app.models import *
import views

admin.autodiscover( )

urlpatterns = patterns( '',
	( r'^surveys/$', views.list_surveys ),
	( r'^surveys/(?P<survey_id>\d+)/$', views.take_survey ),
	( r'^data/(.*)', databrowse.site.root ),
)

databrowse.site.register( Survey, Response, Question, Answer )


