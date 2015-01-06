package ai.state;

import java.util.Arrays;

public class BoardState implements java.io.Serializable {
	// integer arrays are serializable though integers themselves are not 
	private int [] gameSize = new int [2];
	private int [][] boardSpaces;
	
	protected int [] size () {
		return this.gameSize;
	}
	public BoardState (BoardState old) {
		this.setState(old.getStateCopy());	
	}
	public BoardState (int [][] board) {
		this.setState(board);
	}
	protected void setState (int [][] board) {
		gameSize [0] = board.length;
		gameSize [1] = board [0].length;
		
		int [][] copy = Arrays.copyOf(board,gameSize [0]);		
		for (int i=0;i<gameSize [0];i++) {
			copy[i] = Arrays.copyOf(copy[i], copy[i].length);
		}
		this.boardSpaces = copy;
	}
	public int [][] getState () {
		return this.boardSpaces;
	}
	public int [][] getStateCopy () {
		int [][] copy = Arrays.copyOf(this.boardSpaces,this.boardSpaces.length);
		for (int i=0;i<copy.length;i++) {
			copy[i] = Arrays.copyOf(copy[i], copy[i].length);
		}
		return copy;
	}
}
