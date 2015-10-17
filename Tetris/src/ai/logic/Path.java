package ai.logic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.ListIterator;

public class Path extends ArrayList<PathNode> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7465355897894398428L;
	
	public ArrayList<int[]> toControlSequence () {
		ListIterator<PathNode> li = this.listIterator();
		ArrayList<int[]> sequence = new ArrayList<int[]> (this.size());
		sequence.add(new int[]{0,0,0}); // noop
		int count=0;
		while (li.hasNext()) {
			PathNode next = li.next();
			if (li.hasNext()) {
				
				PathNode nextnext = li.next();
				if (nextnext.equals(next.getDown())) {
					sequence.add(new int [] {0,0,1});
				} else if (nextnext.equals(next.getRight())) {
					sequence.add(new int [] {0,1,0});
				} else if (nextnext.equals(next.getLeft())) {
					sequence.add(new int [] {0,-1,0});
				} else if (nextnext.equals(next.getRotate())) {
					sequence.add(new int[] {1,0,0});
				} else {
					System.out.println("WARNING: Path.java: Could not match node " + Arrays.toString(next.getLocation()));
				}
				li.previous();
			}
		}
		// Add a final drop to land the shape when game is paused
		sequence.add(new int [] {0,0,1});
		return sequence;
	}
}
