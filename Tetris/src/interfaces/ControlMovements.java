package interfaces;

import java.util.ArrayList;
import java.util.Arrays;

import ai.logic.AI;
import ai.logic.AIBacktrack;

public class ControlMovements {
	private int [] previous = null;
	private ArrayList<int []> stepBuffer = new ArrayList<int []> ();
	Thread controlThread;
	private AIBacktrack ourAI = null;
	
	public void setAI (AIBacktrack aiBacktrack) {
		this.ourAI = aiBacktrack;
	}
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
	public void dumpShortFormPattern() {
		for (int i=0;i<stepBuffer.size();i++) {
			int [] patt = stepBuffer.get(i);
			int count = 0;
			for (int runner=i;runner<stepBuffer.size();runner++) {
				if (Arrays.equals(patt, stepBuffer.get(runner))) {
					count++;
				} else break;
			}
			System.out.println (getShortForm(patt, count));
			i+= count-1;
		}
	}
	public void dumpPattern () {
		for (int i=0;i<stepBuffer.size();i++) {
			System.out.println (Arrays.toString(stepBuffer.get(i)));
		}
		dumpShortFormPattern();
	}
	public String getShortForm (int [] patt, int count) {
		StringBuilder sb = new StringBuilder ("");
		if (patt[0] > 0) {
			sb.append("RO");
			if (patt[0]>1) sb.append(patt[0] + " ");
		}
		if (patt[1] > 0) {
			sb.append("RI");
			if (patt[1]>1) sb.append(patt[1] + " ");
		}
		if (patt[1] < 0) {
			sb.append("LE");
			if (patt[1]<-1) sb.append(Math.abs(patt[1]) + " ");
		}
		if (patt[2] > 0) {
			sb.append("DR");
			if (patt[2]>1) sb.append(patt[2] + " ");
		}
		if (patt[2]<0) {
			sb.append("PL");
		}
		sb.append(" -> " + count);
		return sb.toString();
	}
	public void add (int [] step) {
		filterAndAdd (step);
	}
	public int [] previous () {
		return this.previous;
	}
	public int numLeft () {
		return stepBuffer.size();
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
