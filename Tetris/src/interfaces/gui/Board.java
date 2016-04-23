package interfaces.gui;

import javax.swing.JPanel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import tetris.engine.mechanics.*;

public class Board extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4336635610825775081L;
	private int rows;
	private int columns;
	private int squareDimension;
	private int xStartPosition;
	private int w;
	private int h;
	private Rectangle[] gameArray;
	private int[] gameState;
	private boolean initializedYet = false;
	private Engine engine;

    public void paint(Graphics g)
    {
      super.paint(g);
      boolean resized = false;

      Graphics2D g2 = (Graphics2D) g;
      
      g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
      
      if (this.getWidth() != this.w || this.getHeight() != h) {
    	  resized = true;
          this.w = this.getWidth();
          this.h = this.getHeight();
      }
      if (this.initializedYet) {
          if (resized) {
              System.out.println("Resized");
              this.resizeBoard(g2);
          } else {
        	  this.updateSpaces(g2);
          }
      } else {
    	  init(g2);
      }
    }
    public double getSquareSize() {
    	return this.squareDimension;
    }
    private void resizeBoard(Graphics2D g2) {
        this.squareDimension = (int) this.w/this.columns;
        while (this.squareDimension*this.rows > this.h) {
      	  this.squareDimension--;
        }       
        this.xStartPosition = (int) ((this.w - this.columns*this.squareDimension)/2);
        System.out.println("Square size: " + this.squareDimension + " x " + this.squareDimension); 
        int ptr = 0;
        for (int row=0;row<this.rows;row++) {
        	  for (int col=0;col<this.columns;col++) {
        		  
        		  this.gameArray[ptr] = new Rectangle(this.xStartPosition + col*this.squareDimension,row*this.squareDimension,this.squareDimension,this.squareDimension);
        		  ptr ++;
        	  }
          }
        this.updateSpaces(g2);
        this.repaint();
    }
    private void updateSpaces(Graphics2D g2) {
    	int ptr = 0;
  		for (int row=0;row<this.rows;row++) {
  			for (int col=0;col<this.columns;col++) {
  				
  				Color color = Color.WHITE;
  		    	switch (gameState[ptr]) {
  		    	case 0 : color = Color.WHITE; break;
  		    	case 1 : color = Color.red; break;
  		    	case 2 : color = Color.blue; break;
  		    	case 3 : color = Color.orange; break;
  		    	case 4 : color = Color.green; break;
  		    	case 5 : color = Color.yellow; break;
  		    	case 6 : color = Color.cyan; break;
  		    	case 7 : color = Color.magenta; break;
  		    	case 10: color = Color.lightGray; break;
  		    	default : color = Color.red; break;
  		    	}
  				
  				
  				g2.setColor(color);
  				g2.fill(this.gameArray[ptr]);
  				
  				ptr++;
  			}
  		}
    }
    public void init(Graphics2D g2) {
    	this.resizeBoard(g2);
        this.initializedYet = true;
    }
    public void updateScreen(int[][] newGameState) {
    	
    	boolean repaint = false;
    	for (int row=0;row<rows;row++) {
    		
    		for (int col=0;col<columns;col++) {
    			
    			
    			final int ptr = row*columns + col;
    			final int oldV = gameState[ptr];
    			final int newV = newGameState[row][col];
    			
    			if (oldV != newV) {
    				gameState[ptr] = newV;
    				repaint = true;
    			}
    		}
    	}
    	
    	if (repaint) repaint();
    	
    }
    private int[] getGameCoordsOfPoint(int x,int y) {
    	int[] coords = new int[] {-1,-1};
    	if (this.gameArray == null) return coords;
    	for (int row=0;row<rows;row++) {
    		for (int col=0;col<columns;col++) {
    			final int ptr = row*columns + col;
    			if (this.gameArray[ptr].contains(x, y)) return new int[] {row,col};
    		}
    	}
    	return coords;
    }
    private void setSpaceInEngine(int row,int col) {
    	this.engine.colorSpace(row,col);
    }
    private Color setTransparency (Color original, float transparency) {
    	transparency = (float) Math.max(0.0, transparency);
    	transparency = (float) Math.min(1.0, transparency);
    	
    	return new Color (
    			original.getRed()/(float)255,
    			original.getGreen()/(float)255,
    			original.getBlue()/(float)255,
    			transparency
    			);
    }
    private void initializeGameState (int rows, int cols) {
    	this.gameState = new int [rows*cols];
    }
	public Board(int rows, int columns,double initialSquareSize) {

		this.rows = rows;
		this.columns = columns;
		int ss = (int) initialSquareSize;
		int dimy = (int)(this.rows*ss);
		int dimx = (int)(this.columns*ss);
		this.setPreferredSize(new Dimension(dimx,dimy));
		initializeGameState (rows, columns);
        this.gameArray = new Rectangle[rows * columns];
        this.setDoubleBuffered(true);
        this.setIgnoreRepaint(true);			
	}
	public Board(int rows, int columns,double initialSquareSize, Engine inEngine) {
		this (rows,columns,initialSquareSize);
		this.engine = inEngine;
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
