package ai.transformations;

import java.util.logging.Logger;

import ai.logic.SolutionMaster;
import ai.state.*;

public class ShapeTransforms {
	private final static Logger LOGGER = Logger.getLogger(SolutionMaster.class.getName());
	static {
		LOGGER.setLevel(Logger.getGlobal().getLevel());		
		//LOGGER.setLevel(Level.SEVERE);	
	}
	public static boolean isCoordPartOfCurrentShape(GameState inState, int[] coord) {
		for (int[] co : inState.getShape().getCoords()) {		
			if (coord[0] == co[0] && coord[1] == co[1]) return true;
		}
		return false;
	}
	public static GameState predictRotate (GameState inState) {		
		if (canRotate(inState)) {
			ShapeState newCurrentShape = new ShapeState (RotationManager.getNextRotateCoords(inState), inState.getShape().getType());
			inState.setCurrentShape(newCurrentShape);
		}
		return inState;
	}
	public static GameState predictDropOnce (GameState inState) {		
		for (int [] coord : inState.getShape().getCoords()) {
			coord[0]++;
		}
		return inState;
	}
	public static GameState predictShiftRight (GameState inState) {		
		for (int [] coord : inState.getShape().getCoords()) {
			coord[1]++;
		}
		return inState;
	}
	public static GameState predictShiftLeft (GameState inState) {		
		for (int [] coord : inState.getShape().getCoords()) {
			coord[1]--;
		}
		return inState;
	}
	public static GameState predictCompleteDrop(GameState inState) {
		int displacement = 0;		
		int [][] gB = inState.getBoardWithoutCurrentShape().getState();
		for (int offset = 1;offset<=gB.length;offset++) {
			try {
				for (int[] coord : inState.getShape().getCoords()) {
					if (gB [coord[0]+offset] [coord[1]] != 0) {
						throw new ArrayIndexOutOfBoundsException();
					}
				}
			} catch (ArrayIndexOutOfBoundsException e) {				
				displacement = offset - 1;
				break;
			}
		}		
		if (displacement >= 0) { // for speed and robustness	
			for (int [] coord : inState.getShape().getCoords()) {
				coord[0] += displacement;
			}
		} else {
			LOGGER.severe("Displacement Error in predict complete drop");
		}
		return inState;
	}
	public static int[] getShapeLimits(GameState inState) { // returns {minCol,MaxCol,MinRow,MaxRow)
		int [][] coords = inState.getShape().getCoords();
		int minCol = coords[0][1];
		int maxCol = coords[0][1]; // represents distance from left (usual GUI coordinate orientation)
		int minRow = coords[0][0]; // used to help place the shadow
		int maxRow = coords[0][0]; // 0=top, represents distance from top 		
		for (int[] coord : coords) {
			minRow = Math.min(minRow, coord[0]);
			maxRow = Math.max(maxRow, coord[0]);
			minCol = Math.min(minCol, coord[1]);
			maxCol = Math.max(maxCol, coord[1]);
		}
		return new int[] {minCol,maxCol,minRow,maxRow};
	}
	public static boolean canRotate(GameState inState) {
		int [][] gB = inState.getBoardWithoutCurrentShape().getState();
		for (int[] coord : RotationManager.getNextRotateCoords(inState)) {
			if (coord[0] < -500) {
				LOGGER.severe("ERROR:\t\t\tNo next rotate pattern found!");
				return false;			
			} else if (coord[0] > gB.length-1 || coord[1] > gB[0].length-1 || coord[0] < 0 || coord[1] < 0){
				return false;
			} else if (gB[coord[0]][coord[1]] != 0) {
				return false;
			}
		}		
		return true;
	}
	public static boolean canDrop(GameState inState) {
		int [][] gB = inState.getBoardWithoutCurrentShape().getState();
		for (int[] coord : inState.getShape().getCoords()) {
			if (coord[0]+1 > gB.length-1 || coord[0]+1 < 0){
				return false;
			} else if (gB[coord[0] + 1][coord[1]] != 0) {
				return false;
			}
		}
		return true;
	}
	public static boolean canShiftRight(GameState gState) {
		int [][] gB = gState.getBoardWithoutCurrentShape().getState();
		for (int[] coord : gState.getShape().getCoords()) {
			if (coord[1]+1 > gB[0].length-1 || coord[1]+1 < 0){
				return false;
			} else if (gB[coord[0]][coord[1] + 1] != 0) {
				return false;
			}
			
		}
		return true;
	}
	public static boolean canShiftLeft(GameState gState) {
		int [][] gB = gState.getBoardWithoutCurrentShape().getState();
		for (int[] coord : gState.getShape().getCoords()) {
			if (coord[1]-1 > gB[0].length-1 || coord[1]-1 < 0){
				return false;
			} else if (gB[coord[0]][coord[1] - 1] != 0) {
				return false;
			}
		}
		return true;
	}
	public static int[] getShapeLimits(ShapeState shapeState) { // returns {minCol,MaxCol,MinRow,MaxRow)
		int [][] coords = shapeState.getCoords();
		int minCol = coords[0][1];
		int maxCol = coords[0][1]; // represents distance from left (usual GUI coordinate orientation)
		int minRow = coords[0][0]; // used to help place the shadow
		int maxRow = coords[0][0]; // 0=top, represents distance from top 		
		for (int[] coord : coords) {
			minCol = Math.min(minCol, coord[1]);
			maxCol = Math.max(maxCol, coord[1]);
			minRow = Math.min(minRow, coord[0]);
			maxRow = Math.max(maxRow, coord[0]);
		}
		return new int[] {minCol,maxCol,minRow,maxRow};
	}
}
