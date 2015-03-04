package ai.logic;

import interfaces.EngineInterface;
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
	public synchronized Solution solve(GameState inState, final boolean notCurrentShape, boolean testOnly , int futureSteps, final boolean isTopLevel) {
		Solution retVal = new Solution ();
		if (isTopLevel) {
			LOGGER.severe("Starting new solution for SHAPETYPE: " + inState.getShape().getType().toString());
		}
		if (futureSteps < 1) futureSteps = 1;
		if (notCurrentShape && !RotationManager.doWeKnowShapeYet(inState.getShape().getType())) return new Solution ();

		ShapeState rotationLearnedShape =  new ShapeState (RotationManager.learnShape(inState, this.engine, notCurrentShape),inState.getShape().getType());
		GameState ours = new GameState (inState.getBoardWithCurrentShape(), rotationLearnedShape);
		
		if (testOnly) {
			ShapeTransforms.predictCompleteDrop(ours);
			SolutionValue.calculateSolution(ours, SolutionValue.getSolutionParameters(ours));
			System.out.println(solutionText);
			return new Solution ();
		} else {
			SolutionNode start = new SolutionNode(inState, null,SolutionNode.SolutionDir.START,this.engine.getNextShape());
			retVal.setValue(start.Solve());			
			start.followSolution();
			
			int [][] solutionPattern = start.getSolution();
			//convertDropsToPlummit (solutionPattern);
			for (int [] line : solutionPattern) {
				retVal.steps.add(line);
			}			
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
	public int [][] convertDropsToPlummit (int [][] path) {
		for (int i=path.length-2;i>=0;i--) {
			if (!(path[i][0]==0 && path[i][1]==0 && path[i][2]==1)) {
				path[i+1][0] = 0;
				path[i+1][1] = 0;
				path[i+1][2] = -1;
				break;
			}
		}
		return path;
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
