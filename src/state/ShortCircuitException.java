package state;

import java.awt.Point;

public class ShortCircuitException extends Exception {
	private static final long serialVersionUID = 1L;
	private int type1 = 0;
	private int type2 = 0;
	private int x;
	private int y;

	public ShortCircuitException( int x, int y, int type1, int type2 ) {
		this.type1 = type1;
		this.type2 = type2;
		this.x = x;
		this.y = y;
	}
	
	public String toString( ) {
		return "Short Circuit at ("
			+ Integer.toString( x ) + "," + Integer.toString( y ) 
			+ ") between pressures " + Integer.toString( type1 ) + " and " + Integer.toString( type2 );
	}

	public Point getLocation() {
		return new Point( x, y );
	}
}
