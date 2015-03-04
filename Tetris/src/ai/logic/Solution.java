package ai.logic;

import interfaces.ControlMovements;

import java.util.ArrayList;

import ai.state.GameState;

public class Solution {
	private static final int MIN_VAL = -99999999;
	private double value = Double.NEGATIVE_INFINITY;
	protected ArrayList<int[]> steps = new ArrayList<int[]> ();
	protected GameState finalState = null; 
	
	public ControlMovements getSequence () {
		return new ControlMovements (steps.toArray(new int [][] {{}}));
	}
	public boolean isValid () {
		return this.value > MIN_VAL;
	}
	public double getValue() {
		return value;
	}
	public void setValue(double value) {
		this.value = Math.max(MIN_VAL, value);
	}
}

