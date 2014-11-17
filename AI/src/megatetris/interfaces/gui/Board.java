package megatetris.interfaces.gui;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import tetris.engine.mechanics.*;

public class Board extends JPanel {
	private int rows;
	private int columns;
	private int squareDimension;
	private int xStartPosition;
	private int yStartPosition;
	private int w;
	private int h;
	private Rectangle[][] gameArray;
	private int[][] gameState;
	private int[][] changes;
	private boolean initializedYet = false;
	private Engine engine;
	Graphics2D g2;
	/**
	 * Create the panel.
	 */
    public void paint(Graphics g)
    {
      super.paint(g);
      boolean resized = false;

      this.g2 = (Graphics2D) g;

      RenderingHints rh =
            new RenderingHints(RenderingHints.KEY_ANTIALIASING,
                               RenderingHints.VALUE_ANTIALIAS_ON);

      rh.put(RenderingHints.KEY_RENDERING,
             RenderingHints.VALUE_RENDER_QUALITY);

      this.g2.setRenderingHints(rh);
      
      if (this.getWidth() != this.w || this.getHeight() != h) {
    	  resized = true;
      }
      this.w = this.getWidth();
      this.h = this.getHeight();

      if (this.initializedYet) {
          if (resized) {
              System.out.println("Resize");
              this.resizeBoard();
          } else {
        	  this.updateSpaces();
          }
      } else {
    	  init();
      }

    }
    public double getSquareSize() {
    	return this.squareDimension;
    }
    private void resizeBoard() {
        this.squareDimension = (int) this.w/this.columns;
        while (this.squareDimension*this.rows > this.h) {
      	  this.squareDimension--;
        }       
        this.xStartPosition = (int) ((this.w - this.columns*this.squareDimension)/2);
        System.out.println("This square dimension: " + this.squareDimension);       
        for (int row=0;row<this.rows;row++) {
        	  for (int column=0;column<this.columns;column++) {
        		  this.gameArray[row][column] = new Rectangle(this.xStartPosition + column*this.squareDimension,row*this.squareDimension,this.squareDimension,this.squareDimension);      		        	
        	  }
          }
        this.updateSpaces();
        this.repaint();
    }
    private void updateSpaces() {
  		for (int row=0;row<this.rows;row++) {
  			for (int column=0;column<this.columns;column++) {
  					this.g2.setColor(this.getColor(gameState[row][column]));
  					this.g2.fill(this.gameArray[row][column]);
  			}
  		}
    }
    public void init() {

    	this.resizeBoard();
        this.initializedYet = true;
    }
    public Color getColor(int colorVal) {
    	switch (colorVal) {
    	case 0 : return Color.white;
    	case 1 : return Color.red;
    	case 2 : return Color.blue;
    	case 3 : return Color.orange;
    	case 4 : return Color.green;
    	case 5 : return Color.yellow;
    	case 6 : return Color.cyan;
    	case 7 : return Color.magenta;
    	case 10: return Color.lightGray;
    	default : return Color.red;
    	}
    }
    public void updateScreen(int[][] gameState) {
    	this.gameState = gameState;
    	this.repaint();
    }
    private int[] getGameCoordsOfPoint(int x,int y) {
    	int[] coords = new int[] {-1,-1};
    	if (this.gameArray == null) return coords;
    	for (int row=0;row<this.gameArray.length;row++) {
    		for (int col=0;col<this.gameArray[0].length;col++) {
    			if (this.gameArray[row][col].contains(x, y)) return new int[] {row,col};
    		}
    	}
    	return coords;
    }
    private void setSpaceInEngine(int row,int col) {
    	this.engine.colorSpace(row,col);
    }
	public Board(int rows, int columns,double initialSquareSize) {

		this.rows = rows;
		this.columns = columns;
		int ss = (int) initialSquareSize;
		int dimy = (int)(this.rows*ss);
		int dimx = (int)(this.columns*ss);
		this.setPreferredSize(new Dimension(dimx,dimy));
		this.gameState = new int[rows][columns];
        this.gameArray = new Rectangle[this.rows][this.columns];
        this.changes = new int[rows][columns];
        this.setDoubleBuffered(true);
        this.setIgnoreRepaint(true);			
	}
	public Board(int rows, int columns,double initialSquareSize, Engine inEngine) {
		this.engine = inEngine;
		this.rows = rows;
		this.columns = columns;
		int ss = (int) initialSquareSize;
		int dimy = (int)(this.rows*ss);
		int dimx = (int)(this.columns*ss);
		this.setPreferredSize(new Dimension(dimx,dimy));
		this.gameState = new int[rows][columns];
        this.gameArray = new Rectangle[this.rows][this.columns];
        this.changes = new int[rows][columns];
        this.setDoubleBuffered(true);
        this.setIgnoreRepaint(true);
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int x = e.getX();
				int y = e.getY();
				int[] coords = getGameCoordsOfPoint(x,y);
				setSpaceInEngine(coords[0],coords[1]);
			}
		});			
	}

}
