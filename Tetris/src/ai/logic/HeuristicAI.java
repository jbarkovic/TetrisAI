package ai.logic;

import tetris.engine.mechanics.*;
import tetris.engine.shapes.*;
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

public class HeuristicAI {
		private boolean holdUpdates;
		private boolean stopped;
		private boolean usePlummit; // not used
		public static boolean plummit; 
		private boolean stillDeciding;
		private boolean waitStep = false;
		private boolean expectingASwap;
		private boolean doASwap = false;;
		private boolean expectingAChange = false;
		private boolean enableSwap = false;
		private int emergencyShiftAmount;
		private int delay = 0;
		private int direction = 1;
		private int[][] gameBoard;
		private int[][] coordsOfCurrentShape;
		private int[][] previousShapeCoords;
		private int minRowLastShape = -1; // to determine if we got a new shape
		private SHAPETYPE currentShapeType = SHAPETYPE.NONE;
		private final int EMPTYCOLOR = 0;
		private final int SHADOWCOLOR = 10;
		private SolutionMaster solMaster;
		private int[][] solution = new int[][] {{}};
		private int solutionPointer = -1;
		private Stepper stepper;
		private boolean testingSolutions = false;
		private int moveCount = 0;
		private Journal journal;
		private EngineInterface engine;
		private static final Logger LOGGER = Logger.getLogger(AI.class.getName());
		static {
			LOGGER.setLevel(Level.SEVERE);		
		}

		public HeuristicAI() {
			this (30, false);
		}
		public HeuristicAI(int AISpeed, boolean usePlummit) {

			this.holdUpdates = false;
			plummit = usePlummit;
			this.stopped = true;
			this.stillDeciding = false;
			this.usePlummit = false;
			this.waitStep = false;
			this.emergencyShiftAmount = 0;
			this.delay = AISpeed;		
			Stepper.start(this.delay,this);

		}
		public void run(Engine inEngine) {			
			if (inEngine != null) {
				this.engine = new EngineInterface (inEngine);
				this.gameBoard = this.engine.getGameBoard();		
				if (this.journal == null) {
					this.journal = new Journal();
				}
			} else {
				this.gameBoard = this.engine.getGameBoard();
				testingSolutions = true;
			}

			this.holdUpdates = false;
			this.stopped = false;
			this.stillDeciding = false;
			this.emergencyShiftAmount = this.gameBoard[0].length;
			this.waitStep = false;
			this.solutionPointer = -1;
			if (inEngine != null) this.solMaster = new SolutionMaster(this.engine,"conf");
		}
		public void run(Engine engine, String configFile) {
			this.run(engine);
			this.solMaster = new SolutionMaster(this.engine,configFile);
		}
		public void stop() {
			testingSolutions = false;
			this.stopped = true;
			this.holdUpdates = true;
			this.stillDeciding = false;
			if (this.journal != null && !this.testingSolutions) {
				this.journal.print();
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
		public boolean isRunning() {
			return !this.stopped;
		}
		public void step() {
			System.out.println ("step");
			if (this.stopped) {
				this.waitStep = false;
				return;
			}
			if (this.engine.isGameLost()) return;
			this.gameBoard = this.engine.getGameBoard(); // so what we think we know is still accurate
			this.coordsOfCurrentShape = this.engine.getCoordsOfCurrentShape();
			this.waitStep = false;

			if (this.holdUpdates) {
				this.waitStep = false;
				return;
			}

			if (this.engine.wasThereANewShape() || this.expectingASwap || this.testingSolutions) {
				if (!this.expectingASwap);// LOGGER.warning("New shape");
				else 	{
					//LOGGER.warning("Caught a swap!");
				}
				this.expectingASwap = false;
				this.solution = new int[][] {{}};
				this.solutionPointer = -1;
				this.waitStep = false;

				
				this.doASwap = false;
				this.decide(true);
				this.dumpSolution(new String[] {"Sol"},true);				
				//if (!this.testingSolutions)  this.logState("");
			} else {			
				if (this.expectingAChange) {
					return;
				}
			}
			this.waitStep = false;
		}	
		public void setExpectations() {
			this.expectingAChange = true;
		}
		public void setSwap(boolean val) {
			this.enableSwap = val;
		}
		private void abortSolution() {
			this.solution = new int[][] {{}};
			this.solutionPointer = -1;
		}
		private void logState (String message) {
			if (this.journal != null) {
				GameState currentState = new GameState ();
				currentState.setState(new BoardState (this.engine.getGameBoard()), new ShapeState (this.engine.getCoordsOfCurrentShape(), this.engine.getCurrentShape()));
				this.journal.add(currentState, this.engine.getCurrentShape().toInt(), this.engine.getNextShape().toInt(), this.engine.getSwapShape().toInt(), message + SolutionNode.getLastKnownMessage());
			}
		}
		public int testSolution (Engine inEngine) {
			if (this.engine == null) this.engine = new EngineInterface (inEngine);
			if (this.stillDeciding) {
				return 0;		
			} else if (this.solMaster != null) {
				LOGGER.info("==================================================================\n"
						+ "\tSolving... \n ===========================================================");
				GameState currentState = new GameState ();
				currentState.setState(new BoardState (this.engine.getGameBoard()), new ShapeState (this.engine.getCoordsOfCurrentShape(), this.engine.getCurrentShape()));
				this.solMaster.solve(currentState, false, true, 1);		
				return 0;
			} else {
				System.out.println("No solution Manager found");
				return -1;
			}
		}
		private int decide(boolean takeRisks) {
			System.out.println ("deciding");
			int val = 1;
			if (this.stillDeciding) {
				System.out.println ("Was still deciding");
				return 0;		
			} else {	
				System.out.println("Working");
				this.stillDeciding = true; // so were not interrupted
				this.solutionPointer = -1;
				this.abortSolution();

				System.out.println ("About to work");
				GameState currentState = new GameState ();
				currentState.setState(new BoardState (this.gameBoard), new ShapeState (this.engine.getCoordsOfCurrentShape(), this.engine.getCurrentShape()));
				for (int c=0;c<this.engine.getGameBoard()[0].length;c++) {
					LOGGER.info(Integer.toString(this.engine.getGameBoard()[this.engine.getGameBoard().length-1][c]));
				}
				System.out.println();
				System.out.println("AI DUMPING...");
				GameState.dumpState(currentState, true);
				Solution solutionNoSwap = this.solMaster.solve(currentState, false, false, 1);					

				this.solution = (int[][]) solutionNoSwap.steps.toArray(new int[][] {});
				//	}
				this.solutionPointer = -1;

				// solution will automatically execute

				this.stillDeciding = false; // now we can be interrupted
				this.finishSolution();
				System.out.println ("Done working");
				return val;
			}			
		}	
		public String dumpSolution(String[] messages, boolean printToOutput) {
			if (!printToOutput ) return "not logging";
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
			return message;
		}
		private void stepSolution() {
			int oldSolPoint = this.solutionPointer;
			if (oldSolPoint < 0) oldSolPoint = 0;
			if (this.doASwap) {
				this.holdUpdates = true;
				this.expectingASwap = true;
				this.engine.swapShapes();
				this.solution = new int[0][0];
				this.solutionPointer = 0;
				this.doASwap = false;


				this.holdUpdates = false;
				this.step();
				LOGGER.warning("AI -> stepSolution: After Swap Holding...");
				return;
			}
			if (this.solutionPointer == this.solution.length || oldSolPoint < 0) {
				return;
			} else if (!this.stillDeciding){
				LOGGER.warning("Notice: AI: running pattern at " + oldSolPoint);
				this.executePatterns(new int[][] {this.solution[oldSolPoint]});
			}
		}
		public synchronized void stepperTick() {
			if (!this.stopped && !this.stillDeciding) this.stepSolution();	
		}
		public void setDelay(int newDelay) {
			if (newDelay <= 0) return;
			else {
				this.delay = newDelay;
				this.stepper.setDelay(newDelay);
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
		private void finishSolution() {
			if (this.usePlummit) this.executePatterns(new int[][] {{0,0,-1}});
			else {
				if (this.usePlummit) this.executePatterns(new int[][] {{0,0,-1}});
			}
			this.solutionPointer = -1;
		}
		private int[][] reversePatterns(int[][] old) {
			int[][] newPatt;
			if (old == null) newPatt = null;
			else {
				newPatt = null;
				for (int i=0;i<old.length;i++) {				
					newPatt = this.addToPatternHistory(newPatt, new int[][] {{-old[1][0],-old[i][1],old[i][2]}});
				}
			}
			return newPatt;
		}
		private int[][] addToPatternHistory(int[][] old,int[][] newPatt) {
			int[][] temp;
			int oldLength;
			if (old == null) oldLength = 0;
			else oldLength = old.length;

			temp = new int[oldLength+newPatt.length][3];

			for (int i=0;i<temp.length-newPatt.length;i++) {
				temp[i] = old[i].clone();
			}
			for (int j=oldLength,k=0;j<temp.length;j++,k++) {
				temp[j] = newPatt[k];	
			}
			return temp.clone();
		}
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
						if(this.engine.forceRotate ()) {
							this.dumpSolution(new String[] {"Rotate Clockwise"},true);
							//this.stop();
						}
						this.expectingAChange = false;
					}
				} 
				else if (patt[0] < 0){
					int r = -patt[0];
					for (int rotate=0;rotate<r;rotate++) {	
						this.expectingAChange = true;
						this.engine.rotateCounterClockwise();
						this.expectingAChange = false;
					}
				}
				if (patt[1] > 0) {
					for (int shift=0;shift<patt[1];shift++) {
						this.expectingAChange = true;
						this.engine.forceShiftRight();
						this.expectingAChange = false;
					}
				} 
				else {
					for (int shift=0;shift>patt[1];shift--) {
						this.expectingAChange = true;
						this.engine.forceShiftLeft();
						this.expectingAChange = false;
					} 
				}
				if (patt[2] < 0) {
					this.expectingAChange = true;
					this.engine.plummit();
					this.expectingAChange = false;
				}
				else {
					for (int drop=0;drop<patt[2];drop++) {
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
		private int[] getShapeLimits(int[][] coords) { // returns {minCol,MaxCol,MinRow,MaxRow)		
			int minCol = this.gameBoard[0].length;
			int maxCol = 0; // represents distance from left (usual GUI coordinate orientation)
			int minRow = this.gameBoard.length; // used to help place the shadow
			int maxRow = 0; // 0=top, represents distance from top 		
			for (int[] coord : coords) {
				if (coord[1] < minCol) minCol = coord[1];
				if (coord[1] > maxCol) maxCol = coord[1];
				if (coord[0] < minRow) minRow = coord[0];
				if (coord[0] > maxRow) maxRow = coord[0];
			}
			return new int[] {minCol,maxCol,minRow,maxRow};
		}
	}
	final class Stepper {
		private Timer timer;
		private int delay;
		private boolean tryingToDrop = true;
		private boolean easySpin = false;
		public static void start(int stepper,HeuristicAI heuristicAI) {
			final HeuristicAI ai = heuristicAI;

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
		public Stepper(int delay,HeuristicAI inAI) {
			final HeuristicAI ai = inAI;
			ai.connectStepper(this);
			this.delay = delay;
			if (delay != 0) { // to allow no stepper
				ActionListener taskPerformer = new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						ai.stepperTick();
					}
				};
				this.timer = new Timer(delay, taskPerformer);
				this.timer.start();
			}
		}
	}

