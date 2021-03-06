# Constants #

# default
THREAD_SSIZE = 1000000

# mgmt #
SERVER_PORT = 8080
NUM_Q_CONNS = 5
HANDLER_BANK_SIZE = 5

# monitor #
MONITOR_PORT = 8081
MONITOR_RECV_LEN = 512
MONITOR_TIMEOUT = 0.5

# gs_scheduler -----------------------------------------#
GSS_SLEEP_INTERVAL = 0.25
GSS_GS_DELAY = 15.0
GSS_COUNTDOWN_TIME = 25.0
T_SCHEDULE = 1
T_CANCEL = 2
#-------------------------------------------------------#

# handlers ---------------------------------------------#
HANDLER_SLEEP_INTERVAL = 0.10
MAX_LOBBY_SIZE = 4
#-------------------------------------------------------#

# admin_listener ---------------------------------------#
KILL_COMMAND = 'q'
#-------------------------------------------------------#

# message tags -----------------------------------------#
register = 'register'                                 # Create new user req.
unregister = 'unregister'			      # Remove user req.
create = 'create'                                     # Create new lobby req.
join = 'join'                                         # Join existing lobby req.
leave = 'leave'                                       # Leave current lobby req.
get_available_lobbies = 'get_lobbies'                 # Query existing lobbies req.
update_lobby = 'lobby_update'			      # Lobby players update
lobby_gs = 'lobby_gs'
update_map = 'update_map'                             # Lobby map update req.
success = 'success'                                   # Server success msg to client
failure = 'failure'                                   # Server error msg to client
#-------------------------------------------------------#

# message keys -----------------------------------------#
tag = 'tag'
status = 'status'
payload = 'payload'
uname = 'uname'
passwd = 'passwd'
pub_up = 'pub_ip'
pub_port = 'pub_port'
priv_ip = 'priv_ip'
priv_port = 'priv_port'
lname = 'lname'
mname = 'mname'
msize = 'msize'
mmoving_obstacles = 'mmoving_obstacles'
mnumber_obstacles = 'mnumber_obstacles'
mshrinkable = 'mshrinkable'
mobstacle_init_pattern = 'obs_init_pattern'
mobstacle_move_pattern = 'moving_obs_pattern'
#-------------------------------------------------------#

# error messages ---------------------------------------#
err_unknown = 'unknown error'
err_uname_exists = 'username not available'
err_lname_exists = 'lobby name already exists'
err_lobb_unavail = 'lobby is no longer available'
#-------------------------------------------------------#
