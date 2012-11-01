package com.walloff.android;

public class Player {
	
	/* CLASS MEMBER(S) */
	private String ip_addr;
	
	/* CONSTRUCTOR(S) */
	public Player( ) {
		super( );
	}
	public Player( String ip_addr ) {
		super( );
		this.ip_addr = ip_addr;
	}
	
	/* GETTER(S) */
	public String get_IP( ) { return this.ip_addr; }
	
	/* MODIFIER(S) */
	public void set_IP( String ip_addr ) { this.ip_addr = ip_addr; }
}
