package ai.state;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.ListIterator;

public class Journal {
	ArrayList<GameState> entries = new ArrayList<GameState> ();
	
	public Journal () {}
	private Journal (ArrayList<GameState> entries) {
		this.entries = entries;
	}
	public void add (GameState inState, int currentShape, int nextShape, int swapShape, String message) {
		add (inState, currentShape, nextShape, swapShape);
		inState.setMessage(message);
	}
	public void add (GameState inState, int currentShape, int nextShape, int swapShape) {
		inState.setOtherShapeData(new Integer [] {currentShape, swapShape, nextShape});
		entries.add(inState);
	}
	public ArrayList<GameState> getHistory () {
		return this.entries;
	}
	public void writeStates (File outfile) throws IOException {
		FileOutputStream outstream;
		if (outfile != null) {
			outstream = new FileOutputStream (outfile);			
		} else {
			String name = "./" + Long.toString(Calendar.getInstance().getTimeInMillis());
			outstream = new FileOutputStream (name);
		}
        ObjectOutputStream out = new ObjectOutputStream(outstream);
        out.writeObject(this.entries);
        out.close();
        outstream.close();
	}
	public static Journal readJournal (String filename) throws IOException {
        FileInputStream infile = new FileInputStream(filename);
        ObjectInputStream in = new ObjectInputStream(infile);
        Journal newJournal = null;
        try {
			newJournal = new Journal ((ArrayList<GameState>) in.readObject());
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			System.err.println("Error deserializing class. Could not read journal file.");
			e.printStackTrace();
			System.err.println("Error deserializing class. Could not read journal file.");
		} finally {
			in.close();
			infile.close();
		}
		return newJournal;
	}
	public void print () {
		ListIterator li = entries.listIterator();
		while (li.hasNext()) {
			GameState next = (GameState) li.next();
			int [][] gameBoard = next.getBoardWithCurrentShape().getState();
			for (int c=0;c< gameBoard [0].length*4;c++) {
				System.out.print("=");
			}
			System.out.println();
			System.out.println(next.getMessage());
			
			System.out.println("\tNextShape: " + next.getOtherShapeData() [2] + " \n\tCurrentShape: " + next.getShape().getType() + " \n\tSwapShape: " + next.getOtherShapeData() [1]);
			System.out.print("\t____");
			for (int c=0;c<gameBoard [0].length*3;c++) {
				System.out.print("_");
			}
			System.out.println();
			for (int r=0;r<gameBoard.length;r++) {
				System.out.print("\t");
				System.out.print("||");
				for (int c=0;c<gameBoard [0].length;c++) {
					 System.out.print("[");
					if (gameBoard [r][c] > 0) System.out.print("O");
					else if (gameBoard [r][c] == 0) System.out.print(" ");
					else System.out.print("e");
					 System.out.print("]");
				}
				System.out.print("||");
				System.out.println("");
			}
			System.out.print("\tTTTT");
			for (int c=0;c< gameBoard [0].length*3;c++) {
				System.out.print("T");
			}
			System.out.println();
			System.out.println("\n\n\n");
		}
	}	
}