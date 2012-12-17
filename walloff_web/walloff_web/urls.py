from django.conf.urls import patterns, include, url
from django.contrib import admin

admin.autodiscover()

urlpatterns = patterns( '',
	url( r'^$', 'main_app.views.index' ),
	url( r'^iterations/$', 'main_app.views.iterations' ),
	url( r'^blog/$', 'main_app.views.blog' ),
	url( r'^creators/$', 'main_app.views.creators' ),
	url( r'^', include( 'survey_app.urls' ) ),
	url( r'^', include( 'lobby_app.urls' ) ),
	url( r'^admin/', include( admin.site.urls ) ),
)
