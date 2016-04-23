package interfaces;

import java.util.ArrayList;
import java.util.Arrays;

public class ControlMovements {
	private ArrayList<int []> stepBuffer = new ArrayList<int []> ();
	private int ptr = 0;
	
	public ControlMovements () {		
	}
	public ControlMovements (int [][] steps) {
		for (int [] singleStep : steps) {
			if (singleStep != null) {
				if (singleStep.length == 3) {
					stepBuffer.add(Arrays.copyOf(singleStep, singleStep.length));
				} else System.err.println("WARNING: An improper length instruction was given to ControlMovements constructor: " + singleStep.length);
			} else {
				System.err.println("WARNING: A null step was given to ControlMovements constructor");
			}
		}
	}
	public void addAll (ControlMovements more) {
		synchronized (stepBuffer) {
			stepBuffer.addAll(more.stepBuffer);
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
			System.out.print (Arrays.toString(stepBuffer.get(i)));
			if (i==ptr) System.out.print ("  <-- ptr");
			System.out.println();
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
	public int numLeft () {
		return stepBuffer.size() - ptr;
	}
	public int [] next () {
	
		synchronized (stepBuffer) {
			if (hasNextInternal ()) {
				int [] retVal = stepBuffer.get(ptr);
				ptr++;
				return retVal;
			} else {
				return null;
			}
		}
	}
	private boolean hasNextInternal () {
		return ptr >= 0 && ptr < stepBuffer.size();
	}
	public boolean hasNext () {
		synchronized (stepBuffer) {
			return hasNextInternal();
		}
	}
}
