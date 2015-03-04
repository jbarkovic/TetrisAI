package interfaces;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

import tetris.engine.mechanics.Engine;

public class EngineInterface {
	private Engine engine;
	private int controlMovementDelay = 30;
	private ControlMovements sequenceBuffer = new ControlMovements ();
	private Stepper sequenceStepper;
	private Object accessLock = new Object();
	
	public EngineInterface (Engine engine) {
		this.engine = engine;
		sequenceStepper = new Stepper (controlMovementDelay);
		sequenceStepper.run();
	}
	public synchronized int setDelay (int newDelay) {
		int oldDelay = controlMovementDelay;
		controlMovementDelay = newDelay;
		sequenceStepper.setDelay(controlMovementDelay);
		return oldDelay;
	}
	private void stepSequence () {
	//	System.out.println ("in step");
		if (sequenceBuffer.hasNext()) {
			//System.out.println ("STEPPING");
			executePatterns (sequenceBuffer.next());
		}
	}
	public void executeSequence (ControlMovements sequence) {
		//sequenceBuffer.addAll(sequence);
		sequenceBuffer = sequence;
		/*if (stepperThread != null && stepperThread.isAlive()) {
			try {
				System.out.println("Joining");
				stepperThread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}*/

	}
	public int getDelay () {
		return this.controlMovementDelay;
	}
	public synchronized ai.state.SHAPETYPE getCurrentShape () {
		return convertShapetypes (this.engine.getCurrentShape());
	}
	public synchronized ai.state.SHAPETYPE getNextShape () {
		return convertShapetypes (this.engine.getNextShape());
	}
	public synchronized ai.state.SHAPETYPE getSwapShape () {
		return convertShapetypes (this.engine.getSwapShape());
	}
	public synchronized boolean dropShape () {
		return this.engine.dropShape ();
	}
	public synchronized boolean shiftLeft () {
		return this.engine.shiftLeft ();
	}
	public synchronized boolean shiftRight () {
		return this.engine.shiftRight ();
	}
	public synchronized int [][] getGameBoard () {
		return this.engine.getGameBoard();
	}
	public synchronized int [][] getCoordsOfCurrentShape () {
		return this.engine.getCoordsOfCurrentShape();
	}
	public synchronized boolean isGameLost () {
		return this.engine.isGameLost();
	}
	public synchronized boolean swapShapes () {
		return this.engine.swapShapes();
	}
	public synchronized boolean isPaused () {
		return this.engine.isPaused();
	}
	public synchronized boolean rotate () {
		return this.engine.rotateClockwise ();
	}
	public synchronized void plummit () {
		this.engine.plummit ();
		if (engine.isPaused()) {
		//	engine.dropShape();
		//	engine.dropShape();
		}
	}
	public synchronized boolean wasThereANewShape () {
		synchronized (engine) {
			return this.engine.wasThereANewShape();
		}
	}
	public synchronized boolean rotateCounterClockwise () {
		return this.engine.rotateCounterClockwise();
	}
	public synchronized boolean forceShiftLeft () {
		return this.engine.forceShiftLeft ();
	}
	public synchronized boolean forceShiftRight () {
		return this.engine.forceShiftRight ();
	}
	public synchronized boolean forceRotate () {
		return this.engine.forceRotateCW ();
	}
	private synchronized ai.state.SHAPETYPE convertShapetypes (tetris.engine.shapes.SHAPETYPE engineType) {
		return ai.state.SHAPETYPE.intToShapeType(engineType.toInt());
	}
	private boolean executePatterns(int [] patt) {
		if (patt == null) return false;
		if (engine == null) {
			System.err.println ("ERROR: ControlMovements: EngineInterface was null");
			return false;
		}
		//synchronized (engine) {
			if (patt[0] > 0) {
				for (int rotate=0;rotate<patt[0];rotate++) {	
					forceRotate ();
				}
			} 
			else if (patt[0] < 0){
				int r = -patt[0];
				for (int rotate=0;rotate<r;rotate++) {	
					rotateCounterClockwise();
				}
			}
			if (patt[1] > 0) {
				for (int shift=0;shift<patt[1];shift++) {
					forceShiftRight();
				}
			} 
			else {
				for (int shift=0;shift>patt[1];shift--)
					forceShiftLeft();				
			}
			if (patt[2] < 0) {
				plummit();
				dropShape();
				return false;
			} else {
				for (int drop=0;drop<patt[2];drop++)
					dropShape();				
			}	
			return true;
		}
	private class Stepper implements Runnable {
		private int delay;
		private Timer timer;
		Stepper (int delay) {
			this.delay = delay;
		}
		public int setDelay (int delay) {
			int oldDelay = delay;
			if (timer != null) {
				this.delay = delay;
				timer.setDelay(delay);
			}
			return oldDelay;
		}
		//}
		@Override
		public void run() {
				//synchronized (accessLock) {
					//while (true) {
				ActionListener taskPerformer = new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
							stepSequence ();
					}
				};
				timer = new Timer(delay, taskPerformer);
				timer.start();
				/*
					while (this.sequence.hasNext() && executePatterns (sequence.next())) {
						//if (this.sequence.hasNext() && executePatterns (sequence.next())) {}
						System.out.println ("While");
						try {
							Thread.sleep(delay);
						} catch (InterruptedException e) {
							System.err.println ("ERROR Executing control movements...");
							e.printStackTrace();
						}
					}
				}*/
			//}		
		}
	}
}
