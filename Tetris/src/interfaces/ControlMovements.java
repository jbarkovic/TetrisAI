package interfaces;

import java.util.ArrayList;
import java.util.Arrays;

public class ControlMovements {
	private int [] previous = null;
	private ArrayList<int []> stepBuffer = new ArrayList<int []> ();
	Thread controlThread;
	
	public ControlMovements () {		
	}
	public ControlMovements (int [][] steps) {
		for (int [] singleStep : steps) {
			filterAndAdd (singleStep);
		}
		//dumpPattern();
	}
	public void addAll (ControlMovements more) {
		synchronized (stepBuffer) {
			stepBuffer.addAll(more.stepBuffer);
		}
	}
	@Override
	public ControlMovements clone () {
		ControlMovements newCopy = new ControlMovements (this.stepBuffer.toArray(new int [][] {{}}));
		if (this.previous != null) {
			newCopy.previous = Arrays.copyOf(previous, previous.length);
		}
		return newCopy;
	}
	private void filterAndAdd (int [] step) {
		if (step != null && step.length == 3) {
			int [] clone = new int [] {step[0],step[1],step[2]};
			stepBuffer.add(clone);
		}
	}
	public void dumpPattern () {
		for (int i=0;i<stepBuffer.size();i++) {
			System.out.println (Arrays.toString(stepBuffer.get(i)));
		}
	}
	public void add (int [] step) {
		filterAndAdd (step);
	}
	public int [] previous () {
		return this.previous;
	}
	public int [] next () {
		synchronized (stepBuffer) {
			if (stepBuffer.size() > 0) {
				int [] retVal = stepBuffer.get(0);
				stepBuffer.remove(0);
				previous = retVal;
				return retVal;
			}
			return null;
		}
	}
	public boolean hasNext () {
		return (stepBuffer.size() > 0 && stepBuffer.get(0) != null);
	}
}
