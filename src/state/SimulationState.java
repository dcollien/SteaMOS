package state;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import javax.imageio.ImageIO;


public class SimulationState {
	public static final int SOLID         = 0;
	public static final int CHANNEL       = 1;
	public static final int V_NARROW      = 2;
	public static final int H_NARROW      = 3;
	public static final int THRU_SHUTTLE  = 4;
	public static final int BLOCK_SHUTTLE = 5;
	public static final int SINK          = 6;
	public static final int SOURCE        = 7;
	public static final int VENT          = 8;
	public static final int INPUT         = 9;
	public static final int OUTPUT        = 10;
	
	public static final int PRESSURE_NONE = 0;
	public static final int PRESSURE_NEG  = 1;
	public static final int PRESSURE_VENT = 2;
	public static final int PRESSURE_POS  = 3;
	
	public static final int[] bitmapColours = new int[] {
		0xFF000000,
		0xFFffffff,
		0xFFc0c0c0,
		0xFF404040,
		0xFFff00ff,
		0xFF800080,
		0xFFff0000,
		0xFF00ff00,
		0xFF0000ff,
		0xFFffff00,
		0xFF00ffff,
	};
	

	
	protected int[][] state;
	protected int[][] pressure;
	
	protected int width, height;
	
	protected ArrayList<Point> inputs;
	protected ArrayList<Point> outputs;
	protected HashMap<Integer, ConnectionNet> nets = new HashMap<Integer, ConnectionNet>( );
	
	public enum Direction { NONE, LEFT, RIGHT, UP, DOWN };
	// Constructors
	
	public SimulationState( int width, int height ) {
		this.state = new int[width][height];
		this.pressure = new int[width][height];
		this.width = width;
		this.height = height;
		
		this.inputs  = new ArrayList<Point>( );
		this.outputs = new ArrayList<Point>( );
	}
	
	public SimulationState( URL imageURL ) throws IOException {
		this( ImageIO.read( imageURL ) );
	}
	
	public SimulationState( File imageFile ) throws IOException {
		this( ImageIO.read( imageFile ) );
	}
	
	public SimulationState( BufferedImage stateImage ) {
		HashMap<Integer, Integer> colourLookup = new HashMap<Integer, Integer>( );
		
		for ( int i = 0; i < bitmapColours.length; i++ ) {
			colourLookup.put( bitmapColours[i], i );
		}
		
		this.width  = stateImage.getWidth();
		this.height = stateImage.getHeight();
		this.state  = new int[width][height];
		this.pressure = new int[width][height];

		this.inputs  = new ArrayList<Point>( );
		this.outputs = new ArrayList<Point>( );
		
		for ( int y = 0; y < height; y++ ) {
			for ( int x = 0; x < width; x++ ) {
				int rgb = stateImage.getRGB( x, y );
				Point gridPoint = new Point( x, y );
				
				// look up the colour to see if it's classifiable
				if ( colourLookup.containsKey( rgb ) ) {
					// classified
					state[x][y] = colourLookup.get( rgb );
					
					// keep track of inputs and outputs
					if ( state[x][y] == INPUT ) {
						inputs.add( gridPoint );
					} else if ( state[x][y] == OUTPUT ) {
						outputs.add( gridPoint );
					}
				} else if ( nets.containsKey( rgb ) ) {
					// keep track of nets
					nets.get( rgb ).addPoint( gridPoint );
					state[x][y] = CHANNEL;
				} else {
					nets.put( rgb, new ConnectionNet( new Point[] { gridPoint }, rgb ) );
					state[x][y] = CHANNEL;
				}
				
				pressure[x][y] = PRESSURE_NONE;
			}
		}
	}
	
	
	// Public methods
	
	public ConnectionNet getNetConnection( Point gridPoint ) {
		for ( ConnectionNet net : nets.values() ) {
			if ( net.containsPoint( gridPoint ) ) {
				return net;
			}
		}
		
		return null;
	}
	
	public void setCell( int x, int y, int value ) {
		state[x][y] = value;
	}
	
	public void setCell( Point gridPoint, int value ) {
		setCell( gridPoint.x, gridPoint.y, value );
	}
	
	public int getCell( int x, int y ) {
		return state[x][y];
	}
	
	public int getCell( Point gridPoint ) {
		return getCell( gridPoint.x, gridPoint.y );
	}
	
	public int getPressure( int x, int y ) {
		return pressure[x][y];
	}
	
	public int getPressure( Point gridPoint ) {
		return getPressure( gridPoint.x, gridPoint.y );
	}
	
	private void setPressure( Point gridPoint, int pressureVal ) {
		pressure[gridPoint.x][gridPoint.y] = pressureVal;
	}
	
	public int getWidth( ) {
		return this.width;
	}
	
	public int getHeight( ) {
		return this.height;
	}
	
	public ArrayList<Point> getInputs( ) {
		return inputs;
	}
	
	public ArrayList<Point> getOutputs( ) {
		return outputs;
	}
	
	public void printDebug( ) {
		char[] outputChars = new char[] {
			'#',
			' ',
			'"',
			':',
			'~',
			'*',
			'-',
			'+',
			'0',
			'^',
			'v'
		};
			
		for ( int y = 0; y < height; y++ ) {
			for ( int x = 0; x < width; x++ ) {
				if ( state[x][y] < outputChars.length ) {
					System.out.print( outputChars[state[x][y]] );
				} else {
					System.out.print( "?" );
				}
			}
			System.out.println( );
		}
	}
	
	public void clearChannels( ) {
		for ( int y = 0; y < height; y++ ) {
			for ( int x = 0; x < width; x++ ) {
				pressure[x][y] = PRESSURE_NONE;
			}
		}
	}

	public ArrayList<Point> getAllOfType( int type ) {
		ArrayList<Point> collection = new ArrayList<Point> ( );
		for ( int y = 0; y < height; y++ ) {
			for ( int x = 0; x < width; x++ ) {
				if ( state[x][y] == type ) {
					collection.add( new Point( x, y ) );
				}
			}
		}		
		return collection;
	}
	
	public void fillPressure( int entryType, int pressureType, ArrayList<ShuttleShift> shuttlePressurePoints ) throws ShortCircuitException {
		ArrayList<Point> entries = getAllOfType( entryType );

		for ( Point startPoint : entries ) {
			fillChannels( startPoint, pressureType, Direction.NONE, shuttlePressurePoints );
		}
	}
	
	
	
	// Private methods
	
	private int entryForPressure( int channelType ) {
		if ( channelType == PRESSURE_NEG ) {
			return SINK;
		} else if ( channelType == PRESSURE_POS ) {
			return SOURCE;
		} else {
			return VENT;
		}
	}
	
	private Direction oppositeDirection( Direction direction ) {
		switch ( direction ) {
		case LEFT : return Direction.RIGHT;
		case RIGHT: return Direction.LEFT;
		case UP   : return Direction.DOWN;
		case DOWN : return Direction.UP;
		}
		
		return Direction.NONE;
	}
	
	private void fillChannels( Point currPoint, int fillType, Direction fromDirection, ArrayList<ShuttleShift> shuttlePressurePoints ) throws ShortCircuitException {
		int x = currPoint.x;
		int y = currPoint.y;
		
		if ( x >= 0 && y >= 0 && x < width && y < height ) {
			// stay within bounds
			
			if ( state[x][y] == BLOCK_SHUTTLE ) {
				// pressure pushing or pulling a shuttle
				Point previousCell = adjacentPoint( currPoint, oppositeDirection( fromDirection ) );
				if ( getCell( previousCell ) != THRU_SHUTTLE ) {
					// don't split shuttles
					ShuttleShift pressureShift = new ShuttleShift( );
					pressureShift.direction = fromDirection;
					pressureShift.position  = new Point( x, y );
					pressureShift.pressure  = fillType;
					shuttlePressurePoints.add( pressureShift );
				}
			} else if ( pressure[x][y] == fillType ) {
				// dead end
			} else if ( pressure[x][y] != PRESSURE_NONE ) {
				// came adjacent to a conflicting pressure, this is bad
				throw new ShortCircuitException( x, y, fillType, pressure[x][y] );
			} else if ( state[x][y] == CHANNEL 
					||  state[x][y] == OUTPUT
					||  state[x][y] == entryForPressure( fillType ) 
					||  state[x][y] == V_NARROW
					||  state[x][y] == H_NARROW
					||  state[x][y] == THRU_SHUTTLE ) {
				
				// pressurise an empty channel, or a pressure entry point
				pressure[x][y] = fillType;
				
				// spread to surrounding cells
				
				if ( state[x][y] != V_NARROW ) {
					fillChannels( new Point( x+1, y   ), fillType, Direction.RIGHT, shuttlePressurePoints );
					fillChannels( new Point( x-1, y   ), fillType, Direction.LEFT,  shuttlePressurePoints );
				}
				
				if ( state[x][y] != H_NARROW ) {
					fillChannels( new Point( x  , y+1 ), fillType, Direction.DOWN,  shuttlePressurePoints );
					fillChannels( new Point( x  , y-1 ), fillType, Direction.UP,    shuttlePressurePoints );
				}
				
				// spread to connected nets
				ConnectionNet net = getNetConnection( new Point( x, y ) );
				if ( net != null ) {
					for ( Point netPoint : net.getPoints( ) ) {
						fillChannels( netPoint, fillType, Direction.NONE,  shuttlePressurePoints );
					}
				}
			}
		}
	}

	public void shiftShuttles( ArrayList<ShuttleShift> shuttlePressurePoints ) {
		for ( ShuttleShift pressurePoint : shuttlePressurePoints ) {
			shiftShuttleCell( pressurePoint.position, pressurePoint.direction, pressurePoint.pressure );
		}
	}
	
	private Point adjacentPoint( Point gridSquare, Direction direction ) {
		int x = gridSquare.x;
		int y = gridSquare.y;
		
		switch ( direction ) {
		case LEFT : x--; break;
		case RIGHT: x++; break;
		case UP   : y--; break;
		case DOWN : y++; break;
		}
		
		if ( x >= 0 && x < width && y >= 0 && y < height ) {
			return new Point( x, y );
		} else {
			return null;
		}
	}
	
	private boolean isShuttle( Point gridPoint ) {
		int cellType = getCell( gridPoint );
		return ( cellType == BLOCK_SHUTTLE || cellType == THRU_SHUTTLE );
	}
	
	private boolean shiftShuttleCell( Point shuttlePoint, Direction direction, int pressure ) {
		boolean isShifted = false;
		
		if ( direction != Direction.NONE ) {
			// has a direction to move in
			
			// the next point in the direction the force is heading
			Point nextPoint = adjacentPoint( shuttlePoint, direction );
		
			boolean canShift = false;
			
			if ( nextPoint != null ) {
				// within bounds
				
				if ( getCell( nextPoint ) == CHANNEL 
						&& getPressure( nextPoint ) < pressure ) {
						// can shift into empty space with less pressure
						canShift = true;
				} else if ( isShuttle( nextPoint ) ) {
					// whether we can shift depends on whether the next shuttle cell can be shifted
					canShift = shiftShuttleCell( nextPoint, direction, pressure );
				}
				

				if ( canShift ) {
					int currValue = getCell( shuttlePoint );
					setCell( nextPoint, currValue );
					setCell( shuttlePoint, CHANNEL );
					// carry pressure with the shift
					setPressure( shuttlePoint, getPressure( nextPoint ) );
					isShifted = true;
				}
			}
		}
		
		return isShifted;
	}

	public boolean hasInputAt( int gridX, int gridY ) {
		for ( Point p : inputs ) {
			if ( p.x == gridX && p.y == gridY ) {
				return true;
			}
		}
		return false;
	}


	
}
