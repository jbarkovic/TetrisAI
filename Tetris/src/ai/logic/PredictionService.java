package ai.logic;

import ai.state.GameState;
import ai.state.SHAPETYPE;

public class PredictionService {
	GameState startState;
	int numLinesCleared;
	public PredictionService (GameState inState) {
		startState = new GameState(inState);
	}
	public GameStateGraph predictNTimes(int nLevels, int nWidth) {
		/* Defaults */
		nWidth = Math.max(nWidth, 1);
		nLevels = Math.max(nLevels, 1);
		
		/* Setup */
		AIBacktrack ai = new AIBacktrack(); 
		GameStateGraph graphStart = new GameStateGraph(startState);
		
		/* Run the ai */
		recursivePredictNTimes(nLevels,nWidth,ai, graphStart);
		
		return graphStart;
	}
	public GameStateGraph predictForAllShapeTypes (int nLevels) {
		if (nLevels < 0) return new GameStateGraph(startState);
		else {
			AIBacktrack ai = new AIBacktrack(); 
			GameStateGraph graphStart = new GameStateGraph(startState);
			recursivePredictForAllShapes (nLevels, ai, graphStart);
			return graphStart;
		}
	}
	private void recursivePredictNTimes (final int level, final int width, AIBacktrack ai, GameStateGraph current) {
		if (level <= 0) return;
		else {
			for (int i=0;i<width;i++) {
				solveAndAdd (ai, current, AITools.getRandomShape());
			}
			for (GameStateGraph child : current.getChildren()) {
				recursivePredictNTimes(level-1,width,ai,child);
			}
		}
	}
	private void recursivePredictForAllShapes (int level, AIBacktrack ai, GameStateGraph current) {
		if (level <= 0) return;
		else {
			for (SHAPETYPE type : SHAPETYPE.values()) {
				solveAndAdd (ai, current, type);
			}
			for (GameStateGraph child : current.getChildren()) {
				recursivePredictForAllShapes(level-1,ai,child);
			}
		}
	}
	private void solveAndAdd (AIBacktrack ai, GameStateGraph current, SHAPETYPE type) {	
		GameState finalState = ai.decideSOL(current).finalState;
		current.setState(finalState.getBoardWithCurrentShape(), finalState.getShape());
		
		GameStateGraph child = new GameStateGraph(current);
		child.setParent(current);
		current.addChild(child);
		AITools.loadShape(child, type);
	}
}
