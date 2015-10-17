package ai.logic;

import java.util.Random;

import ai.state.GameState;
import ai.state.RotationManager;
import ai.state.SHAPETYPE;

public class AITools {
	private static Random r;
	static {
		r = new Random(System.currentTimeMillis());
	}
	public static boolean isGameLost(GameState inState) {
		int colsStart = (inState.numColumns()/2) - 2;
		int colsEnd = colsStart + 4;
		if (colsEnd > inState.numColumns() || colsStart >= inState.numColumns()) {
			return false;
		} else {
			int [][] gB = inState.getBoardWithCurrentShape().getState();
			for (int row=0; row<2;row++) {
				for (int col=colsStart;col<colsEnd;col++) {
					if (gB[row][col] != 0) return false;
				}
			}
			return true;
		}
	}
	public static SHAPETYPE getRandomShape () {
		int rVal = r.nextInt(7)+1;
		return SHAPETYPE.intToShapeType(rVal);
	}
	public static GameState loadShape (GameState inState, SHAPETYPE type) {
		if (RotationManager.doWeKnowShapeYet(type)) {
			return new GameState(inState.getBoardWithCurrentShape(), RotationManager.getStartState(type));
		} else {
			return inState;
		}
	}
}
