package ai.gameinterface;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

import old_files.ai.ai3.AI;
import old_files.ai.ai3.Stepper;
import tetris.engine.mechanics.Engine;

public class EngineInterface {
	private Engine engine;
	private Stepper stepper;
	public void connectStepper(Stepper g) {
		this.stepper = g;
	}
	private synchronized boolean executePatterns(int[][] patterns) {
		LOGGER.warning("Execute PAtterns... " + this.solutionPointer);
		boolean didWeDoSomething = patterns.length > 0 ? true : false;
		for (int[] patt : patterns) {		
			if (patt.length <= 0) continue;
			//LOGGER.warning("rot: " + patt[0] + " shi: " + patt[1] + " drop: " + patt[2]);
			if (patt[0] > 0) {
				for (int rotate=0;rotate<patt[0];rotate++) {
					this.expectingAChange = true;		
					LOGGER.warning("rotateCW...");
					if(this.engine.forceRotateCW()) {
						this.dumpSolution(new String[] {"Rotate Clockwise"});
						//this.stop();
					}
					this.expectingAChange = false;
				}
			} 
			else if (patt[0] < 0){
				LOGGER.warning("Shift rotate...");
				int r = -patt[0];
				for (int rotate=0;rotate<r;rotate++) {	
					this.expectingAChange = true;
					this.engine.rotateCounterClockwise();
					this.expectingAChange = false;
				}
			}
			if (patt[1] > 0) {
				for (int shift=0;shift<patt[1];shift++) {
					LOGGER.warning("Shift right...");
					this.expectingAChange = true;
					this.engine.forceShiftRight();
					this.expectingAChange = false;
				}
				} 
			else {
				for (int shift=0;shift>patt[1];shift--) {
					LOGGER.warning("Shift left...");
					this.expectingAChange = true;
					this.engine.forceShiftLeft();
					this.expectingAChange = false;
				} 
			}
			if (patt[2] < 0) {
				LOGGER.warning("plummit...");
				this.expectingAChange = true;
				this.engine.plummit();
				this.expectingAChange = false;
			}
			else {
				for (int drop=0;drop<patt[2];drop++) {
					LOGGER.warning("drop...");
					this.expectingAChange = true;
					this.engine.dropShape();
					this.expectingAChange = false;
				}
			}
		}
		if (didWeDoSomething) this.solutionPointer++;
		this.holdUpdates = false;
		return didWeDoSomething;
	}
}
final class Stepper {
	private Timer timer;
	private int delay;
	private boolean tryingToDrop = true;
	private boolean easySpin = false;
	public static void start(int stepper,AI aI) {
		final AI ai = aI;

			final int G = stepper;
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					try {
						new Stepper(G,ai);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		
	}
	public void setEasySpin(boolean val) {
		this.easySpin = val;
	}
	public boolean getEasySpin() {
		return this.easySpin;
	}
	public void setDelay(int time) {
		this.timer.setDelay(time);
	}
	public int getStepper() {
		return this.delay;
	}
	public void stop () {
		this.timer.stop();
	}
	public boolean needToDrop() {
		return tryingToDrop;
	}
	private void setDropRequestStatus(boolean status) {
		tryingToDrop = status;
	}
	public Stepper(int delay,EngineInterface inInterface) {
		final EngineInterface ei = inInterface;
		ei.connectStepper(this);
		this.delay = delay;
		if (delay != 0) { // to allow no stepper
		  ActionListener taskPerformer = new ActionListener() {
		      public void actionPerformed(ActionEvent evt) {
		    	  ei.stepperTick();
		      }
		  };
		  this.timer = new Timer(delay, taskPerformer);
		  this.timer.start();
		}
	}
}
