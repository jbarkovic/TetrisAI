package ai.logic;

import ai.logic.Solution.DIRECTION;
import ai.state.GameState;
import ai.state.ShapeState;

public class ShapeNode {	
	protected static final int NUM_CHILDREN = 4;
	public ShapeState sState;
	public long value = 0;
	public boolean valid;
	protected ShapeNode [] neighbours = new ShapeNode [NUM_CHILDREN];
	DIRECTION [] dirs = new Solution.DIRECTION [] {DIRECTION.LEFT, DIRECTION.RIGHT, DIRECTION.ROTATE, DIRECTION.DOWN};
	int distance = Integer.MAX_VALUE;
	boolean visited;
	
	public ShapeNode (GameState gState) {
		this.sState = new ShapeState (gState.getShape());
		this.valid = true;
	}
	public void link (ShapeNode toOther, Solution.DIRECTION relationship) {
		Solution.DIRECTION backwards = null;
		switch (relationship) {
		case LEFT : {backwards = Solution.DIRECTION.RIGHT;break;}
		case RIGHT : {backwards = Solution.DIRECTION.LEFT;break;}
		default : {backwards = null; break;}
		}		
		for (int i=0;i<NUM_CHILDREN; i++) {
			if (dirs[i].equals(relationship)) {
				neighbours[i] = toOther;
			}
		}
		if (backwards != null) {
			for (int i=0;i<NUM_CHILDREN; i++) {
				if (toOther.dirs[i].equals(backwards)) {
					toOther.neighbours[i] = this;
				}
			}
		}
	}
	public void setInfiniteDistance () {
		this.distance = Integer.MAX_VALUE;
		this.visited = false;
	}
	public boolean equals (ShapeNode other) {
		return this.sState.equals(other.sState);
	}
}
