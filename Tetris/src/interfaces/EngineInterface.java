package interfaces;

import java.util.Timer;
import java.util.TimerTask;

import tetris.engine.mechanics.Engine;

public class EngineInterface {
	private Engine engine;
	private boolean cautious = true;
	private final int rows, cols;
	private int lastSpeed = 30;
	private int numShapeCount;
	private LineRateTracker lineTracker;
	private int userSetSpeed = 30;
	private ControlMovements sequenceBuffer = new ControlMovements ();
	private Object accessLock = new Object();
	private Timer sequenceStepper;
	
	
	public EngineInterface (int rows, int cols, int speed, CallBackMessenger cback) {
		this.rows = rows;
		this.cols = cols;
		engine = new Engine(rows,cols,speed,cback);
		sequenceStepper = new Timer (false);
		
		lineTracker = new LineRateTracker();
		
		sequenceStepper.schedule(new SequenceExecutor (), 2, speed);
	}
	public synchronized int setDelay (int speed) {
		if (speed > 0) {
			userSetSpeed = speed;
			return setDelayInternal (speed);
		} else {
			return lastSpeed;
		}
	}
	private synchronized int setDelayInternal (int speed) {
		int oldDelay = lastSpeed;
		if (speed > 0) {

			lastSpeed = speed;
			
			sequenceStepper.cancel();
			sequenceStepper = new Timer (false);
			
			sequenceStepper.scheduleAtFixedRate(new SequenceExecutor (), 2, speed);
			System.out.println("AI Speed: " + speed);
		}
		return oldDelay;
	}
	public Engine getEngine () {
		return engine;
	}
	private void stepSequence () {
		if (engine.isGameLost()) {
			sequenceStepper.cancel();
			
			System.out.println("Engine reports GameOver, printing last control sequence executed...");
			sequenceBuffer.dumpPattern();
			
		} else if (sequenceBuffer.hasNext()) {
			executePatterns (sequenceBuffer.next());
		} else {
			synchronized (accessLock) {
				accessLock.notifyAll();
			}	
		}
	}
	public void executeSequence (ControlMovements sequence) {
		synchronized (accessLock) {
			while (sequenceBuffer.hasNext()) {
				try {
					accessLock.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return;
				}
			}
			numShapeCount ++;
			
			lineTracker.recordShape(engine.getLinesCleared());

			if (numShapeCount %500 == 0) {
				System.out.println(String.format("Lines per second: %3.3f", lineTracker.getAvgTime()));
			}
			if (cautious) {
				if (lineTracker.getLineRate() > (cols / 4d)*1.1) {
					System.err.println("AI is slowing down to ensure correctness");
					// Somethings wrong cause we're loosing at this rate, could be time based, try slowing down
					int newRate = lastSpeed*2;
					if (newRate > userSetSpeed+30) {
						newRate = userSetSpeed+30;
						System.err.println("Error: AI has slowed to its min speed, something's fucky");
					}
					setDelayInternal(newRate);
				} else if (lineTracker.getLineRate() < (cols / 4d)*0.95){
					int newRate = lastSpeed / 2;
					if (newRate < userSetSpeed) newRate = userSetSpeed;

					if (newRate != lastSpeed) setDelayInternal(newRate);
				}
			}
			sequenceBuffer = sequence;
		}
	}
	public int getDelay () {
		return this.lastSpeed;
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
	private synchronized boolean executePatterns(int [] patt) {
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
	public int getNumberOfRowsCleared () {
		return engine.getLinesCleared();
	}
	private class SequenceExecutor extends TimerTask {

		@Override
		public void run() {
			stepSequence();
			
		}
		
	}
	private class LineRateTracker {
		private long lastTime = 0;
		private double avgTime;
		private int lastNumLines = 0;
		private int [] rateHistory = new int[15];
		private double [] lineTimes = new double [40];
		private double avg = 0d;
		
		
		public void recordShape (int linesCleared) {
			if (lastTime ==0) lastTime = System.nanoTime();
			if (linesCleared > lastNumLines) {

				updateAvg();
				updateTime();
				
				lineTimes[0] = (Math.max(1, linesCleared - lastNumLines)) / ((double)(System.nanoTime() - lastTime)/1000000000d);
				lastTime = System.nanoTime();
				rateHistory[0] = 0;
				
				lastNumLines = linesCleared;
			} else {
				rateHistory[0]++;
			}
		}
		private void updateTime () {
			avgTime = 0l;
			
			for (int i=0;i<lineTimes.length;i++) {
				avgTime += lineTimes[i];
			}
			avgTime /= lineTimes.length;
			for (int i=lineTimes.length-1; i>0;i--) {
				lineTimes [i] = lineTimes[i-1];
			}
		}
		
		private void updateAvg () {
			avg = 0d;
			for (int i=0;i<rateHistory.length;i++) {
				avg += rateHistory[i];
			}
			avg /= rateHistory.length;
			for (int i=rateHistory.length-1; i>0;i--) {
				rateHistory [i] = rateHistory[i-1];
			}
			
			
		}
		
		public double getAvgTime () {
			return avgTime;
		}
		
		public double getLineRate () {
			return avg;
		}
	}
}
