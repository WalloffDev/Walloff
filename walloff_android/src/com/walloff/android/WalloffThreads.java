package com.walloff.android;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
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
import android.util.Log;

public class WalloffThreads {
	
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
		
		public void handle_result( JSONObject result ) {
			Log.i( this.identity, "recvd: " + result.toString( ) );
			
			try 
			{
				//cancel regardless
				if ( this.p_dialog != null )
					this.p_dialog.cancel();
				
				if ( result.get( Constants.STATUS ).equals( Constants.SUCCESS ) )
				{					
					if ( this.dialog != null )
						this.dialog.cancel();
					
					Log.i( this.identity, "SUCCESS" );
					if ( this.intent != null )
					{
						this.activity_context.startActivity( this.intent );
					}
				}
				else if ( result.get( Constants.STATUS ).equals( Constants.FAIL ) )
				{
					Log.i(this.identity, "Fail message rec");
				}
			}
			catch ( Exception e ) { e.printStackTrace(); }
			finally
			{
				
			}
			
		}
		
		public void reset( ) {
			this.dialog = null;
			this.p_dialog = null;
			this.to_send = null;
			this.intent = null;
			this.conn = null;
		}
		
		public void run( ) {
			while( !this.isInterrupted( ) ) {
				try {
					Log.i(this.identity, "STARTING TO RUN");
					this.lock.lock( );
					this.cv.await( );
					if( this.isInterrupted( ) ) {
						this.lock.unlock( );
						break;
					}
					this.conn = new Socket( );
					this.conn.connect( new InetSocketAddress( Constants.server_url, Constants.server_port ) );
					Log.i(this.identity, "Socket con");
					this.dos = new DataOutputStream( this.conn.getOutputStream( ) );
					this.dis = new DataInputStream( this.conn.getInputStream( ) );
					
					String payload = this.to_send.toString( );
					String NPL = ( "NPL:" + String.valueOf( payload.getBytes().length ) + '\0' );
					Log.i(this.identity, payload);
					Log.i(this.identity, NPL);
					this.dos.write( NPL.getBytes( ) );
					Log.i("PAYLOAD TO WRITE LENGTH", String.valueOf( payload.getBytes().length ));
					this.dos.flush( );
					this.dos.write( payload.getBytes() );
					this.dos.flush( );
					int next_byte;
					String raw_payload = "";
					Log.i(this.identity, "getting the payload length");
					while( ( next_byte = this.dis.read( ) ) != '\0' ) {
						raw_payload += (char)next_byte;
					}
					Log.i(this.identity, String.valueOf( raw_payload.getBytes().length ) );
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
						this.reset( );
						if( this.conn != null )
							this.conn.close( );
						this.lock.unlock( );
					} catch( Exception e ) { e.printStackTrace( ); }
				}
			}
		}
	}

	/* This task is used to keep a tcp hole punched to our server */
	public static class Heartbeat extends Thread {
		
		private String identity = "Heartbeat";
		private Context activity_context = null;
		private Lock lock = null;
		private Condition cv = null;
		private Socket socket = null;
		private SharedPreferences prefs = null;
		private DataOutputStream dos = null;
		private boolean is_ready = false;
		
		public Heartbeat( Context activity_context ) {
			super( );
			this.activity_context = activity_context;
			this.prefs = this.activity_context.getSharedPreferences( Constants.PREFS_FILENAME, Context.MODE_PRIVATE );
		}
		
		public void set_lock( Lock lock, Condition cv ) {
			this.lock = lock;
			this.cv = cv;
		}
		
		public boolean is_ready( ) {
			return this.is_ready;
		}
		
		public void send_heartbeat( ) {
			try {
				this.socket = new Socket( );
				this.socket.setReuseAddress( true );
				this.socket.bind( new InetSocketAddress( Constants.backdoor_port ) );
				this.socket.connect( new InetSocketAddress( Constants.server_url, Constants.server_heartbeat_port ) );
				this.dos = new DataOutputStream( this.socket.getOutputStream( ) );
				
				JSONObject net_info = new JSONObject( );
				net_info.put( Constants.L_USERNAME, prefs.getString( Constants.L_USERNAME, "" ) );
				net_info.put( Constants.PRI_IP, this.socket.getLocalAddress( ).getHostAddress( ) );
				net_info.put( Constants.PRI_PORT, this.socket.getLocalPort( ) );
				 
				String raw_payload = net_info.toString( );
				
				this.dos.write( ( "NPL:" + String.valueOf( raw_payload.getBytes().length ) + '\0' ).getBytes( ) );
				this.dos.flush( );
				this.dos.write( raw_payload.getBytes( ) );
				this.dos.flush( );
			} catch( Exception e ) {
				e.printStackTrace( );
			} finally {
				try {
					this.socket.close( );
				} catch( Exception e ) { e.printStackTrace( ); }
			}
		}
		
		public void run( ) {
			try {
				this.is_ready = true;
				while( !this.isInterrupted( ) ) {
					this.lock.lock( );
					this.cv.await( );
					if( this.isInterrupted( ) ) {
						this.lock.unlock( );
						break;
					}
					this.send_heartbeat( );
					this.cv.signal( );
					this.lock.unlock( );
				}
			}catch( InterruptedException ie ) {
				if( this.socket.isConnected( ) ) {
					try{ this.socket.close( ); } catch( Exception e ) { }
				}
				Log.i(this.identity, "Interrupted!");
			}catch( Exception e ) {
				e.printStackTrace( );
			}
		}
	}

	public static class Backdoor extends Thread {
		
		private String identity = "Backdoor";
		private Context activity_context = null;
		private Heartbeat heartbeat_thd = null;
		private Lock lock = null;
		private Condition cv = null;
		private ServerSocket doorbell = null;
		private Socket conn = null;
		private DatagramSocket ds = null;
		
		public Backdoor( Context activity_context ) {
			super( );
			this.activity_context = activity_context;
			this.lock = new ReentrantLock( );
			this.cv = this.lock.newCondition( );
		}
	
		public void setup( ) {
			this.heartbeat_thd = new Heartbeat( this.activity_context );
			this.heartbeat_thd.set_lock( this.lock, this.cv );
			this.heartbeat_thd.start( );
		}
		
		public void handle_conn( ) {
			// received push message from Walloff Server
			Log.i( this.identity, "established backdoor connection. FUCK YEAH" );
		}
		
		public void listen( ) {
			try {
				int loops = Constants.HEARTBEAT_INTERVAL / 500;
				this.doorbell = new ServerSocket( );
				this.doorbell.bind( new InetSocketAddress( Constants.backdoor_port ) );
				this.doorbell.setReuseAddress( true );
				this.doorbell.setSoTimeout( 500 );
				
//				this.ds = new DatagramSocket( );
//				ds.setReuseAddress( true );
//				ds.setSoTimeout( 500 );
//				DatagramPacket dp = new DatagramPacket( new byte[ 17 ], 17, InetAddress.getByName(Constants.server_url), Constants.backdoor_port);
				
				while( !this.isInterrupted( ) && loops != 0 ) {
					try {
//						ds.receive(dp);
//						Log.i( this.identity, "received something fucker" );
						this.conn = this.doorbell.accept( );
						this.handle_conn( );
					} catch( InterruptedIOException iioe ) { // doorbell timeout small to detect interrupts faster
						loops--;
						continue;
					}
				}
			} catch( Exception e ) { 
				e.printStackTrace( ); 
			} finally {
				try { 
					this.doorbell.close( );
					
				} catch( Exception e ) {
					e.printStackTrace( );
				}
			}
		}
		
		public void run( ) {
			try {
				this.setup( );
				while( !this.heartbeat_thd.is_ready( ) ) { }
				while( !this.isInterrupted( ) ) {
					this.lock.lock( );
					this.cv.signal( );
					this.cv.await( );
					this.lock.unlock( );
					this.listen( );
				}
				this.heartbeat_thd.interrupt( );
				Log.i( this.identity, "Interrupted!" );
				this.doorbell.close( );
			} catch( InterruptedException ie ) {
				Log.i( this.identity, "Interrupted!" );
				this.heartbeat_thd.interrupt( );
			} catch( Exception e ) {
				e.printStackTrace( );
			}
		}
	}

}
