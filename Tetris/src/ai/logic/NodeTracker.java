package ai.logic;

import ai.logic.Solution.DIRECTION;
import ai.state.GameState;
import ai.state.ShapeState;
import ai.transformations.ShapeTransforms;

public class NodeTracker {
	private static final int NUM_THREADS = 8;
	private ShapeMemory [] knownShapes = new ShapeMemory [0];
	private int numKnown = 0;
	private int ptr = -1;
	private int best_index = -1;
	private GameState gState;
	
	private boolean previouslyTracked (ShapeState sState) {
		for (int i=0;i<numKnown;i++) {
			if (knownShapes [i].length() > 0 && knownShapes[i].get(0).sState.getType().equals(sState.getType())) {
				ptr = i;
				return true;
			}
		}
		return false;
	}
	public void track (GameState gState) {
		this.gState = new GameState (gState);
		if (previouslyTracked (gState.getShape())) {
			return;
		} else {
			ShapeMemory [] newList = new ShapeMemory [knownShapes.length+1];
			for (int i=0;i<numKnown;i++) {
				newList [i] = knownShapes [i];
			}
			newList [numKnown] = new ShapeMemory ();
			knownShapes = newList;
			ptr = numKnown;
			recursiveTrack (new ShapeNode (gState), gState, numKnown);
			numKnown++;
		}
	}
	public Solution buildSolution () {
		Solution solution = new Solution ();
		DIRECTION [] steps = knownShapes[ptr].findPath(knownShapes[ptr].getInitialState(), knownShapes[ptr].get(best_index));
		solution.setValue((int) knownShapes[ptr].get(best_index).value);
		for (DIRECTION step : steps) {
			solution.addStep(step);
		}
		return solution;		
	}
	private void recursiveTrack (ShapeNode currentNode, GameState gState, int index) {		
		knownShapes[index].add(currentNode);
		if (ShapeTransforms.canShiftLeft(gState)) {
			GameState nextState = ShapeTransforms.predictShiftLeft(new GameState (gState));
			followRecursion (currentNode,nextState,Solution.DIRECTION.LEFT,index);
		}
		if (ShapeTransforms.canShiftRight(gState)) {
			GameState nextState = ShapeTransforms.predictShiftRight(new GameState (gState));
			followRecursion (currentNode,nextState,Solution.DIRECTION.RIGHT,index);
		}
		if (ShapeTransforms.canDrop(gState)) {
			GameState nextState = ShapeTransforms.predictDropOnce(new GameState (gState));
			followRecursion (currentNode,nextState,Solution.DIRECTION.DOWN,index);
		}
		/*if (ShapeTransforms.canRotate(gState)) {
			GameState nextState = ShapeTransforms.predictRotate(new GameState (gState));
			followRecursion (currentNode,nextState,Solution.DIRECTION.ROTATE,index);
		}*/		
	}
	private void followRecursion (ShapeNode current, GameState nextState, Solution.DIRECTION direction, int index) {
		ShapeNode node = new ShapeNode (nextState);
		if (!isDuplicate(node, index)) {
			current.link(node, direction);
			System.out.println (knownShapes[index].length());
			recursiveTrack (node, nextState, index);
		}
	}
	private boolean isDuplicate (ShapeNode node, int index) {
		return knownShapes[index].contains(node);
	}
	public ShapeState parallelSolve () {
		best_index = -1;
		Thread [] threads = new Thread [NUM_THREADS];
		int span = knownShapes[ptr].length() / (NUM_THREADS - 1);
		for (int i=0;i<NUM_THREADS;i++) {
			int start = i*span;
			int end = start+span;
			if (end >= knownShapes[ptr].length()) end = knownShapes[ptr].length()-1;
			threads [i] = new Thread(new RunnableSolver(start,end,ptr));
			(threads[i]).start();
		}
		for (int i=0;i<NUM_THREADS;i++) {
			try {
				(threads[i]).join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		ShapeState maxState = knownShapes[ptr].get(best_index).sState;
		knownShapes[ptr].sort();
		ShapeState element1 = knownShapes[ptr].getLast().sState;
		if (!element1.equals(maxState)) {
			System.err.println ("ERROR \n ERROR\nERROR NOT SORTED!");
		}
		return element1;
	}
	private class RunnableSolver implements Runnable {
		private int ptr, start, end;
		public RunnableSolver (int start, int end, int ptr) {
			this.ptr = ptr;
			this.end = end;
			this.start = start;
		}
		@Override
		public void run() {
			System.out.println ("Running Parallel solver: {"+start+","+end+"}");
			for (int i=start;i<=end;i++) {
				GameState solState = new GameState (gState);
				solState.setState(solState.getBoardWithoutCurrentShape(), knownShapes[ptr].get(i).sState);
				boolean validNode = true;
				for (int [] coord : knownShapes[ptr].get(i).sState.getCoords()) {
					if (gState.getBoardWithoutCurrentShape().getState()[coord[0]][coord[1]] != 0) validNode = false;
				}
				knownShapes[ptr].get(i).valid = validNode;
				if (validNode) {
					knownShapes[ptr].get(i).value = SolutionValue.calculateSolution(solState, SolutionValue.getSolutionParameters(solState), false);
					if (best_index < 0 || knownShapes[ptr].get(i).value > knownShapes[ptr].get(best_index).value) best_index = i;
				}
			}
		}
		
	}
}
