package ai.logic;

import tetris.engine.mechanics.*;
import interfaces.ControlMovements;
import interfaces.EngineInterface;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.Timer;

import ai.state.BoardState;
import ai.state.GameState;
import ai.state.Journal;
import ai.state.ShapeState;

import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AIBacktrack {
		private boolean holdUpdates;
		private boolean stopped;
		private boolean usePlummit; // not used
		public static boolean plummit; 
		private boolean stillDeciding;
		private boolean expectingASwap;
		private boolean doASwap = false;;
		private boolean expectingAChange = false;
		private boolean enableSwap = false;
		private Thread stepperThread;
		
		private int delay = 0;
		private int[][] gameBoard;
		private SolutionMaster solMaster;
		private int[][] solution = new int[][] {{}};
		private int solutionPointer = -1;
		private boolean testingSolutions = false;
		private Journal journal;
		private EngineInterface engine;
		private static final Logger LOGGER = Logger.getLogger(AI.class.getName());
		
		static {
			LOGGER.setLevel(Logger.getGlobal().getLevel());		
		}

		public AIBacktrack(Engine engine) {
			this (engine, 10, false);
		}
		public AIBacktrack(Engine engine, int AISpeed, boolean usePlummit) {
			this.engine = new EngineInterface (engine);
			this.holdUpdates = false;
			plummit = usePlummit;
			this.stopped = true;
			this.stillDeciding = false;
			this.usePlummit = false;
			this.delay = AISpeed;
			this.engine.setDelay(AISpeed);
			if (this.journal == null) {
				this.journal = new Journal();
			}
			//stepperThread = new Thread (new Stepper(AISpeed));
			//stepperThread.start();
		}
		public synchronized void start () {			
			if (engine != null) {
				this.gameBoard = this.engine.getGameBoard();		
			} else {
				this.gameBoard = this.engine.getGameBoard();
				testingSolutions = true;
			}
			System.out.println ("AI Started...");
			this.holdUpdates = false;
			this.stopped = false;
			this.stillDeciding = false;
			this.solutionPointer = -1;
			if (engine != null) this.solMaster = new SolutionMaster(this.engine,"conf");
		}
		public synchronized void start (String configFile) {
			this.start ();
			this.solMaster = new SolutionMaster(this.engine,configFile);
		}
		public synchronized void stop() {
			testingSolutions = false;
			this.stopped = true;
			this.holdUpdates = true;
			this.stillDeciding = false;
			System.out.println ("AI Stopped...");
			if (this.journal != null && !this.testingSolutions) {
				//this.journal.print();
				File outfile = new File ("./" + Long.toString(Calendar.getInstance().getTimeInMillis()));
				try {					
					this.journal.writeStates(outfile);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.err.println("ERROR: Could not write history to file: " + outfile.getAbsolutePath() + " \nWill ignore and continue like nothing happened.");
				}
			}
		}
		public synchronized boolean isRunning() {
			return !this.stopped;
		}
		public synchronized void step () {
			//System.out.println ("step"); // commented out due to > 3% cpu useage, left in for debbuging purposes
			if (this.stopped) {
				return;
			}
			if (this.engine.isGameLost()) return;
			this.gameBoard = this.engine.getGameBoard(); // so what we think we know is still accurate
			if (this.holdUpdates) {
				return;
			}

			//if (this.engine.wasThereANewShape() || this.expectingASwap || this.testingSolutions) {
			if (this.engine.wasThereANewShape()) {
				//System.out.println ("NEW SHAPE: " + this.engine.getCurrentShape());
				if (!this.expectingASwap);// LOGGER.warning("New shape");
				else 	{
					//LOGGER.warning("Caught a swap!");
				}
				this.expectingASwap = false;
				this.solution = new int[][] {{}};
				this.solutionPointer = -1;
				
				this.doASwap = false;
				this.decide();
				this.dumpSolution(new String[] {"Sol"},true);				
				if (!this.testingSolutions)  this.logState("");
			}					
		}	
		public synchronized void setExpectations() {
			this.expectingAChange = true;
		}
		public synchronized void setSwap(boolean val) {
			this.enableSwap = val;
		}
	/*	private void abortSolution() {
			this.solution = new int[][] {{}};
			this.solutionPointer = -1;
		}*/
		private void logState (String message) {
			if (this.journal != null) {
				GameState currentState = new GameState (new BoardState (this.engine.getGameBoard()), new ShapeState (this.engine.getCoordsOfCurrentShape(), this.engine.getCurrentShape()));
				//this.journal.add(currentState, this.engine.getCurrentShape().toInt(), this.engine.getNextShape().toInt(), this.engine.getSwapShape().toInt(), message + SolutionNode.getLastKnownMessage());
			}
		}
		public synchronized int testSolution (Engine inEngine) {
			if (this.engine == null) this.engine = new EngineInterface (inEngine);
			if (this.stillDeciding) {
				return 0;		
			} else if (this.solMaster != null) {
				LOGGER.info("==================================================================\n"
						+ "\tSolving... \n ===========================================================");
				GameState currentState = new GameState (new BoardState (this.engine.getGameBoard()), new ShapeState (this.engine.getCoordsOfCurrentShape(), this.engine.getCurrentShape()));
				this.solMaster.solve(currentState, false, true, 1);		
				return 0;
			} else {
				System.out.println("No solution Manager found");
				return -1;
			}
		}
		private synchronized void decide() {
			synchronized (engine) {
				//GameState currentState = new GameState (new BoardState (this.gameBoard), new ShapeState (this.engine.getCoordsOfCurrentShape(), this.engine.getCurrentShape()));
				GameState currentState = new GameState (engine);
				if (this.solMaster == null) {
					System.out.println ("ERROR: AI: NULL SolutionMaster");
					return;
				}
				Solution solutionNoSwap = this.solMaster.solve(currentState, false, false, 1);					

				System.out.println ("AI Finished");

				ControlMovements sequence = solutionNoSwap.getSequence();
				this.engine.executeSequence(sequence);
			}
		}	
		public String dumpSolution(String[] messages, boolean printToOutput) {
			/*if (!printToOutput ) return "not logging";
			String message = "";
			if (this.solution.length == 0) {LOGGER.warning("AI -> dumpSolution:Zero length Solution!");return "";}
			if (printToOutput) {
				LOGGER.warning("Shape: " + this.engine.getCurrentShape().toString());
				LOGGER.warning("AI -> dumpSolution: Solution was");
				LOGGER.warning("AI: solutionPointer was: " + this.solutionPointer);
			}
			
			for (int[] patt : this.solution) {
				message += ("Patt: ("+patt[0]+","+patt[1]+","+patt[2]+")\n");

			}			
			for (String mes : messages) {
				message += mes;
			}
			if (printToOutput) LOGGER.warning(message);		
			return message;*/
			return "";
		}
		public void setDelay(int newDelay) {
			if (newDelay <= 0) return;
			else {
				this.delay = newDelay;
			}
		}
		public int getDelay() {
			return this.delay;
		}
		public void usePlummit(boolean val) {
			this.usePlummit = val;
		}
		public boolean getUsePlumit() {
			return this.usePlummit;
		}
	}
