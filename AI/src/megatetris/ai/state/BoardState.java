package megatetris.ai.state;

import java.util.Arrays;

public class BoardState {
	private int [][] boardSpaces;
	public BoardState (BoardState old) {
		this.setState(old.getStateCopy());	
	}
	public BoardState (int [][] board) {
		this.setState(board);
	}
	protected void setState (int [][] board) {
		int [][] copy = Arrays.copyOf(board,board.length);
		for (int i=0;i<copy.length;i++) {
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
