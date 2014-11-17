package megatetris.interfaces.gui;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;


public class AuxShapeBoard extends JFrame {

		private ShapeDisplay sd;
	

	/**
	 * Launch the application.
	 */
	public void updateScreen(int[][] newData) {
		this.sd.updateScreen(newData);
	}
	public int getWindowHeight() {
		return this.getHeight();
	}
	public int getWindowWidth() {
		return this.getWidth();
	}
	public static void start(final GameWindow gw, String title, double squareSize, int x,int y) {
		final int X = x;
		final int Y = y;
		final String t = title; 
		final double ss = squareSize;
		EventQueue.invokeLater(new Runnable() {	
			public void run() {
				try {
					AuxShapeBoard frame = new AuxShapeBoard(ss,t,X,Y);
					frame.setVisible(true);
					gw.linkShapeScreen(frame);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public AuxShapeBoard(double squareSize, String title, int x, int y) {	
		int dimension = (int)(4*squareSize);
		setBounds(x, y, dimension, dimension - 5);
		this.setSize(dimension -8, dimension);
		this.setResizable(false);

		setTitle(title);
		setFocusable(false);
		this.sd = new ShapeDisplay(4,4,squareSize);
		add(this.sd);
		this.pack();
	}

}
