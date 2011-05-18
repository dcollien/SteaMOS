package state;

import java.awt.Point;

import java.util.ArrayList;
import java.util.Arrays;

public class ConnectionNet {
	protected ArrayList<Point> points;
	protected int id;
	
	public ConnectionNet( Point[] points, int id ) {
		this.id = id;
		this.points = new ArrayList<Point>( Arrays.asList( points ) );
	}
	
	public void addPoint( Point point ) {
		points.add( point );
	}
	
	public boolean containsPoint( Point candidatePoint ) {
		for ( Point point : points ) {
			if ( point.equals( candidatePoint ) ) {
				return true;
			}
		}
		
		return false;
	}

	public ArrayList<Point> getPoints() {
		return points;
	}
	
	public int getID( ) {
		return id;
	}
	
}
