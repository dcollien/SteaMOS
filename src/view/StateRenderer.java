package view;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;

import state.ConnectionNet;
import state.SimulationState;

public class StateRenderer {

	protected SimulationState state;
	
	protected int squareSize = 12;
	protected int gridSpacing = 0;
	
	public static final int[] cellColours = new int[] {
		0xFF000000,
		0xFFffffff,
		0xFF000000, // v_narrow
		0xFF000000, // h_narrow
		0xFFc0c0c0,
		0xFF808080,
		0xFFff0000,
		0xFF00ff00,
		0xFF0000ff,
		0xFFffff00,
		0xFF00ffff,
	};
	
	public static final int[] pressureChannelColours = new int[] {
		0xFFffffff,
		0xFFffa0a0,
		0xFFa0a0ff,
		0xFFa0ffa0,
	};
	
	public StateRenderer( SimulationState state ) {
		this.state = state;
	}
	
	public int getSquareSize( ) {
		return squareSize;
	}
	
	public int getSpacing( ) {
		return gridSpacing;
	}

	public void render( Graphics g ) {
		for ( int y = 0; y < state.getHeight( ); y++ ) {
			for ( int x = 0; x < state.getWidth( ); x++ ) {
				int cellARGB = 0;
				int pressureARGB = 0xFFffffff;
				int cellValue = state.getCell( x, y );
				int pressureValue = state.getPressure( x, y );
				if ( cellValue > 0 && cellValue < cellColours.length ) {
					cellARGB = cellColours[ state.getCell( x, y ) ];
				}
				if ( pressureValue > 0 && pressureValue < pressureChannelColours.length ) {
					pressureARGB = pressureChannelColours[ state.getPressure( x, y ) ];
				}
				g.setColor( new Color( cellARGB ) );
				int drawX = x * squareSize + x * gridSpacing;
				int drawY = y * squareSize + y * gridSpacing;
				g.fillRect( drawX, drawY, squareSize, squareSize );

				if ( state.hasInputAt( x, y ) ) {
					g.setColor( Color.white );
					g.drawRect( drawX, drawY, squareSize-1, squareSize-1 );
				}

				g.setColor( new Color( pressureARGB ) );
				
				if ( cellValue == SimulationState.CHANNEL ) {
					g.fillRect( drawX, drawY, squareSize, squareSize );
					
					ConnectionNet net = state.getNetConnection( new Point( x, y ) );
					if ( net != null ) {
						int dotSize = 5;
						int dotX = drawX + squareSize/2 - dotSize/2;
						int dotY = drawY + squareSize/2 - dotSize/2;
						
						g.setColor( Color.black );
						g.fillOval( dotX-1, dotY-1, dotSize+2, dotSize+2 );
						g.setColor( new Color( net.getID() ) );
						g.fillOval( dotX, dotY, dotSize, dotSize );
					}
					
				} else {
					if ( cellValue == SimulationState.H_NARROW ) {
						g.fillRect( drawX, drawY+squareSize/4, squareSize, squareSize/2 );
					} else if ( cellValue == SimulationState.V_NARROW ) {
						g.fillRect( drawX+squareSize/4, drawY, squareSize/2, squareSize );
					} else if ( cellValue == SimulationState.THRU_SHUTTLE && pressureValue != SimulationState.PRESSURE_NONE ) {
						int xLeft  = x-1;
						int xRight = x+1;
						int yUp    = y-1;
						int yDown  = y+1;
						
						if ( xLeft >= 0 && state.getPressure( xLeft, y ) == pressureValue  ) {
							g.fillRect( drawX, drawY+squareSize/4, squareSize/2, squareSize/2 );
						}
						if ( xRight < state.getWidth() && state.getPressure( xRight, y ) == pressureValue  ) {
							g.fillRect( drawX+squareSize/2, drawY+squareSize/4, squareSize/2, squareSize/2 );
						}
						if ( yUp >= 0 && state.getPressure( x, yUp ) == pressureValue  ) {
							g.fillRect( drawX+squareSize/4, drawY, squareSize/2, squareSize/2 );
						}
						if ( yDown < state.getHeight() && state.getPressure( x, yDown ) == pressureValue  ) {
							g.fillRect( drawX+squareSize/4, drawY+squareSize/2, squareSize/2, squareSize/2 );
						}
					}
				}
			}
		}
	}

}
