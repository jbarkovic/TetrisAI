package megatetris.interfaces.gui;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.logging.Level;

import javax.swing.JFrame;
import javax.swing.JPanel;

import megatetris.interfaces.CallBackMessenger;
import tetris.logging.TetrisLogger;
import megatetris.ai.*;
import megatetris.ai.logic.AI;
import megatetris.ai.logic.HeuristicAI;
import tetris.engine.mechanics.*;
import tetris.engine.shapes.SHAPETYPE;

public class GameWindow extends JFrame {
	private Engine engine;
	private JPanel contentPane;
	private int[][] gameState;
	private Board gameBoard;
	private AuxShapeBoard swapShape;
	private AuxShapeBoard nextShape;
	private int scale = 30;
	private boolean swapStarted = false;
	private boolean nextStarted = false;
	private boolean needToRequestFocus = false;
	private boolean justStarted = true;
	private int oldLinesCleared = 0;
	private HeuristicAI ai;
	private boolean germanMode = false;
	/**
	 * Launch the application.
	 */
	public void linkShapeScreen(AuxShapeBoard asb) {
		if (this.swapShape == null) {
			this.swapShape = asb;
	        Point location = this.getLocation();
			int scale = (int) (this.gameBoard.getSquareSize()*1.6);	
	        int w = this.getWidth();

        	AuxShapeBoard.start(this, "Next Shape", scale, (int)location.getX()+w,(int)location.getY() + this.swapShape.getHeight() );
        	return;
		} else if (this.nextShape == null) {
			this.nextShape = asb;
	        this.needToRequestFocus = true;		       
		


	       // this.engine.pause();
		    // this.requestFocus();
			this.swapShape.updateScreen(new int[4][4]);
			int[][] swapSpace = this.engine.getSwapBoard();
			int[][] nextShape = this.engine.getNextShapeBoard();
			this.swapShape.updateScreen(swapSpace);
			this.nextShape.updateScreen(nextShape);
		}
	}
	public void updateScreen() {			
		if (this.needToRequestFocus) {
			this.requestFocus();
			this.needToRequestFocus = false;
			if (this.engine.isPaused()) {
				this.engine.pause();
			}
		}
		if (this.engine.isGameLost()) {
			this.engine.pause();
			for (int row=0; row<this.gameState.length;row++) {
				for (int column=0;column<this.gameState[0].length;column++) {
					//this.gameState[row][column] = -1;
				}
			}
		} else {
			this.gameState = this.engine.getGameBoard();
		}
		this.gameBoard.updateScreen(this.gameState);		
		if (this.nextShape != null && this.swapShape != null) {
			if (this.swapShape.isFocused() || this.nextShape.isFocused()) {
				this.requestFocus();
			}
			this.swapShape.updateScreen(new int[4][4]);
			this.nextShape.updateScreen(new int[4][4]);
			int[][] swapSpace = this.engine.getSwapBoard();
			int[][] nextShape = this.engine.getNextShapeBoard();
			this.swapShape.updateScreen(swapSpace);
			this.nextShape.updateScreen(nextShape);
		}
		if (this.justStarted) {
			this.updateSwap();
			this.justStarted = false;
			return;
		}
		if (this.oldLinesCleared < this.engine.getLinesCleared()) {
			if (this.engine.getLinesCleared() % 10 == 0) {
				int oldGravity = this.engine.getGravity();				
				this.engine.setGravity(oldGravity - 10);	
				System.out.println("Setting gravity to: " + (oldGravity - 10));
			}
			this.oldLinesCleared = this.engine.getLinesCleared();
		}
		this.ai.step();
		this.setTitle("Mega Tetris" + " " + this.engine.getLinesCleared() + " Lines Cleared");
	}
	private void updateSwap() {
		if (this.swapShape == null || this.nextShape == null) {
			this.needToRequestFocus = true;
			this.swapStarted = true; // so only one swap window is drawn
			int scale = (int) (this.gameBoard.getSquareSize()*1.6);	
	        int w = this.getWidth();
	        Point location = this.getLocation();
	        AuxShapeBoard.start(this, "Swap Shape", scale, (int)location.getX()+w,(int)location.getY() );
	        if (!this.engine.isPaused()) this.engine.pause();
	        return;
			}
			this.swapShape.updateScreen(new int[4][4]);
			int[][] swapSpace = this.engine.getSwapBoard();
			int[][] nextShape = this.engine.getNextShapeBoard();
			this.swapShape.updateScreen(swapSpace);
			this.nextShape.updateScreen(nextShape);		
	}
	public void rotateClockwise() {
		this.engine.rotateClockwise();
	}
	public void rotateCounterClockwise() {
		this.engine.rotateCounterClockwise();
	}
	private void shiftLeft() {
		this.engine.shiftLeft();
	}
	private void shiftRight() {
		this.engine.shiftRight();
	}
	public boolean drop() {
		return this.engine.dropShape();
	}
	public void toggleDropShadow() {
		this.engine.enableDropShadow(!this.engine.getDropShadowEnabled());
	}
	public void newGame() {
		this.main(new String[] {Integer.toString(this.gameState.length),Integer.toString(this.gameState[0].length)});
		this.nextShape.dispose();
		this.swapShape.dispose();
		this.dispose();
	}
	public void plummit() {
		this.engine.plummit();
		this.updateScreen();;
	}
	public void toggleAI() {
		if (this.ai == null) return;
		if (!this.ai.isRunning()) {
			this.ai.run(this.engine);;
		} else this.ai.stop();
	}
	public void connectAI(HeuristicAI ai) {
		this.ai = ai;
	}
	public void probeSolution () {
		this.ai.testSolution(this.engine);
	}
	private static void createAI(Engine engine, GameWindow gw) {
		final Engine eng = engine;
		final GameWindow gwin = gw;
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					HeuristicAI ai = new HeuristicAI();
						gwin.connectAI(ai);
					
				} catch (Exception e) {
					e.printStackTrace();
				}	
			}
		});
	}
	private void impossible() {
		this.engine.impossible();
	}
	private void setGravity(int g) {
		this.engine.setGravity(g);
	}
	private void swapShapes() {
		this.engine.swapShapes();
	}
	private void pause() {		
		this.engine.pause();
		if (this.engine.isPaused()) this.ai.stop();		
	}
	private void requestShape(SHAPETYPE type) {
		this.engine.requestNextShape(type);
	}
	public static void main(String[] args) {
		System.out.println("Loading...");
		final String[] dim = args;
		final int DEFAULTROWS = 16;
		final int DEFAULTCOLUMNS = 10;
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					int logLevel = 0;
					try {
						logLevel = Integer.parseInt(dim[2]);					
					} catch (ArrayIndexOutOfBoundsException e0) {						
					} catch (NullPointerException e1) {						
					} catch (NumberFormatException e2) {				
					}
					Level level = Level.OFF;
					switch (logLevel) {
					case 0 : {level = Level.OFF; break;}
					case 1 : {level = Level.SEVERE; break;}
					case 2 : {level = Level.WARNING; break;}
					case 3 : {level = Level.INFO; break;}
					case 4 : {level = Level.FINE; break;}
					case 5 : {level = Level.FINER; break;}
					case 6 : {level = level.FINEST; break;}
					case 7 : {level = level.CONFIG; break;}
					case 8 : {level = level.ALL; break;}
					default : {level = level.ALL; break;}
					}
					TetrisLogger.setup(level);
				} catch (IOException e) {
					e.printStackTrace();
					System.err.println("ERROR: Failed to initialize logging system. Will exit.");
					throw new RuntimeException ();
				}
				try {
					int rows = 8;
					int columns = 8;
					try {
						rows = Integer.parseInt(dim[0]);
						columns = Integer.parseInt(dim[1]);
					} catch (ArrayIndexOutOfBoundsException e0) {
						rows = DEFAULTROWS; columns = DEFAULTCOLUMNS;
					} catch (NullPointerException e1) {
						rows = DEFAULTROWS; columns = DEFAULTCOLUMNS;
					} catch (NumberFormatException e2) {
						rows = DEFAULTROWS; columns = DEFAULTCOLUMNS;
					} finally {
						GameWindow frame = new GameWindow(rows,columns);
						frame.setVisible(true);
						//frame.updateScreen();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame."ERROR: SolutionNode: Owner not found"
	 */
	public GameWindow(int rows, int columns) {

		int scale = 30;
		System.out.println("Starting.. ");
        this.engine = new Engine(rows,columns,500,new CallBackMessenger(this)); 
		this.gameBoard = new Board(rows,columns,scale,this.engine);
        add(this.gameBoard);
        this.pack();
        setTitle("Mega Tetris - 0 Lines Cleared");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);        
        setVisible(true);
        setResizable(true);
        this.gameState = new int[rows][columns];        
        this.createAI(this.engine, this);
        this.requestFocus();
		this.addKeyListener(new KeyAdapter() {
			private boolean holdInput = false;
			private boolean holdcRotate = false;
			private boolean holdccRotate = false;
			@Override
			public void keyTyped(KeyEvent arg0) {
				if (this.holdInput) return; 
				char key = arg0.getKeyChar();
				
				if (arg0.isControlDown()) {
					switch (key) {
						case 't' :;
						case 'T' : {
							requestShape(SHAPETYPE.T);
							break;
						}
						case 'i' :;
						case 'I' : {
							requestShape(SHAPETYPE.I);
							break;
						}
						case 'j' :;
						case 'J' : {
							requestShape(SHAPETYPE.J);
							break;
						}
						case 'l' :;
						case 'L' : {
							requestShape(SHAPETYPE.L);
							break;
						}
						case 's' :;
						case 'S' : {
							requestShape(SHAPETYPE.S);
							break;
						}
						case 'z' :;
						case 'Z' : {
							requestShape(SHAPETYPE.Z);
							break;
						}
						case 'o' :;
						case 'O' : {
							requestShape(SHAPETYPE.O);
							break;
						}
					}
				}
				
				if (key == 'r' || key == 'R') {
					if (!this.holdcRotate) {
						this.holdcRotate = true;
						if(!this.holdInput)rotateClockwise();
						if (germanMode) probeSolution();
					}
				}
				else if (key == 'e' || key == 'E') {
					if (!this.holdccRotate) {
						this.holdccRotate = true;
						if(!this.holdInput)rotateCounterClockwise();
						if (germanMode) probeSolution();
					}
				}
				else if (key == 'p' || key == 'P') {
					if(!this.holdInput)pause();
				}
				else if (key == 'i' || key == 'I') {
					toggleAI();
				}
				else if (key == 'a' || key == 'A') {
					if(!this.holdInput)shiftLeft();
				}
				else if (key == 'g' || key == 'G') {
					if(!this.holdInput)setGravity(800);
				}
				else if (key == 'd' || key == 'D') {
					if(!this.holdInput)shiftRight();
				}
				else if (key == 's' || key == 'S') {
					if(!this.holdInput)drop();
				}
				else if (key == 'n' || key == 'N') {
					if(!this.holdInput)newGame();
				}
				else if (key == 'o' || key == 'O') {
					if(!this.holdInput)toggleDropShadow();
				}
				else if (key == 'u' || key == 'U') {
					if(!this.holdInput)impossible();
				}
				else if (key == 'm' || key == 'M') {
					probeSolution();
				}
				else if (key == '9') {
					germanMode = !germanMode;
					System.out.println("GERMAN MODE " + ((germanMode) ? "ON" : "OFF"));
				}
				// new
				try {
					int shape = Integer.parseInt(""+key);
					requestShape(SHAPETYPE.intToShapeType(shape));
				} catch (NumberFormatException e) {
					// Do nothing
				}
//				else if (key == '1') {
//					requestShape(Engine.ShapeType.I);
//				}
//				else if (key == '2') {
//					requestShape(Engine.ShapeType.L);
//				}
//				else if (key == '3') {
//					requestShape(Engine.ShapeType.J);
//				}
//				else if (key == '4') {
//					requestShape(Engine.ShapeType.O);
//				}
//				else if (key == '5') {
//					requestShape(Engine.ShapeType.Z);
//				}
//				else if (key == '6') {
//					requestShape(Engine.ShapeType.S);
//				}
//				else if (key == '7') {
//					requestShape(Engine.ShapeType.T);
//				}
			}
			public void keyReleased(KeyEvent arg0) {
				this.holdccRotate = false;
				this.holdcRotate = false;
			}
			@Override
			public void keyPressed(KeyEvent arg0) {
					if (this.holdInput) return;
					if (arg0.getKeyCode() == KeyEvent.VK_LEFT) {						
						if(!this.holdInput)shiftLeft();
						if (germanMode) probeSolution();
						return;
					}
					if (arg0.getKeyCode() == KeyEvent.VK_RIGHT) {
						if(!this.holdInput)shiftRight();
						if (germanMode) probeSolution();
						return;
					}
					if (arg0.getKeyCode() == KeyEvent.VK_DOWN) {
						if (drop()) {
						}
						if (germanMode) probeSolution();
						return;
					}
					if (arg0.getKeyCode() == KeyEvent.VK_SHIFT) {
						if(!this.holdInput)rotateClockwise();
						if (germanMode) probeSolution();
						return;
					}
					if (arg0.getKeyCode() == KeyEvent.VK_SPACE) {
						if(!this.holdInput)swapShapes();
						if (germanMode) probeSolution();
						return;
					}
					if (arg0.getKeyCode() == KeyEvent.VK_CAPS_LOCK) {
						this.holdInput = true;
						plummit();
						this.holdInput = false;
						return;
					}
			}			
		});
	}

}
