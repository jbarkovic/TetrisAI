package ai.logic;

import java.util.ArrayList;
import java.util.ListIterator;

public class Journal {
	ArrayList<GameState> entries = new ArrayList<GameState> ();
	private int height, width;
	public Journal(int height, int width) {
		this.height = height;
		this.width = width;
	}
	public void add (int[][] board, int currentShape, int nextShape, int swapShape, String message) {
		add (board, currentShape, nextShape, swapShape);
		entries.get(entries.size()-1).setMessage(message);
	}
	public void add (int[][] board, int currentShape, int nextShape, int swapShape) {
		entries.add(new GameState (board, currentShape,nextShape,swapShape));
	}
	public void print () {
		ListIterator li = entries.listIterator();
		while (li.hasNext()) {
			GameState next = (GameState) li.next();
			for (int c=0;c<next.gameBoard[0].length*4;c++) {
				System.out.print("=");
			}
			System.out.println();
			System.out.println(next.getMessage());
			
			System.out.println("\tNextShape: " + next.nextShape + " \n\tCurrentShape: " + next.currentShape + " \n\tSwapShape: " + next.swapShape);
			System.out.print("\t____");
			for (int c=0;c<next.gameBoard[0].length*3;c++) {
				System.out.print("_");
			}
			System.out.println();
			for (int r=0;r<next.gameBoard.length;r++) {
				System.out.print("\t");
				System.out.print("||");
				for (int c=0;c<next.gameBoard[0].length;c++) {
					 System.out.print("[");
					if (next.gameBoard[r][c] > 0) System.out.print("O");
					else if (next.gameBoard[r][c] == 0) System.out.print(" ");
					else System.out.print("e");
					 System.out.print("]");
				}
				System.out.print("||");
				System.out.println("");
			}
			System.out.print("\tTTTT");
			for (int c=0;c<next.gameBoard[0].length*3;c++) {
				System.out.print("T");
			}
			System.out.println();
			System.out.println("\n\n\n");
		}
		
	}
	private class GameState {
		int [][] gameBoard;	
		int currentShape = -1;
		int nextShape = -1;
		int swapShape = -1;
		private String message;
		public GameState (int[][] board, int currentShape, int nextShape, int swapShape) {
			this.gameBoard = board;
			this.currentShape = currentShape;
			this.nextShape = nextShape;
			this.swapShape = swapShape;
		}
		public void setMessage (String message) {
			this.message += message;
		}		
		public String getMessage () {
			return this.message;
		}
	}

}