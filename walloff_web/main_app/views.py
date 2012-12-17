from django.http import HttpResponse
from django.shortcuts import render_to_response

def index( request ):
	return render_to_response( 'index.html' )

def iterations( request ):
	return render_to_response( 'iterations.html' )

def blog( request ):
	return render_to_response( 'blog.html' )

def creators( request ):
	return render_to_response( 'creators.html' )
