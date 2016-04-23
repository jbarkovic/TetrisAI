package ai.state;

import java.io.Serializable;
import java.util.Arrays;

public class BoardState implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 4307861709018106033L;
	private int [] gameSize = new int [2];
	private int [][] boardSpaces;
	private static MemoryDrugs memory;
	
	protected int [] size () {
		return this.gameSize;
	}
	public BoardState (BoardState old) {
		this.setState(old.getState());	
	}
	public BoardState (int [][] board) {
		if (memory == null) {
			memory = new MemoryDrugs (board.length,board[0].length);
			memory.makeAvailable(board.length*board[0].length*6);
		}
		memory.clearAll();
		this.setState(board);
	}
	protected void setSpaces (int [][] spaces, int value) {
		for (int i=0;i<spaces.length;i++) {
			boardSpaces [spaces[i][0]][spaces[i][1]] = value;
		}
	}
	protected void clearSpaces (int [][] spaces) {
		setSpaces (spaces, 0);
	}
	protected void setState (BoardState other) {
		setState (other.boardSpaces);
	}
	public boolean equals (BoardState other) {
		return Arrays.equals(gameSize, other.gameSize) && Arrays.deepEquals(boardSpaces, other.boardSpaces);
	}
	
	public boolean colorBlindEquals (BoardState other) {
		if (other == null) return false;
		else if (this.boardSpaces.length != other.boardSpaces.length) return false;
		else {
			for (int i=0;i<boardSpaces.length;i++) {
				if (boardSpaces[i].length != other.boardSpaces[i].length) return false;
				else {
					for (int j=0;j<boardSpaces[i].length;j++) {
						if ((boardSpaces[i][j]==0)^(other.boardSpaces[i][j]==0)) return false;
					}
				}
			}
		}
		return true;
	}
	
	private void setState (int [][] board) {
		if (gameSize[0] != board.length || gameSize[1] != board[0].length) {
			gameSize [0] = board.length;
			gameSize [1] = board [0].length;
			if (memory == null) {
				System.err.println ("ERROR: memory was null, using an inefficient allocation strategy");
				int [][] copy = Arrays.copyOf(board,gameSize [0]);		
				for (int i=0;i<gameSize [0];i++) {
					copy[i] = Arrays.copyOf(copy[i], copy[i].length);
				}
				this.boardSpaces = copy;
			} else {
				this.boardSpaces = memory.dereference(memory.malloc());
			}
		} 		
		for (int i=0;i<boardSpaces.length;i++) {
			for (int j=0;j<boardSpaces[0].length;j++) {
				boardSpaces[i][j] = board[i][j];
			}
		}		
	}
	public int [][] getState () {
		return this.boardSpaces;
	}
	public String dumpBoard () {
		String message = "";
		for (int row = 0; row<this.getState().length;row++) {
			message += "||";
			for (int col=0;col<this.getState()[0].length;col++) {
				message += this.getState()[row][col];
			}
			message += "||\n";
		}
		return message;
	}
	private static class MemoryDrugs {
		public int [][][] memory;
		private boolean [] available;
		int [] size;
		int numFull = 0;
		int lastKnownEmpty = 0;
		MemoryDrugs (int rows, int cols) {
			size = new int [] {rows,cols};
			memory = new int [81][rows][cols];
			available = new boolean [81];
			available [0] = false;
			for (int i=1;i<available.length;i++) {
				memory [i] = new int [rows][cols];
				available [i] = true;
			}
			lastKnownEmpty = 1;
		}
		public void makeAvailable (int nSpaces) {
			if (nSpaces > memory.length) {
				int [][][] copyMemory = new int [nSpaces+1][size[0]][];
				boolean [] copyAvailable = new boolean [nSpaces+1];
				int i=1;
				for (;i<memory.length;i++) {
					copyMemory [i] = new int [size[0]][size[1]]; 
					copyMemory [i] = memory [i];
					copyAvailable [i] = available [i];
				}
				for (;i<copyAvailable.length;i++) {
					copyMemory [i] = new int [size[0]][size[1]];
					copyAvailable [i] = true;
				}
				memory = copyMemory;
				available = copyAvailable;
			}
		}
		public int malloc () {
			if (numFull >= memory.length-1) {
				makeAvailable ((memory.length + 1) * 2);
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
			//System.out.println("Dereferencing index: " + index);
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
		public void clearAll () {
			for (int i=available.length-1;i>0;i--) {
				free (i);
			}
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
