package interfaces.gui;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;

import ai.logic.AIBacktrack;
import ai.state.GameState;
import tetris.engine.mechanics.*;
import tetris.engine.shapes.SHAPETYPE;

public class GameWindow extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2037282391019749417L;
	private Engine engine;
	private int[][] gameState = null;
	private SHAPETYPE swapShapeData = SHAPETYPE.I;
	private SHAPETYPE nextShapeData = SHAPETYPE.I;
	private Board gameBoard;
	private AuxShapeBoard swapShape;
	private AuxShapeBoard nextShape;
	private boolean needToRequestFocus = false;
	private boolean justStarted = true;
	private ArrayList<GameState> replay;
	private int oldLinesCleared = 0;
	private AIBacktrack ai;
	private int germanMode = 0;
	public String [] args = null;
	private GUI guiClass;
	private InfoWindow infoWindow;
	
	static {
		Logger logger = Logger.getGlobal();
		logger.setLevel(Level.OFF);			
	}

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
	public synchronized void updateScreen() {	
		if (this.needToRequestFocus) {
			this.requestFocus();
			this.needToRequestFocus = false;
			if (this.engine.isPaused()) {
				this.engine.pause();
			}
		}
		if (this.engine.isGameLost()) {
			this.engine.pause();

		} else {
				synchronized (gameState) {
					gameState = this.engine.getGameDisplayBoard();
				}
				synchronized (swapShapeData) {
					synchronized (nextShapeData) {
						swapShapeData = engine.getSwapShape();
						nextShapeData = engine.getNextShape();
					}
				}
				
				if (justStarted) {
					updateSwap();
					justStarted = false;
					return;
				}
				if (oldLinesCleared < engine.getLinesCleared()) {
					oldLinesCleared = engine.getLinesCleared();
					setTitle();
				}
		}
	}
	private class ScreenUpdater extends TimerTask {
		@Override
		public void run() {
			//System.out.println("run1");
			synchronized (gameState) {
				//System.out.println("run2");
				if (gameState != null) gameBoard.updateScreen(gameState);
				else System.out.println("GS null");
			}
			synchronized (swapShapeData) {
				synchronized (nextShapeData) {
					//System.out.println("run3");
					if (nextShapeData != null && swapShapeData != null) {
						updateShapeBoards(swapShapeData, nextShapeData);
					}
				}
			}
			//System.out.println("run5");
		/*	if (oldLinesCleared < engine.getLinesCleared()) {
				oldLinesCleared = engine.getLinesCleared();
				setTitle();
			}*/
		}
			
	}
	private void setTitle () {
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
		if (this.args != null) GUI.main(args);
		else GUI.main(new String[] {Integer.toString(this.gameState.length),Integer.toString(this.gameState[0].length)});
		this.nextShape.dispose();
		this.swapShape.dispose();
		this.dispose();
	}
	public void plummit() {
		this.engine.plummit();
		this.updateScreen();;
	}
	public void toggleAI() {
		if (this.ai == null) {
			System.err.println ("Null AI");
			return;
		} else if (this.guiClass.cback.isAIRunning()) {
			this.guiClass.cback.stopAI();
		} else {
			this.guiClass.cback.startAI();
		}
	}
	public void connectAI(AIBacktrack ai) {
		this.ai = ai;
	}
	public void probeSolution () {
		/*if (germanMode > 0) {
			GameState testState = new GameState (EngineInterface());
			GameState.dumpState(testState, true);
			if (germanMode >= 2) testState = new GameState (ShapeTransforms.predictCompleteDrop(testState));
			GameState.dumpState(testState, true);
			ComputedValue values = new SolutionValue().getSolutionParameters(testState);
			double finalVal = new SolutionValue().calculateSolution(testState, values);
			System.out.println ("Value after a Complete Drop:");
			System.out.println ("VALUE: " + finalVal);
			System.out.println ("SHAPETYPE: " + testState.getShape().getType());
			System.out.println (values.dump());
			
		
		}*/
	}
	private GameState overlay (GameState top, GameState bottom) {
		GameState overlay = new GameState(top);
		int [][] gB = bottom.getBoardWithCurrentShape().getState();
		for (int [] coord : bottom.getShape().getCoords()) {
			overlay.getBoardWithCurrentShape().getState()[coord[0]][coord[1]] = -1;
		}
		overlay.invalidate();
		return overlay;
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
	private void toggleGermanMode () {
		germanMode = (++germanMode) % 3;
		System.out.println("GERMAN MODE LEVEL: " + germanMode + ((germanMode > 0) ? "[ON]" : "[OFF]"));
	}
	private void pause() {		
		this.engine.pause();
		if (this.engine.isPaused() && this.guiClass != null) this.guiClass.cback.stopAI();		
	}
	private void requestShape(SHAPETYPE type) {
		this.engine.requestNextShape(type);
	}
	protected static void afterInit (GameWindow gw) {
		gw.setVisible(true);
	}
	/**
	 * Create the frame."ERROR: SolutionNode: Owner not found"
	 */
	public GameWindow (final GUI gui) {
		this.ai = gui.ai;
		this.engine = gui.engine.getEngine();
		this.args = gui.args;
		guiClass = gui;
		int scale = 30;
		System.out.println("Starting.. ");
		this.gameBoard = new Board(gui.rows,gui.cols,scale,engine);
		//infoWindow.start();
        add(this.gameBoard);
        this.pack();
        setTitle("Mega Tetris - 0 Lines Cleared");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);        
        setVisible(true);
        setResizable(true);
        this.gameState = new int[gui.rows][gui.cols];        
        setTitle();
        this.requestFocus();
        
        Timer screenUpdateTimer = new Timer();
        screenUpdateTimer.schedule(new ScreenUpdater (), 100, 70);
        System.out.println("Screen timer set");
        
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
						if (germanMode > 0) probeSolution();
					}
				}
				else if (key == 'e' || key == 'E') {
					if (!this.holdccRotate) {
						this.holdccRotate = true;
						if(!this.holdInput)rotateCounterClockwise();
						if (germanMode > 0) probeSolution();
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
					if(!this.holdInput) toggleGermanMode() ;
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
				else if (key == '+') {
					int oldValue = gui.engine.getDelay();
					int increment = (int)Math.ceil(oldValue*0.15);
					int newValue = oldValue + increment;
					if (newValue == 0) newValue = 1;
					if (newValue > oldValue) {
						gui.engine.setDelay(newValue);
					}
				}
				else if (key == '-') {
					int oldValue = gui.engine.getDelay();
					int increment = (int)Math.ceil(oldValue*0.15);
					int newValue = oldValue - increment;
					if (newValue < oldValue && newValue >= 0) {
						gui.engine.setDelay(newValue);
					}
				}
				else if (key == '9') {
					toggleGermanMode();
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
						if (germanMode>0) probeSolution();
						return;
					}
					if (arg0.getKeyCode() == KeyEvent.VK_RIGHT) {
						if(!this.holdInput)shiftRight();
						if (germanMode>0) probeSolution();
						return;
					}
					if (arg0.getKeyCode() == KeyEvent.VK_DOWN) {
						if (drop()) {
						}
						if (germanMode>0) probeSolution();
						return;
					}
					if (arg0.getKeyCode() == KeyEvent.VK_SHIFT) {
						if(!this.holdInput)rotateClockwise();
						if (germanMode>0) probeSolution();
						return;
					}
					if (arg0.getKeyCode() == KeyEvent.VK_SPACE) {
						if(!this.holdInput)swapShapes();
						if (germanMode>0) probeSolution();
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
			System.out.println ("Found a replay that is " + replay.size() + " items long.");
			Thread replayThread = new Thread (new ReplayWalker (replay, 50));
			replayThread.start();
		}
	}
	class ReplayWalker implements Runnable {
		ArrayList<GameState> replay = null;
		int replaySpeed = 30;
		ReplayWalker (ArrayList<GameState> replay, int replaySpeed) {
			this.replay = (ArrayList<GameState>) replay.clone();
			this.replaySpeed = replaySpeed;
		}
		@Override
		public void run() {
			try {
				Thread.sleep(replaySpeed + 20);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			if (replay != null) {
				GameState currentState = null;
				while (replay.size() > 0) {
					System.out.println ("Stepping through the replay");
					currentState = replay.get(0);
					replay.remove(0);
					while (gameBoard == null || nextShape == null || swapShape == null) {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					updateShapeBoards(SHAPETYPE.intToShapeType(currentState.getOtherShapeData()[1]), SHAPETYPE.intToShapeType(currentState.getOtherShapeData()[2]));
					gameBoard.updateScreen(currentState.getBoardWithCurrentShape().getState());
					try {
						Thread.sleep(90);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				if (currentState != null) {
					for (int i=0;i<currentState.getBoardSize()[0];i++) {
						for (int j=0;j<currentState.getBoardSize()[1];j++) {
							engine.colorSpace(i, j, currentState.getBoardWithoutCurrentShape().getState()[i][j]);							
						}
					}
					engine.requestNextShape( tetris.engine.shapes.SHAPETYPE.intToShapeType(currentState.getShape().getType().toInt()));
				}
			}
			if (engine.isPaused()) engine.pause();
		}			
	}
}
