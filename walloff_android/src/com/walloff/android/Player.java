package com.walloff.android;

public class Player {
	
	/* CLASS MEMBER(S) */
	private String pub_ip, priv_ip;
	private int pub_port, priv_port;
	
	/* CONSTRUCTOR(S) */
	public Player( ) {
		super( );
	}
	public Player( String pub_ip, int pub_port, String priv_ip, int priv_port ) {
		super( );
		this.pub_ip = pub_ip;
		this.pub_port = pub_port;
		this.priv_ip = priv_ip;
		this.priv_port = priv_port;
	}
	
	/* GETTER(S) */
	public String get_PubIP( ) { return this.pub_ip; }
	public String get_PrivIP( ) { return this.priv_ip; }
	public int get_PubPort( ) { return this.pub_port; }
	public int get_PrivPort( ) { return this.priv_port; }
	
	/* MODIFIER(S) */
	public void set_PubIP( String pub_ip ) { this.pub_ip = pub_ip; }
	public void set_PrivIP( String priv_ip ) { this.priv_ip = priv_ip; }
	public void set_PubPort( int pub_port ) { this.pub_port = pub_port; }
	public void set_PrivPort( int priv_port ) { this.priv_port = priv_port; }

	/* HELPER(S) */
	public String pretty_print( ) {
		return "PUI: " + this.pub_ip + "  PUP: " + String.valueOf( this.pub_port ) + "  PRI: " 
						+ this.priv_ip + "  PRP: " + String.valueOf( this.priv_port ) + "  ";
	}
}
