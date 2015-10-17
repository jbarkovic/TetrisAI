package ai.state;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

public class ShapeState implements Comparable<ShapeState>, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8976070161672375966L;
	private int [][] coords;
	private int hash;
	private SHAPETYPE type;
	private int coordsPtr;
	private static MemoryDrugs memory = clearMemory();
	private static MemoryDrugs clearMemory() {
		memory = new MemoryDrugs ();
		return memory;
	}
	public ShapeState (int [][] coords, SHAPETYPE type) {
		//if (memory != null) LOGGER.info ("Previous memory size before clear: " + memory.size());
		clearMemory ();
		this.init (coords, type);
	}
	public ShapeState (ShapeState old, int [][] coords, SHAPETYPE type) {
		this.init (coords, type);
	}
	public ShapeState (ShapeState old) {
		this.init (old.getCoords(), old.type);
	}
	private void init (int [][] coords, SHAPETYPE type) {
		this.coordsPtr = memory.malloc();
		this.coords = memory.dereference (coordsPtr);
		setMemory (coords);
		this.type = type;
	}
	private void setMemory (int[][] coords) {
		for (int i=0;i<4;i++) {
			if (this.coords == null) System.out.println ("coords is null");
			this.coords [i][0] = coords[i][0];
			this.coords [i][1] = coords[i][1];
		}
		this.hash = generateHash();
	}
	protected void setState (ShapeState otherState) {
		setMemory(otherState.coords);
		this.type = otherState.type;
	}
	public int [][] getCoords () {
		return this.coords;
	}
	public SHAPETYPE getType () {
		return this.type;
	}
	public boolean contains (int [] coord) {
		for (int [] c : this.coords)
			if (c[0] == coord[0] && c[1] == coord[1]) return true;		
		return false;
	}
	public boolean stateEquals (ShapeState other) {	
		if (this.coords.length == other.coords.length) {
			for (int i=0;i<this.coords.length;i++) {
				if (!Arrays.equals(this.coords[i], other.coords[i])) return false;
			}
		}
		return true;
	}
	@Override
	public boolean equals (Object arg0) {
		if (arg0 instanceof ShapeState) {
			return stateEquals ((ShapeState) arg0);
		} else {
			return false;
		}
	}
	private int generateHash () {
//		int newHash = 0;
//		for (int i=0;i<this.coords.length;i++) {
//			newHash += this.coords[i][0];
//			newHash <<= 1;
//			newHash += this.coords[i][1];
//		}		
		ArrayList<Integer> hashList = new ArrayList<Integer>();
		for (int [] coord : this.coords) {
			hashList.add(coord[0]);
			hashList.add(coord[1]);
		}
		return hashList.hashCode();
	}
	@Override
	public int hashCode () {
		int newHash = 0;
		for (int i=0;i<this.coords.length;i++) {
			newHash += this.coords[i][0];
			newHash <<= 1;
			newHash += this.coords[i][1];
		}
		return newHash;
	}
	public boolean equals (ShapeState other) {
		//if (this.type == other.type)			
			return stateEquals (other);
		//return true;
	}
	public String dumpState (String message) {
		if (message == null) message = "";
		message += "============================\n";
		message += "ShapeState Dump: " + this.type + "\n";
		for (int i=0;i<this.coords.length;i++) {
			message +="\t[";
			for (int j=0;j<this.coords[0].length-1;j++) {
				message += this.coords[i][j] + ",";
			}
			message += this.coords[i][this.coords[i].length-1] + "]\n";
		}
		message += "\n";
		return message;	
	}
	@Override
	public int compareTo(ShapeState other) {
		int thisHash = this.hashCode();
		int otherHash = other.hashCode();
		if (this.equals(other)) return 0;
		else if (thisHash > otherHash) return 1;
		else return -1;
	}
	private static class MemoryDrugs {
		public int [][][] memory = new int [810][4][2];
		private boolean [] available = new boolean [810];
		int numFull = 0;
		int lastKnownEmpty = 0;
		MemoryDrugs () {
			available [0] = false;
			for (int i=1;i<available.length;i++) {
				memory [i] = new int [4][2];
				available [i] = true;
			}
			lastKnownEmpty = 1;
		}
		public void makeAvailable (int nSpaces) {
			if (nSpaces > memory.length) {
				int [][][] copyMemory = new int [nSpaces+1][4][];
				boolean [] copyAvailable = new boolean [nSpaces+1];
				int i=1;
				for (;i<memory.length;i++) {
					copyMemory [i] = new int [4][2]; 
					copyMemory [i] = memory [i];
					copyAvailable [i] = available [i];
				}
				for (;i<copyAvailable.length;i++) {
					copyMemory [i] = new int [4][2];
					copyAvailable [i] = true;
				}
				memory = copyMemory;
				available = copyAvailable;
			}
		}
		public int malloc () {
			if (numFull >= memory.length-12) {
				makeAvailable (memory.length * 2);
			}
			int index = lastKnownEmpty;
			boolean isFree = available [index];
			while (!isFree) {
				isFree = available [++index];
			}
			lastKnownEmpty = index;
			numFull++;
			available [index] = false;
			return index;
		}
		public final int [][] dereference (int index) {
			if (!inBounds(index)) {
				System.err.println("ERROR: SHAPESTATE>MEMORY: memory address out of bounds" + index);
				return null;
			}
			return memory [index];
		}
		private boolean inBounds (int index) {
			return (index >= 1 && index < memory.length);
		}
		public int size () {
			return this.numFull;
		}
		public void free (int index) {
			if (!inBounds(index)) return;
			if (!available [index]) {
				lastKnownEmpty = index;
				available[index] = true;
				numFull--;
			}
		}
	}
}
