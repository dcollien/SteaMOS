package simulation;

import java.awt.Point;
import java.util.ArrayList;

import state.ShortCircuitException;
import state.ShuttleShift;
import state.SimulationState;
import view.Viewer;

public class SimulationThread extends Thread {
	private SimulationState state;
	private Viewer viewer;
	
	private int delay = 10;
	private long iterations = 0;
	private boolean running;
	
	public SimulationThread( Viewer viewer, SimulationState state ) {
		this.viewer = viewer;
		this.state  = state;
	}
	
	public void update( ) {
		viewer.clearError( );
		try {
			stepSimulation( );
		} catch ( ShortCircuitException e ) {
			viewer.showError( e.toString( ), e.getLocation( ) );
		}
		
		if ( viewer.getGraphics() != null ) {
			viewer.paint( viewer.getGraphics() );
		}
	}
	
	public long getNumIterations( ) {
		return iterations;
	}
	
	public void run() {
		int settleIterations = 3;
		
		running = true;
		try {
			
			// print truth table
			int inputVal = 0;
			int inputBits = state.getInputs( ).size( );
			int inputSize  = (int) Math.pow( 2, inputBits );
			
			while ( inputVal < inputSize ) {
				
				printTruthTableInputs( inputVal );
			
				for ( int i = 0; i < settleIterations; i++ ) {
					update( );
					iterations++;
					sleep( delay );
				}
			
				printTruthTableOutputs( );
			
				inputVal++;
			}
			
			// enable user input
			viewer.enableInput( );
			
			// iterate forever
			while ( running ) {
				update( );
				iterations++;
				sleep( delay );
			}
			
		} catch ( InterruptedException e ) {
		}
    }	
	
	public void halt( ) {
		running = false;
	}
	
	
	// simulator guts
	
	private void stepSimulation( ) throws ShortCircuitException {
		// set inputs to sources/sinks first.
		
		
		state.clearChannels( );
		
		ArrayList<ShuttleShift> shuttlePressurePoints = new ArrayList<ShuttleShift>( );
		state.fillPressure( SimulationState.SOURCE, SimulationState.PRESSURE_POS , shuttlePressurePoints );
		state.fillPressure( SimulationState.VENT  , SimulationState.PRESSURE_VENT, shuttlePressurePoints );
		state.fillPressure( SimulationState.SINK  , SimulationState.PRESSURE_NEG , shuttlePressurePoints );
		
		state.shiftShuttles( shuttlePressurePoints );
	}
	
	
	// debug
	
	public void printTruthTableInputs( int inputVal ) {
		int inputBits = state.getInputs( ).size( );
		int inputSize  = (int) Math.pow( 2, inputBits );
		
		if ( inputVal < inputSize ) {
			boolean[] bitValues = intToBinBoolean( inputVal, inputBits );
			
			for ( int i = 0; i < bitValues.length; i++ ) {
				if ( bitValues[i] ) {
					System.out.print( " 1 |" );
				} else {
					System.out.print( " 0 |" );
				}
			}
			
			System.out.print( "|" );
			setInputs( bitValues );
		}
	}
	
	public void printTruthTableOutputs( ) {
		boolean[] outputValues = getOutputs(  );
		for ( int i = 0; i < outputValues.length; i++ ) {
			if ( outputValues[i] ) {
				System.out.print( " 1 |" );
			} else {
				System.out.print( " 0 |" );
			}
		}
		
		System.out.println( );
	}
	
	private boolean[] intToBinBoolean( int inputVal, int size ) {
		String binaryString = Integer.toBinaryString( inputVal );
		int padding = size - binaryString.length( );
		
		boolean[] boolString = new boolean[ size ];
		
		for ( int i = 0; i < binaryString.length(); i++ ) {
			if ( binaryString.charAt( i ) == '1' ) {
				boolString[padding+i] = true;
			} else {
				boolString[padding+i] = false;
			}
		}
		
		return boolString;
	}
	
	public void setInputs( boolean[] inputValues  ) {
		ArrayList<Point> inputPoints = state.getInputs();
		
		int i = 0;
		for ( Point inputPoint : inputPoints ) {
			if ( inputValues[i] ) {
				state.setCell( inputPoint, SimulationState.SOURCE );
			} else {
				state.setCell( inputPoint, SimulationState.SINK );
			}
			i++;
		}
	}

	public boolean[] getOutputs( ) {
		
		ArrayList<Point> outputPoints = state.getOutputs();
		
		boolean[] outputValues = new boolean[outputPoints.size()];
		int i = 0;
		for ( Point outputPoint : outputPoints ) {
			if ( state.getPressure( outputPoint ) == SimulationState.PRESSURE_POS ) {
				outputValues[i] = true;
			} else {
				outputValues[i] = false;
			}
			i++;
		}
		
		return outputValues;
	}

	
}
