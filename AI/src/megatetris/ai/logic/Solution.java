package megatetris.ai.logic;

import java.util.ArrayList;
import megatetris.ai.state.GameState;

public class Solution {
	private static final int MIN_VAL = -99999999;
	private int value = -999999;
	protected ArrayList<int[]> steps = new ArrayList<int[]> ();
	protected GameState finalState = new GameState (); 
	
	public boolean isValid () {
		return this.value > MIN_VAL;
	}
	public int getValue() {
		return value;
	}
	public void setValue(int value) {
		this.value = Math.max(MIN_VAL, value);
	}
}

