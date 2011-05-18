package view;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

import simulation.SimulationThread;
import state.ConnectionNet;
import state.SimulationState;

public class Viewer extends Canvas implements MouseListener,
MouseMotionListener {
	private static final long serialVersionUID = 1L;

	protected StateRenderer stateRenderer;
	protected SimulationState state;
	protected SimulationThread simulation;
	
	protected String errorMessage = "";

	private boolean inputEnabled;
	private int width;
	private int height;
	
	private Image offscreenBuffer;

	private Point errorLocation = null;

	private ConnectionNet hoverNet = null;

	private Point hoverPoint;
	
	public Viewer( String filename ) {
		init( filename );
	}
	
	public void init( String filename ) {
		inputEnabled = false;
		
		
		this.setBackground( Color.BLACK );
		File imageFile = new File( filename );
		try {
			this.state = new SimulationState( imageFile );
			this.state.printDebug( );
		} catch ( IOException e ) {
			System.err.println( "Oh crap: " );
			e.printStackTrace();
		} 
		
		this.stateRenderer = new StateRenderer( state );
		
		simulation = new SimulationThread( this, this.state );
		simulation.start( );
	}
	
	public void paint( Graphics g ) {

		Dimension size = this.getSize( );
		
		if ( size.width != this.width || size.height != this.height ) {
			this.width = size.width;
			this.height = size.height;
			offscreenBuffer = null;
		}
		
		if ( offscreenBuffer == null ) {
			offscreenBuffer = this.createImage( width, height );
		}
		
		Graphics bufferG = offscreenBuffer.getGraphics( );
		
		bufferG.clearRect( 0, 0, width, height );
		stateRenderer.render( bufferG );
		
		bufferG.setColor( Color.red );
		bufferG.drawString( errorMessage, 10, 10 );
		
		if ( errorLocation != null ) {
			int squareSize = stateRenderer.getSquareSize( );
			int spacing    = stateRenderer.getSpacing( );
			int screenX = errorLocation.x * squareSize + errorLocation.x * spacing;
			int screenY = errorLocation.y * squareSize + errorLocation.y * spacing;

			bufferG.setColor( Color.black );
			bufferG.drawLine( screenX+1, screenY+1, screenX+squareSize+1, screenY+squareSize+1 );
			bufferG.drawLine( screenX+squareSize+1, screenY+1, screenX+1, screenY+squareSize+1 );
			bufferG.drawOval( screenX+1, screenY+1, squareSize+1, squareSize+1 );
			
			bufferG.setColor( Color.red );			
			bufferG.drawLine( screenX, screenY, screenX+squareSize, screenY+squareSize );
			bufferG.drawLine( screenX+squareSize, screenY, screenX, screenY+squareSize );
			bufferG.drawOval( screenX, screenY, squareSize, squareSize );
			
		}
		
		// draw nets
		if ( hoverNet != null ) {
			if ( hoverPoint != null && hoverNet.containsPoint( hoverPoint ) ) {
				int pressure = state.getPressure( hoverPoint );
				bufferG.setColor( new Color( StateRenderer.pressureChannelColours[pressure] ) );
				for ( Point gridPoint : hoverNet.getPoints( ) ) {
					Point screenFrom = gridToScreen( hoverPoint );
					Point screenTo   = gridToScreen( gridPoint );
					
					bufferG.drawLine( screenFrom.x, screenFrom.y, screenTo.x, screenTo.y );
				}
			}
		}
		
		g.drawImage( offscreenBuffer, 0, 0, this );
	}
	
	private Point gridToScreen( Point gridPoint ) {
		int squareSize = stateRenderer.getSquareSize( );
		int spacing    = stateRenderer.getSpacing( );
		int screenX = gridPoint.x * squareSize + gridPoint.x * spacing;
		int screenY = gridPoint.y * squareSize + gridPoint.y * spacing;
		
		return new Point( screenX + squareSize/2, screenY + squareSize/2 );
	}

	public void update( Graphics g ) {
		paint( g );
	}


	public void showError( String string, Point location ) {
		showError( string );
		errorLocation = location;
	}
	
	public void showError( String error ) {
		errorMessage = error;
		errorLocation = null;
	}
	
	public void clearError( ) {
		errorMessage = "";
		errorLocation = null;
	}

	public void addInput( ) {
		this.addMouseListener( this );
		this.addMouseMotionListener( this );
	}
	
	public void removeInput( ) {
		this.removeMouseListener( this );
		this.removeMouseMotionListener( this );
	}
	
	@Override
	public void mouseDragged(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseMoved( MouseEvent evt ) {
		int x = evt.getX( );
		int y = evt.getY( );
		
		Point grid = screenToGrid( new Point( x, y ) );
		
		this.hoverNet   = state.getNetConnection( grid );

		this.hoverPoint = grid;
	}

	@Override
	public void mouseClicked( MouseEvent evt ) {

	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	public Point screenToGrid( Point screen ) {
		int squareSize = stateRenderer.getSquareSize( );
		int spacing    = stateRenderer.getSpacing( );
		
		int x = screen.x;
		int y = screen.y;
		
		int gridX = x / (squareSize + spacing);
		int gridY = y / (squareSize + spacing);
		
		return new Point( gridX, gridY );
	}
	
	@Override
	public void mousePressed( MouseEvent evt ) {
		int x = evt.getX( );
		int y = evt.getY( );
		
		Point grid = screenToGrid( new Point( x, y ) );
		
		
		if ( state.hasInputAt( grid.x, grid.y ) ) {
			if ( state.getCell( grid.x, grid.y ) == SimulationState.SINK ) {
				state.setCell( grid.x, grid.y, SimulationState.SOURCE );
			} else {
				state.setCell( grid.x, grid.y, SimulationState.SINK );
			}
		} else {
			System.out.println( "No input at: " );
			System.out.println( new Point( grid.x, grid.y ) );
		}
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void enableInput() {
		if ( !this.inputEnabled ) {
			addInput( );
		}
		this.inputEnabled = true;
	}
	
	public void disableInput( ) {
		if ( this.inputEnabled ) {
			removeInput( );
		}
		this.inputEnabled = false;
	}
	
	
	
	
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Frame viewerFrame = new Frame( "SteamOS Sim" );
		Viewer viewer = new Viewer( args[0] );
		
		viewerFrame.add( viewer );
		viewerFrame.setSize( 640, 480 );
		
		viewerFrame.addWindowListener( new WindowAdapter() {
			public void windowClosing( WindowEvent we ) {
				System.exit( 0 );
			}
		} );
		
		viewerFrame.setVisible( true );
	}


}
