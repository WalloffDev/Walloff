package com.walloff.android;

public class Player {
	
	/* CLASS MEMBER(S) */
	private String uname;
	private String pub_ip, priv_ip;
	private int pub_port, priv_port;
	private int gc_pub_port, gc_priv_port;
	
	/* CONSTRUCTOR(S) */
	public Player( ) {
		super( );
	}
	public Player( String uname, String pub_ip, int pub_port, String priv_ip, int priv_port ) {
		super( );
		this.uname = uname;
		this.pub_ip = pub_ip;
		this.pub_port = pub_port;
		this.priv_ip = priv_ip;
		this.priv_port = priv_port;
	}
	
	/* GETTER(S) */
	public String get_Uname( ) { return this.uname; }
	public String get_PubIP( ) { return this.pub_ip; }
	public String get_PrivIP( ) { return this.priv_ip; }
	public int get_PubPort( ) { return this.pub_port; }
	public int get_PrivPort( ) { return this.priv_port; }
	public int get_GC_PubPort( ) { return this.gc_pub_port; }
	public int get_GC_PrivPort( ) { return this.gc_priv_port; } 
	
	/* MODIFIER(S) */
	public void set_Uname( String uname ) { this.uname = uname; }
	public void set_PubIP( String pub_ip ) { this.pub_ip = pub_ip; }
	public void set_PrivIP( String priv_ip ) { this.priv_ip = priv_ip; }
	public void set_PubPort( int pub_port ) { this.pub_port = pub_port; }
	public void set_PrivPort( int priv_port ) { this.priv_port = priv_port; }
	public void set_GC_PubPort( int gc_pub_port ) { this.gc_pub_port = gc_pub_port; }
	public void set_GC_PrivPort( int gc_priv_port ) { this.gc_priv_port = gc_priv_port; }

	/* HELPER(S) */
	public String pretty_print( ) {
		return "UNAME: " + this.uname + " PUI: " + this.pub_ip + "  PUP: " + String.valueOf( this.pub_port ) + "  PRI: " 
						+ this.priv_ip + "  PRP: " + String.valueOf( this.priv_port ) + "  ";
	}
}
