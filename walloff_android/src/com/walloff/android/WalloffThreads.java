package com.walloff.android;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.json.JSONObject;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class WalloffThreads {
	
	/* handles normal lobby traffic between client and WalloffServer */
	public static class Sender extends Thread {
		
		ProgressDialog p_dialog = null;
		Dialog dialog = null;
		String identity = "Sender";
		Context activity_context = null;
		JSONObject to_send = null;
		Socket conn = null;
		DataOutputStream dos = null;
		DataInputStream dis = null;
		Intent  intent = null;
		Lock lock = null;
		Condition cv = null;
		Handler handler = null;
		
		public Sender( Context activity_context ) {
			super( );
			this.activity_context = activity_context;
			this.lock = new ReentrantLock( );
			this.cv = lock.newCondition( );
		}
		
		public void setIntent( Intent intent ) {
			this.intent = intent;
		}
		
		public void sendMessage( JSONObject to_send ) {
			try {
				this.lock.lock( );
				this.to_send = to_send;
				this.cv.signal( );
				this.lock.unlock( );
			} catch( Exception e ) {
				e.printStackTrace( );
			}
		}
		
		public void setDialog( Dialog dialog ) {
			this.dialog = dialog;
		}
		
		public void setPDialog( ProgressDialog pdialog ) {
			this.p_dialog = pdialog;
		}
		
		public void setHandler( Handler handler ) {
			this.handler = handler;
		}
		
		public void handle_result( JSONObject result ) {
			Log.i( this.identity, "recvd: " + result.toString( ) );
			
			try {
				if( this.p_dialog != null )
					this.p_dialog.cancel();
				
				if ( result.get( Constants.STATUS ).equals( Constants.SUCCESS ) ) {
					if( this.dialog != null )
						this.dialog.cancel();
					
					if( result.has( Constants.PAYLOAD ) ) {
						Message temp = new Message( );
						Bundle bundle = new Bundle( );
						bundle.putString( Constants.PAYLOAD, result.getString( Constants.PAYLOAD ) );
						temp.setData( bundle );
						this.handler.sendMessage( temp );
					}
					
					if( this.intent != null ) {
						this.activity_context.startActivity( this.intent );
					}
				}
				else if( result.get( Constants.STATUS ).equals( Constants.FAILURE ) ) {
					Log.i(this.identity, "Fail message rec");
					
					/*TODO: pull failure message from payload */
					
					/* Kill backdoor thread if it has started */
					try {
						Constants.backdoor.die( );
					} catch( Exception e ) { }
				}
			}
			catch ( Exception e ) { 
				e.printStackTrace(); 
			}			
		}
		
		public void reset( ) {
			this.dialog = null;
			this.p_dialog = null;
			this.to_send = null;
			this.intent = null;
			this.conn = null;
			this.handler = null;
		}
		
		public void run( ) {
			while( !this.isInterrupted( ) ) {
				try {
					this.lock.lock( );
					this.cv.await( );
					if( this.isInterrupted( ) ) {
						this.lock.unlock( );
						break;
					}
					this.conn = new Socket( );
					this.conn.connect( new InetSocketAddress( Constants.server_url, Constants.server_lobby_port ) );
					this.dos = new DataOutputStream( this.conn.getOutputStream( ) );
					this.dis = new DataInputStream( this.conn.getInputStream( ) );
					
					byte[ ] payload = this.to_send.toString( ).getBytes( );
					String NPL = ( "NPL:" + String.valueOf( ( int )payload.length ) + '\0' );
					this.dos.write( NPL.getBytes( ) );
					this.dos.flush( );
					Log.i( this.identity, "sending: " + new String( payload ) );
					this.dos.write( payload );
					this.dos.flush( );
					
					int next_byte;
					String raw_payload = "";
					while( ( next_byte = this.dis.read( ) ) != '\0' ) {
						raw_payload += ( char )next_byte;
					}
					int npl = Integer.parseInt( raw_payload.split( ":" )[ 1 ] );
					byte[ ] next_payload = new byte[ npl ];
					this.dis.read( next_payload, 0, next_payload.length );
					this.handle_result( new JSONObject( new String( next_payload ) ) );
					
				} catch( InterruptedException ie ) {
					Log.i( this.identity, "Interrupted!" );
				} catch( Exception e ) {
					e.printStackTrace( );
				} finally {
					try {
						if( this.conn != null )
							this.conn.close( );
						this.reset( );
						this.lock.unlock( );
					} catch( Exception e ) { e.printStackTrace( ); }
				}
			}
		}
	}

	/* This thread manages backdoor network activity */
	public static class Backdoor extends Thread {
		
		/* Constant(s) */
		private static final String identity = "[-Backdoor_Handler-]";
		private static final Object LOCK = Backdoor.class;
		private static final int SO_TIMEOUT = 500;
		
		/* Member(s) */
		private static boolean stopped = false;
		private static InetSocketAddress shared_addr, heartbeat_addr;
		private Context activity_context;
		private Heartbeat heartbeat_thd;
		private Receiver receiver_thd;
		private DatagramSocket soc;
		
		/* Helper class(es) */
		private class Heartbeat extends Thread {
			
			/* Constant(s) */
			private final String identity = "[-Backoor_Heartbeat-]";
			
			/* Member(s) */
			private Context activity_context;
			private SharedPreferences prefs;
			private DatagramSocket heartbeat_soc;
			private DatagramPacket packet;
			private byte[ ] buffer;
			
			public Heartbeat( Context activity_context, DatagramSocket socket ) {
				super( );
				this.activity_context = activity_context;
				this.heartbeat_soc = socket;
			}
			
			public boolean initialize( ) {
				try {
					this.prefs = this.activity_context.getSharedPreferences( Constants.PREFS_FILENAME, Context.MODE_PRIVATE );
				} catch( Exception e ) {
					e.printStackTrace( );
					return false;
				}
				return true;
			}
					
			public void send_heartbeat( ) {
				try {
					
					JSONObject net_info = new JSONObject( );
					net_info.put( Constants.L_USERNAME, prefs.getString( Constants.L_USERNAME, "" ) );
					
					WifiManager wifiManager = ( WifiManager )this.activity_context.getSystemService( Context.WIFI_SERVICE );  
					WifiInfo wifiInfo = wifiManager.getConnectionInfo( );  
					int ipAddress = wifiInfo.getIpAddress( );  
					String ip = intToIp( ipAddress );  
					
					net_info.put( Constants.PRI_IP, ip );
					net_info.put( Constants.PRI_PORT, this.heartbeat_soc.getLocalPort( ) );
					 
					String raw_payload = net_info.toString( );
					this.buffer = raw_payload.getBytes( );
					this.packet = new DatagramPacket( this.buffer, this.buffer.length, Backdoor.heartbeat_addr );
					this.heartbeat_soc.send( this.packet );
					Log.i( this.identity, "sent hearbeat..." );
					
				} catch( Exception e ) {
					e.printStackTrace( );
				} 
			}
			
			public String intToIp( int i ) { return ( ( i & 0xFF ) + "." + ( ( i >> 8 ) & 0xFF ) + "." + ( ( i >> 16 ) & 0xFF ) + "." + ( ( i >> 24 ) & 0xFF ) ); }
			
			public void run( ) {
				try {
					Log.i( this.identity, "up and running..." );
					while( !Backdoor.is_stopped( ) ) {
						this.send_heartbeat( );
						Thread.sleep( Constants.HEARTBEAT_INTERVAL );	
					}
				} catch( InterruptedException ie ) {
					
				} catch( Exception e ) {
					e.printStackTrace( );
				} finally {
					Log.i( this.identity, "terminating..." );
					try {
						this.heartbeat_soc.close( );
					} catch( Exception e ) {
						e.printStackTrace( );
					}
				}
			}
		}
		private class Receiver extends Thread {			
			/* Constant(s) */
			private final String identity = "[-Backdoor_Receiver-]";
			
			/* Member(s) */
			private Context activity_context;
			private DatagramSocket backdoor_soc;
			private DatagramPacket packet;
			private byte[ ] buffer;
			
			/* Constructor(s) */
			public Receiver( Context context, DatagramSocket socket ) {
				super( );
				this.activity_context = context;
				this.backdoor_soc = socket;
			}
			
			/* Method(s) */
			public boolean initialize( ) {
				try {
					this.buffer = new byte[ Constants.BACKDOOR_BUFLEN ];
					this.packet = new DatagramPacket( this.buffer, this.buffer.length );
				} catch( Exception e ) {
					e.printStackTrace( );
					return false;
				}
				return true;
			}
			
			public void handle_conn( ) {
				try {
					String payload = new String( this.packet.getData( ) ).trim( );
					JSONObject temp = new JSONObject( payload );
					String m_intent = temp.getString( Constants.M_TAG );
					if( m_intent.equals( Constants.LOBBY_UPDATE ) ) {
						Intent intent = new Intent( Constants.BROADCAST_LOBBY_UPDATE );
						intent.putExtra( Constants.PAYLOAD, payload );
						this.activity_context.sendStickyBroadcast( intent );
					}
					else if( m_intent.equals( Constants.LOBBY_GS ) ) {
						Intent intent = new Intent( Constants.BROADCAST_LOBBY_GS );
						intent.putExtra( Constants.PAYLOAD, Long.parseLong( temp.getString( Constants.PAYLOAD ) ) );
						this.activity_context.sendBroadcast( intent );
					}
					
				} catch( Exception e ) {
					e.printStackTrace( );
				}
			}
			
			public void run( ) {
				try {
					Log.i( this.identity, "up and running..." );
					while( !Backdoor.is_stopped( ) ) {
						try {
							this.backdoor_soc.receive( this.packet );
							this.handle_conn( );
						} catch( InterruptedIOException iioe ) {
							continue;
						} catch( Exception e ) {
							e.printStackTrace( );
						}
					}
				} catch( Exception e ) {
					e.printStackTrace( );
				} finally {
					try {
						this.backdoor_soc.close( );
						Log.i( this.identity, "terminating..." );
					} catch( Exception e ) { e.printStackTrace( ); }
				}
			}
		}
		
		/* Constructor(s) */
		public Backdoor( Context activity_context ) {
			super( );
			this.activity_context = activity_context;
		}
	
		/* Method(s) */
		public boolean initialize( ) {
			try {
				Backdoor.shared_addr = new InetSocketAddress( Constants.backdoor_port );
				Backdoor.heartbeat_addr = new InetSocketAddress( Constants.server_url, Constants.server_heartbeat_port );
				
				this.soc = new DatagramSocket( null );
				this.soc.setReuseAddress( true );
				this.soc.setSoTimeout( Backdoor.SO_TIMEOUT );
				this.soc.bind( Backdoor.shared_addr );
			} catch( Exception e ) {
				e.printStackTrace( );
				return false;
			}

			this.receiver_thd = new Receiver( this.activity_context, soc );
			if( !this.receiver_thd.initialize( ) )
				return false;
			this.heartbeat_thd = new Heartbeat( this.activity_context, soc );
			if( !this.heartbeat_thd.initialize( ) )
				return false;
			return true;
		}
		
		public static boolean is_stopped( ) {
			synchronized( Backdoor.LOCK ) {
				return Backdoor.stopped;
			}
		}
		
		public void run( ) {
			Log.i( Backdoor.identity, "managing the backdoor" );
			this.receiver_thd.start( );
			this.heartbeat_thd.start( );
			while( !Backdoor.is_stopped( ) ) {
				try {
					Thread.sleep( 250 );
				} catch( Exception e ) {
					e.printStackTrace( );
				}
			}
			Log.i( Backdoor.identity, "terminating..." );
		}
		
		public void die( ) {
			synchronized( Backdoor.LOCK ) {
				Backdoor.stopped = true;
			}
			try {
				this.heartbeat_thd.interrupt( );
			} catch( Exception e ) {
				e.printStackTrace( );
			}
		}
		
		public void reset( ) {
			synchronized( Backdoor.LOCK ) {
				if( Backdoor.stopped == true )
					Backdoor.stopped = false;
			}
		}
	}
}
