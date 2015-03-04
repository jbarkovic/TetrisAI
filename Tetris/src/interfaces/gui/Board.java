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
	private Rectangle[][] gameArray;
	private int[][][] gameState;
	private boolean initializedYet = false;
	private Engine engine;

    public void paint(Graphics g)
    {
      super.paint(g);
      boolean resized = false;

      Graphics2D g2 = (Graphics2D) g;

      RenderingHints rh =
            new RenderingHints(RenderingHints.KEY_ANTIALIASING,            				   
                               RenderingHints.VALUE_ANTIALIAS_ON);

      rh.put(RenderingHints.KEY_RENDERING,
             RenderingHints.VALUE_RENDER_QUALITY);

      
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
        for (int row=0;row<this.rows;row++) {
        	  for (int col=0;col<this.columns;col++) {
        		  this.gameArray[row][col] = new Rectangle(this.xStartPosition + col*this.squareDimension,row*this.squareDimension,this.squareDimension,this.squareDimension);      		        	
        	  }
          }
        this.updateSpaces(g2);
        this.repaint();
    }
    private void updateSpaces(Graphics2D g2) {
        Rectangle clipBounds = g2.getClipBounds();
  		for (int row=0;row<this.rows;row++) {
  			for (int col=0;col<this.columns;col++) {
  				if (clipBounds.contains(this.gameArray [row][col])) {
  					g2.setColor(this.getColor(gameState[row][col][0]));
  					g2.fill(this.gameArray[row][col]);
  				}
  			}
  		}
    }
    public void init(Graphics2D g2) {
    	this.resizeBoard(g2);
        this.initializedYet = true;
    }
    public Color getColor(int colorVal) {
    	Color choose;
    	switch (colorVal) {
    	case 0 : {
    		choose = Color.white;
    		//choose = setTransparency(Color.white,(float) 0.5);
    		break;
    		}
    	case 1 : {choose = Color.red; break;}
    	case 2 : {choose = Color.blue; break;}
    	case 3 : {choose = Color.orange; break;}
    	case 4 : {choose = Color.green; break;}
    	case 5 : {choose = Color.yellow; break;}
    	case 6 : {choose = Color.cyan; break;}
    	case 7 : {choose = Color.magenta; break;}
    	case 10: {choose = Color.lightGray; break;}
    	default :{choose = Color.red; break;}
    	}
    	return choose;
    }
    public void updateScreen(int[][] gameState) {
    	for (int row=0;row<this.gameState.length;row++) {
    		for (int col=0;col<this.gameState[row].length;col++) {
				this.gameState [row][col][0] = gameState [row][col];
    			if (this.gameArray != null && this.gameArray [0] != null) {
    				boolean redraw = false;
    				if (this.gameState [row][col][0] != this.gameState [row][col][1]) {
    					this.gameState [row][col][1] = this.gameState [row][col][0];
    					this.gameState [row][col][2] = 0;
    					redraw = true;
    					//this.repaint(this.gameArray[row][col]);  				
    				} else if (this.gameState [row][col][2] > 0) {
    					this.gameState[row][col][2]--;
    					redraw = true;    					
    				}
    				if (redraw) {
    					//this.repaint();
    					if (this.gameArray[row][col] != null) this.repaint(this.gameArray[row][col]);
    				}
    			}
    		}
    	}
    	
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
    	this.gameState = new int [rows][cols][3];
    	for (int row=0;row<rows;row++) {
    		for (int col=0;col<cols;col++) {
    			// Set them different to force a redraw
    			this.gameState [row][col][0] = 0;
    			this.gameState [row][col][1] = 1;
    		}
    	}
    }
	public Board(int rows, int columns,double initialSquareSize) {

		this.rows = rows;
		this.columns = columns;
		int ss = (int) initialSquareSize;
		int dimy = (int)(this.rows*ss);
		int dimx = (int)(this.columns*ss);
		this.setPreferredSize(new Dimension(dimx,dimy));
		initializeGameState (rows, columns);
        this.gameArray = new Rectangle[this.rows][this.columns];
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
