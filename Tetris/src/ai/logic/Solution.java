package ai.logic;

import java.util.ArrayList;

import ai.state.GameState;

public class Solution {
	public enum DIRECTION {
		LEFT, RIGHT, DOWN, ROTATE
	}
	private static final int MIN_VAL = -99999999;
	private int value = -999999;
	protected ArrayList<int[]> steps = new ArrayList<int[]> ();
	protected GameState finalState = new GameState (); 
	
	public void addStep (DIRECTION direction) {
		switch (direction) {
		case LEFT   : {steps.add(new int [] {0,-1,0}); break;}
		case RIGHT  : {steps.add(new int [] {0,1,0});  break;}
		case DOWN   : {steps.add(new int [] {0,0,1});  break;}
		case ROTATE : {steps.add(new int [] {1,0,0});  break;}
		default :;
		}
	}
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

