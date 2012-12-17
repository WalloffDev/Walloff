# Python Google Cloud Messaging ( GCM ) Module
# - utility for 3rd-party servers written in python to contact android clients via GCM servers

from types import *
import urllib2, json, time
from constants import *

class Message:
	
	def __init__( self, reg_ids=None, data=None, ck=None, dwi=False, ttl=None ):

		# Check registration ids
		if reg_ids is None or type( reg_ids ) is not ListType:
			raise MessageParamException( "Need at least one registration ID, or invalid type" )
		elif len( reg_ids ) > 1000 or len( reg_ids ) < 1:
			raise MessageParamException( "Too many registration ID's or empty reg_id list" )
		self.registration_ids = reg_ids

		# Check data
		if type( data ) is not NoneType and type( data ) is not DictType:
                        raise MessageParamException( "Data param is not a dictionary" )
                self.data = data
		
		# Check collapse key
		if ck is not None and type( ck ) is not StringType:
			raise MessageParamException( "Collapse key param is not a string" )
		self.collapse_key = ck

		# Check delay while idle
		if dwi is not None and type( dwi ) is not BooleanType:
			raise MessageParamException( "Delay while idle param is not a boolean" )
                self.delay_while_idle = dwi

		# Check time to live
		if ttl is not None and ck is None:
			raise MessageParamException( "There is no collapse key for our time to live value" )
		elif ttl is not None and type( ttl ) is not IntType:
			raise MessageParamException( "Time to live param is not an integer" )
		elif ttl is not None and ( ttl > 2419200 or ttl < 0 ):
			raise MessageParamException( "Time to live is too big or too small" )
                self.time_to_live = ttl

class gcm( object ):
	
	def __init__( self, api_key ):
		self.api_key = api_key
		self.backoff = backoff_initial_delay
	
	def build_payload( self, data, reg_ids, ttl, ck, dwi ):

		# Create new message
		message = Message( reg_ids, data, ck, dwi, ttl )

		# Construct payload
		payload = { param_registration_ids : message.registration_ids }
		if message.data:
			payload[ param_data ] = message.data
		if message.collapse_key:
			payload[ param_collapse_key ] = message.collapse_key		
                if message.time_to_live:
			payload[ param_time_to_live ] = message.time_to_live
		if message.delay_while_idle:
			payload[ param_delay_while_idle ] = message.delay_while_idle	

                return json.dumps( payload )

	def send_message( self, data=None, reg_ids=None, ttl=None, ck=None, dwi=None, retries=1 ):
                attempt = 0
                for attempt in range( retries ):

                        payload = self.build_payload( data, reg_ids, ttl, ck, dwi )

			# Sanity check
                      	try:
                               json.loads( payload )
                        except:
                               raise MalformedJsonException( "The payload object is not in a JSON format." )

                        # Headers for the GCM message         
                        headers = { 'Content-Type' : 'application/json' , 'Authorization' : 'key=' + self.api_key }

                        # Create request and send
                        req = urllib2.Request( gcm_url, payload, headers )
			try:
                                response = urllib2.urlopen( req ).read( )
				response = json.loads( response )
				print 'GCM Response: ' + str( response )
				if response[ json_failure ] == 0 and response[ json_canonical_ids ] == 0:
					return ( ) 
				else:
					return self.handle_200_response( response )

			except urllib2.HTTPError as e:
				handle_GCM_error( e, attempt )
				continue

	def handle_200_response( self, response ):

		# Make dict to pass back ( map old reg_ids to new reg_ids )
		id_updates = dict( )
		failures = dict( )

		if response[ json_canonical_ids ] > 0:

			result_values = response[ json_results ]

			for index in range( len( result_values ) ):
				d = dict( result_values[ index ] )
				if json_registration_id in d:
					id_updates[ index ] = d[ json_registration_id ]

		if response[ json_failure ] > 0:

			result_values = response[ json_results ]

			for index in range( len( result_values ) ):
				d = dict( result_values[ index ] )
				if json_error in d:
					failures[ index ] = d[ json_error ]

		return ( id_updates, failures )

	def handle_GCM_error( self, e, attempt ):
		if e.code == 400:
			raise MalformedJsonException( "The request could not be parsed as JSON" )
                elif e.code == 401:
			raise AuthenticationException( "There was an error authenticating the sender account" )
		elif e.code >= 500:
			
			# Delay send
			time.sleep( self.backoff )

			# Adjust backoff	
			self.backoff = self.backoff * 2 
			if self.backoff >= backoff_max_delay:
				raise UnavailableException( "GCM Service is unavailable" )

			return