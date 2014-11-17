package megatetris.ai.state;

import megatetris.ai.transformations.ShapeTransforms;
import tetris.engine.mechanics.Engine;
import tetris.engine.shapes.SHAPETYPE;

public class RotationManager {
	// rotatePatterns: [
	static private int[][][][] rotatePatterns = new int[0][0][0][0];
	static private int[]	requirementsToRotate = new int[0];
	static private SHAPETYPE[] knownShapes = new SHAPETYPE[0];
	static private int knownShapePointer = 0;

	public static int [][] learnShape(GameState inState, Engine engine, boolean notCurrentShape) {
		SHAPETYPE currType = inState.getShape().getType();
		for (int i=0;i<knownShapes.length;i++) {
			if (knownShapes[i] == currType) {
				if (requirementsToRotate[i] != 0 && ShapeTransforms.getShapeLimits(inState)[2] == 0 && ! notCurrentShape) {
					//engine.dropShape();
				}
				knownShapePointer = i;
				return rotatePatterns[knownShapePointer][0];
			}
		}
		System.out.println("" + rotatePatterns.length + " Patterns known, Learning: " + currType.toString());
		int didWeDrop = 0;
		int[][][] rotPatt = new int[4][4][2];
		for (int i=0;i<4;i++) {
			rotPatt[i] = engine.getCoordsOfCurrentShape().clone();
			while (engine.forceRotateCW()) {
				System.out.println("INFO: LEARN SHAPE: Needed to Drop SHAPETYPE: " + currType.toString() + " at index i= " + i);
				didWeDrop ++;
				engine.dropShape();
				i=0; // Need to re-do the learn, so there is no weird offset
				engine.forceRotateCW();				
			}
			try {
				Thread.sleep(40);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		SHAPETYPE[] types = new SHAPETYPE[knownShapes.length+1];
		int[][][][] patts = new int[rotatePatterns.length+1][4][4][2];
		int[] 		requirements = new int[requirementsToRotate.length+1];
		for (int i=0;i<knownShapes.length;i++) {
			requirements[i] = requirementsToRotate[i];
			types[i] = knownShapes[i];
			patts[i] = rotatePatterns[i].clone();
		}
		requirements[requirements.length-1] = didWeDrop;
		patts[patts.length-1] = rotPatt.clone(); 
		types[types.length-1] = inState.getShape().getType();	
		rotatePatterns = patts;
		knownShapes = types;
		knownShapePointer = rotatePatterns.length-1;
		requirementsToRotate = requirements;
		return rotatePatterns[knownShapePointer][0];
	}
	public static boolean doWeKnowShapeYet (SHAPETYPE checkShape) {
		for (SHAPETYPE known : knownShapes) {
			if (known == checkShape) return true;
		}
		return false;
	}
	public static int[][] getNextRotateCoords (GameState inState) {
		int [][] currentCoords = inState.getShape().getCoordsCopy();
		for (int i=0;i<4;i++) {
			int offsetRow = rotatePatterns[knownShapePointer][i][0][0] - currentCoords[0][0];	
			int offsetCol = rotatePatterns[knownShapePointer][i][0][1] - currentCoords[0][1];	
			int foundCount = 0;
			for (int[] coord : rotatePatterns[knownShapePointer][i]) {
				boolean found = false;				
				for (int c=0;c<currentCoords.length;c++){
					if (currentCoords[c][0] + offsetRow != coord[0] || currentCoords[c][1]  + offsetCol != coord[1]) {
						continue;
					} else {
						foundCount++;
						if (foundCount == 4) {
							found = true;
							break;
						}
					}
				}
				if (found) {
					int[][] nextPatt = new int[4][2];
					for (int coordinate=0;coordinate<nextPatt.length;coordinate++) {
						nextPatt[coordinate] = new int[] {rotatePatterns[knownShapePointer][(i+1) % 4][coordinate][0],rotatePatterns[knownShapePointer][(i+1) % 4][coordinate][1]};
						nextPatt[coordinate][0] -= offsetRow;
						nextPatt[coordinate][1] -= offsetCol;
					}
					return nextPatt;
				}	
			}
		}
		return new int[][] {{-9000,-9000},{-9000,-9000},{-9000,-9000},{-9000,-9000}};
	}
}
