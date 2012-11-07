# Constants to be used by gcm module

# 3RD PARTY SERVER
backoff_initial_delay = 1000
backoff_max_delay = 1024000
utf_8 = 'UTF-8'

# TOKEN FIELD(S)
token_canonical_reg_id = 'registration_id'
token_error = 'Error'
token_message_id = 'id'

# MESSAGE PARAM(S)
gcm_url = 'https://android.googleapis.com/gcm/send'
param_collapse_key = 'collapse_key'
param_delay_while_idle = 'delay_while_idle'
param_data = 'data'
param_registration_id = 'registration_id'
param_time_to_live = 'time_to_live'

# JSON PARAM(S)
json_canonical_ids = 'canonical_ids'
json_error = 'error'
json_failure = 'failure'
json_message_id = 'message_id'
json_multicast_id = 'multicast_id'
json_data = 'data'
json_registration_ids = 'registration_ids'
json_results = 'results'
json_success = 'success'

# ERROR(S)
err_device_quota_exceeded = 'DeviceQuotaExceeded'
err_internal_server_error = 'InternalServerError'
err_invalid_registration = 'InvalidRegistration'
err_invalid_ttl = 'InvalidTimeToLive'
err_message_too_big = 'MessageTooBig'
err_mismatch_sender_id = 'MissingRegistration'
err_missing_collapse_key = 'MissingCollapseKey'
err_missing_registration = 'MissingRegistration'
err_not_registered = 'NotRegistered'
err_quota_exceeded = 'QuotaExceeded'
err_unavailable = 'Unavailable'

# CUSTOM EXCEPTION(S)
class MessageParamException( Exception ) : pass
class MalformedJsonException( Exception ) : pass
class AuthenticationException( Exception ) : pass
class UnavailableException( Exception ) : pass
class CanonicalIdsException( Exception ) : pass
