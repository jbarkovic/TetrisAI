package ai.logic;

import tetris.engine.mechanics.*;
import tetris.engine.shapes.*;
import interfaces.EngineInterface;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import ai.state.BoardState;
import ai.state.GameState;
import ai.state.Journal;
import ai.state.RotationManager;
import ai.state.ShapeState;
import ai.transformations.ShapeTransforms;


public class SolutionMaster {
	static String solutionText = "";
	private final EngineInterface engine;
	
	private Journal journal;
	
	private final static Logger LOGGER = Logger.getLogger(SolutionMaster.class.getName());
	static {
		LOGGER.setLevel(Logger.getGlobal().getLevel());		
		LOGGER.setLevel(Level.SEVERE);	
	}

	public SolutionMaster () {
		this.engine = null;
		SolutionValue.loadConfiguration(null);
	}
	public SolutionMaster (EngineInterface engine,String configFile,Journal journal) {
		this(engine,configFile);
		this.journal = journal;			
	}
	public SolutionMaster (EngineInterface engine,String configFile) {		//	 this was added to facilitate tweaking of the weighting function coefficients
		this.engine = engine;
		SolutionValue.loadConfiguration(configFile);
	}
	public Solution solve(GameState inState, boolean notCurrentShape, boolean testOnly) {
		return solve(inState,notCurrentShape,testOnly,1);
	}
	public Solution solve(GameState inState, boolean notCurrentShape, boolean testOnly , int futureSteps) {
		return solve(inState,notCurrentShape,testOnly, futureSteps, true);
	}
	public Solution solve(GameState inState, final boolean notCurrentShape, boolean testOnly , int futureSteps, final boolean isTopLevel) {
		Solution retVal = new Solution ();
		if (isTopLevel) {
			LOGGER.severe("Starting new solution for SHAPETYPE: " + inState.getShape().getType().toString());
		}
		if (futureSteps < 1) futureSteps = 1;
		if (notCurrentShape && !RotationManager.doWeKnowShapeYet(inState.getShape().getType())) return new Solution ();


		//knownSolutionNodes = new ArrayList<SolutionNode> ();  			// this is very important for correct operation without re-instantiation each time
		GameState ours = new GameState ();
		ours.setState(inState.getBoardWithoutCurrentShape(), new ShapeState (RotationManager.learnShape(inState, this.engine, notCurrentShape),inState.getShape().getType()));
	//	System.out.println("Solution Master Dumping: \n");
	//	ours.dumpState(ours, true);
		// get start coords. for "hypothetical" shape

		if (testOnly) {
			ShapeTransforms.predictCompleteDrop(ours);
			SolutionValue.calculateSolution(ours, SolutionValue.getSolutionParameters(ours), false);
			System.out.println(solutionText);
			return new Solution ();
		} else {
			SolutionNode start = new SolutionNode(inState, null,SolutionNode.SolutionDir.START);
			retVal.setValue(start.Solve());

			LOGGER.info("SolutionMaster: -> solutionVal: "+ retVal.getValue());
			//TRAVERSE SOLUTION
			if (isTopLevel) {
				LOGGER.info("SOLVER FOUND: (" + retVal.getValue() +")\n"+start.getMessage());

				start.followSolution();
				retVal.finalState.setState(new BoardState (inState.getBoardWithoutCurrentShape()), new ShapeState (start.getSolutionCoords(), inState.getShape().getType()));
				int [][] solutionPattern = SolutionNode.getSolution();
				for (int [] line : solutionPattern) {
					retVal.steps.add(line);
				}
			} else {
				//this.start.followSolution();
			}
			//		if (futureSteps > 1 && futureSteps < IMAGINARY_DEPTH) {
			//			this.putImaginaryShape = true;
			//			int valO = 0;
			//			int valI = 0;
			//			int valT = 0;
			//			int valJ = 0;
			//			int valL = 0;
			//			int valS = 0;
			//			int valZ = 0;
			//			int min = -1;
			//			int avg = -1;
			//			int[][][] ourCoords = this.imaginaryShapeCoords.clone();
			//			ourCoords[futureSteps] = SolutionNode.getSolutionCoords();
			//			LOGGER.info("Level " + futureSteps + "  of solution started");
			//			this.imaginaryShapeCoords = ourCoords;
			//			this.imaginaryUpdate = futureSteps >= 0 ? futureSteps : -1;
			//			/*Square*/
			//			valO = this.solve(SHAPETYPE.O, true, futureSteps-1, false);
			//			this.imaginaryShapeCoords = ourCoords;
			//			/*Straight*/
			//			valI = this.solve(SHAPETYPE.I, true, futureSteps-1, false);
			//			this.imaginaryShapeCoords = ourCoords;
			//			/*L*/
			//			valL = this.solve(SHAPETYPE.L, true, futureSteps-1, false);
			//			this.imaginaryShapeCoords = ourCoords;
			//			/*J*/
			//			valJ = this.solve(SHAPETYPE.J, true, futureSteps-1, false);
			//			this.imaginaryShapeCoords = ourCoords;
			//			/*T*/
			//			valT = this.solve(SHAPETYPE.T, true, futureSteps-1, false);
			//			this.imaginaryShapeCoords = ourCoords;
			//			/*S*/
			//			valS = this.solve(SHAPETYPE.S, true, futureSteps-1, false);
			//			this.imaginaryShapeCoords = ourCoords;
			//			/*Z*/
			//			valZ = this.solve(SHAPETYPE.Z, true, futureSteps-1, false);
			//			this.imaginaryShapeCoords = ourCoords;
			//			
			//			//int min = Math.max(0, Math.min(valO, Math.min(valI,Math.min(valT,Math.min(valL,Math.min(valJ,Math.min(valS,valZ))))))); // there can be neginf solutions when the shape is not known yet
			//			//int avg = (int) ((Math.max(0,valO) + Math.max(0,valT) + Math.max(0,valI) + Math.max(0,valL) + Math.max(0,valJ) + Math.max(0,valS) + Math.max(0,valZ)) / 7.0);
			//			int [] data = new int[] {valO, valI, valT, valL, valJ, valS, valZ};
			//			Arrays.sort(data);
			//			int median = data[3];
			//			
			//			if (isTopLevel) {
			//				this.imaginaryShapeCoords = new int[IMAGINARY_DEPTH][4][2];
			//				this.imaginaryBoard = null;
			//				this.imaginaryUpdate = 0;
			//				this.putImaginaryShape = false;
			//			}
			//			LOGGER.info("Level " + futureSteps + "  of solution finished. MED: " + median + ", MIN: " + min + ", AVG: " + avg);
			//			
			//			solutionVal += median;
			//		}				
			LOGGER.severe("Ending new solution for SHAPETYPE: " + inState.getShape().getType().toString());		
			return retVal;
		}
	}

	private void printCoords(String[] message, int[][] coords) {
		for (int[] c : coords) {
			LOGGER.info("("+c[0]+","+c[1]+")");
		}
		LOGGER.info("Accompanying message: ");
		for (String mes : message) {
			LOGGER.info(mes);
		}
	}

	private boolean areCoordsEqual(int[][] coords1, int[][] coords2) {
		try {
			for (int row=0;row<coords1.length;row++) {
				for (int col=0;col<coords2[row].length;col++) {
					if (coords1[row][col] != coords2[row][col]) {
						return false;
					}
				}
			}
		} catch (ArrayIndexOutOfBoundsException e) { // will be thrown if column sizes are not the same
			return false;
		}
		return true;
	}
	public int[][] combineDropsInHistory(int[][] history) {
		int[][] newHistory = null; // empty, don't worry, the method we call handles this
		for (int i=0;i<history.length;i++) {
			if (history[i][0] == 1 && history[i][1] == 0 && history[i][2] == 0) {
				int rotCount = 0;
				for (int j=i;j<history.length;j++) {					
					if (history[j][0] == 1 && history[j][1] == 0 && history[j][2] == 0) {
						rotCount++;
					} else break;
					if (rotCount == 4) {
						history[j] = new int[] {0,0,0};
						history[j-1] = new int[] {0,0,0};
						history[j-2] = new int[] {0,0,0};
						rotCount = 0;
					}
				}
			}
			else if (history[i][0] == 0 && history[i][1] != 0 && history[i][2] == 0) {
				int shiftCount = 0;
				int loopCount = 0;
				for (int j=i;j<history.length;j++) {
					loopCount++;
					if (history[j][0] == 0 && history[j][1] != 0 && history[j][2] == 0) {
						shiftCount += history[j][1];
					} else break;
					if (loopCount > 0 && shiftCount == 0) {
						for (int k=i+1;k<=j;k++) {
							history[k] = new int[] {0,0,0};
						}
						break;
					}
				}
			}
		}
		return newHistory;
	}

}
