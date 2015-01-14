package interfaces.gui;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;

import javax.swing.JFrame;

import ai.logic.HeuristicAI;
import ai.state.GameState;
import ai.state.Journal;
import interfaces.CallBackMessenger;
import tetris.logging.TetrisLogger;
import tetris.engine.mechanics.*;
import tetris.engine.shapes.SHAPETYPE;

public class GameWindow extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2037282391019749417L;
	private Engine engine;
	private int[][] gameState;
	private Board gameBoard;
	private AuxShapeBoard swapShape;
	private AuxShapeBoard nextShape;
	private boolean needToRequestFocus = false;
	private boolean justStarted = true;
	private ArrayList<GameState> replay;
	private int oldLinesCleared = 0;
	private HeuristicAI ai;
	private boolean germanMode = false;

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
		this.updateShapeBoards(this.engine.getSwapShape(), this.engine.getNextShape());
		if (this.justStarted) {
			this.updateSwap();
			this.justStarted = false;
			return;
		}
		if (this.oldLinesCleared < this.engine.getLinesCleared()) {
			if (this.engine.getLinesCleared() % 10 == 0) {
				int oldGravity = this.engine.getGravity();				
				//this.engine.setGravity(oldGravity - 10);	
				System.out.println("Setting gravity to: " + (oldGravity - 10));
			}
			this.oldLinesCleared = this.engine.getLinesCleared();
		}
		this.ai.step();
		this.setTitle("Mega Tetris" + " " + this.engine.getLinesCleared() + " Lines Cleared");
	}
	private void updateShapeBoards (SHAPETYPE swap, SHAPETYPE next) {
		if (this.nextShape != null && this.swapShape != null) {
			if (this.swapShape.isFocused() || this.nextShape.isFocused()) {
				this.requestFocus();
			}
			this.swapShape.updateScreen(new int[4][4]);
			this.nextShape.updateScreen(new int[4][4]);
			int[][] swapSpace = this.getShapeDisplayPattern(swap);
			int[][] nextShape = this.getShapeDisplayPattern(next);
			this.swapShape.updateScreen(swapSpace);
			this.nextShape.updateScreen(nextShape);
		}
	}
	private int [][] getShapeDisplayPattern (SHAPETYPE type) {
		int [][] out = new int [4][4];
		switch (type) {
		case T : return new int [][] {{0,0,0,0},{0,0,7,0},{0,7,7,7},{0,0,0,0}};
		case I : return new int [][] {{0,0,0,0},{0,0,0,0},{6,6,6,6},{0,0,0,0}};
		case O : return new int [][] {{0,0,0,0},{0,5,5,0},{0,5,5,0},{0,0,0,0}};
		case J : return new int [][] {{0,0,0,0},{0,2,0,0},{0,2,2,2},{0,0,0,0}};
		case L : return new int [][] {{0,0,0,0},{0,0,0,3},{0,3,3,3},{0,0,0,0}};
		case S : return new int [][] {{0,0,0,0},{0,4,4,0},{4,4,0,0},{0,0,0,0}};
		case Z : return new int [][] {{0,0,0,0},{0,1,1,0},{0,0,1,1},{0,0,0,0}};
		default : return out;
		}
	}
	private void updateSwap() {
		if (this.swapShape == null || this.nextShape == null) {
			this.needToRequestFocus = true;
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
	private static void createAI(Engine engine, GameWindow gw, final int AISpeed) {
		final Engine eng = engine;
		final GameWindow gwin = gw;
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					HeuristicAI ai = new HeuristicAI(AISpeed);
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
				int AISpeed = 40;
				String historyFile = null; 
				try {
					int rows = DEFAULTROWS;
					int columns = DEFAULTCOLUMNS;
					int logLevel = 0;
					for (int i=0;i<dim.length;i++) {
						if (dim [i].equals("-l")) {
							try {
								i++;
								logLevel = Integer.parseInt(dim[2]);									
							} catch (ArrayIndexOutOfBoundsException e0) {						
							} catch (NullPointerException e1) {						
							} catch (NumberFormatException e2) {
								try {
									historyFile = dim [2];
								} catch (ArrayIndexOutOfBoundsException e3) {}
									
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
							default : {
								level = level.ALL;
								break;
								}
							}
							TetrisLogger.setup(level);
						}
						else if (dim [i].equals("-t")) {
							try {
								int speed = Integer.parseInt(dim [i+1]);
								System.out.println("AI Speed: " + speed);
								AISpeed = speed;
							} catch (NumberFormatException e2) {
								System.out.println("option -t (AI SPEED) requires an integer argument, usually 30 <= i <= 100");
							}
						}
						else if (dim [i].equals("-s")) {
							try {
								try {
									rows = Integer.parseInt(dim[i+1]);
									columns = Integer.parseInt(dim[i+2]);
									i += 2;
								} catch (ArrayIndexOutOfBoundsException e0) {
									rows = DEFAULTROWS; columns = DEFAULTCOLUMNS;
								} catch (NullPointerException e1) {
									rows = DEFAULTROWS; columns = DEFAULTCOLUMNS;
								} catch (NumberFormatException e2) {
									rows = DEFAULTROWS; columns = DEFAULTCOLUMNS;
								} finally {
									//frame.updateScreen();
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
					GameWindow frame = new GameWindow(rows,columns, historyFile, AISpeed);
					frame.setVisible(true);
				} catch (IOException e) {
					e.printStackTrace();
					System.err.println("ERROR: Failed to start game.");
					throw new RuntimeException ();
				}
			}
		});
	}

	/**
	 * Create the frame."ERROR: SolutionNode: Owner not found"
	 */
	public GameWindow(int rows, int columns, String historyFile, int AISpeed) {

		int scale = 30;
		System.out.println("Starting.. ");
        this.engine = new Engine(rows,columns,500,new CallBackMessenger(this));
        if (historyFile != null) {
        	this.engine.pause();
        	try {
				this.replay = Journal.readJournal(historyFile).getHistory();
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("ERROR: could not read history file, will exit");
				this.dispose();
			}
        }
		this.gameBoard = new Board(rows,columns,scale,this.engine);
        add(this.gameBoard);
        this.pack();
        setTitle("Mega Tetris - 0 Lines Cleared");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);        
        setVisible(true);
        setResizable(true);
        this.gameState = new int[rows][columns];        
        this.createAI(this.engine, this, AISpeed);
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
		if (replay != null) {
			ArrayList<GameState> tempBuffer = (ArrayList<GameState>) replay.clone();
			while (tempBuffer.size() > 0) {
				GameState currentState = tempBuffer.get(0);
				tempBuffer.remove(0);
				updateShapeBoards(SHAPETYPE.intToShapeType(currentState.getOtherShapeData()[1]), SHAPETYPE.intToShapeType(currentState.getOtherShapeData()[2]));
				gameBoard.updateScreen(currentState.getBoardWithCurrentShape().getState());
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

}
