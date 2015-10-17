package tetris.engine.mechanics;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Timer;

import tetris.engine.shapes.*;



public class Engine implements Tetris{
	private Shape currentShape;
	private Shape swappedShape;
	private Shape nextShape;
	private Space[][] gameSpaceArray;
	private Space[][] swapShapeGrid;
	private Space[][] nextShapeGrid;
	private boolean dropDone;
	private boolean holdDrops = false;
	private boolean dropShadow = false;
	private boolean wasThereNewShape = false;
	private final int rows;
	private final int columns;
	private int linesCleared;
	private boolean swapShapes;
	private boolean swapUsed;
	private int previousChosenShape;
	private boolean pause;
	//	private boolean holdInput;
	private boolean impossibleMode;
	private Gravity gravity;
	private static final int EMPTYCOLOR = 0;
	private static final int SHADOWCOLOR = 10;
	private boolean gameOver = false;
	private CallBack callBackMessenger;
	private Space[] startSpaces;
	private int numShapes = 0;
	private Shape[] shapeBuffer;
	private SHAPETYPE nextShapeRequest;
	private Object accessLock = new Object();
	private ShapeStats shapeStats = new ShapeStats();
	Random random = new Random();

	private final static Logger LOGGER = Logger.getLogger(Engine.class.getName());
	static {
		LOGGER.setLevel(Level.OFF);		
	}

	public enum ShapeType {
		L,J,O,S,Z,I,T
	}
	public int[][] getSwapBoard() {
		synchronized (accessLock) {
			int[][] retVal = new int[this.swapShapeGrid.length][this.swapShapeGrid[0].length];
			for (int row=0;row<retVal.length;row++) {
				for (int column=0;column<retVal[0].length;column++) {
					retVal[row][column] = this.swapShapeGrid[row][column].getColor();
				}
			}
			return retVal;
		}
	}
	public int[][] getGameDisplayBoard () {
		return getGameBoard(true);
	}
	public int[][] getGameBoard () {
		return getGameBoard(false);
	}
	private int[][] getGameBoard(boolean withShadow) {
		synchronized (accessLock) {
			boolean useShadow = false;
			if (this.dropShadow) useShadow = true; 
			int[][] retVal = new int[this.rows][this.columns];
			if (useShadow) {
				for (int row=0;row<this.rows;row++) {
					for (int column=0;column<this.columns;column++) {				
						int color = this.gameSpaceArray[row][column].getColor();
						if (color == EMPTYCOLOR) {
							if (withShadow && this.gameSpaceArray[row][column].getShadow()) retVal[row][column] = SHADOWCOLOR;
							else retVal[row][column] = EMPTYCOLOR;
						}
						else retVal[row][column] = color;
					}
				}
			} else {
				for (int row=0;row<this.rows;row++) {
					for (int column=0;column<this.columns;column++) {				
						retVal[row][column] = this.gameSpaceArray[row][column].getColor();
					}
				}
			}
			int [][] rr = retVal.clone ();
			return rr;
		}
	}
	public int[][] getNextShapeBoard() {
		synchronized (accessLock) {
			int[][] retVal = new int[this.rows][this.columns];
			for (int row=0;row<this.nextShapeGrid.length;row++) {
				for (int column=0;column<this.nextShapeGrid[0].length;column++) {
					retVal[row][column] = this.nextShapeGrid[row][column].getColor();
				}
			}
			return retVal;
		}
	}
	public int[][] getCoordsOfCurrentShape() {
		synchronized (accessLock) {
			int[][] coords = new int[4][2];
			if (this.currentShape != null) {
				Space[] spaces = this.currentShape.getSpaces();
				for (int sp=0;sp<spaces.length;sp++) {
					int[] c = spaces[sp].getCoords().clone();
					coords[sp] = new int[] {c[0],c[1]};
				}	
			}
			return coords;
		}
	}
	public void requestNextShape(SHAPETYPE type) {
		if (type == SHAPETYPE.NONE) return;
		synchronized (accessLock) {
			if (this.nextShapeRequest == null) {
				this.nextShapeRequest = type;
			}
		}
	}
	public void colorSpace(int r,int c) {
		colorSpace(r,c,4);
	}
	public void colorSpace(int r,int c,int color) {
		synchronized (accessLock) {
			if (this.gameSpaceArray == null) {
				return;
			}
			if (r>this.gameSpaceArray.length || c > this.gameSpaceArray[0].length || c < 0 || r < 0) {
				LOGGER.severe("ERROR: in engine attempted to color space with bad coordinates");
			}
			if (this.gameSpaceArray[r][c].getColor() != EMPTYCOLOR && this.gameSpaceArray[r][c].getColor() != SHADOWCOLOR) {
				this.gameSpaceArray[r][c].setColor(EMPTYCOLOR);
			} else {
				this.gameSpaceArray[r][c].setColor(color);
			}
			this.callBackMessenger.ringBell();
		}
	}
	public boolean gravityTick() {
		if(drop(false,true)) {

			newShape();
			return true;
		}
		return false;

	}
	public void enableDropShadow(boolean val) {
		synchronized (accessLock) {
			this.dropShadow = val;
			if (!val) {
				for (int row=0;row<this.rows;row++) {
					for (int col=0;col<this.columns;col++) {
						this.gameSpaceArray[row][col].setShadow(true);
					}
				}
			}
			this.callBackMessenger.ringBell();
		}
	}
	public boolean getDropShadowEnabled() {
		return this.dropShadow;
	}
	public boolean wasThereANewShape() {
		boolean val = this.wasThereNewShape;
		this.wasThereNewShape = false;
		return val;
	}
	public int getLinesCleared() {
		synchronized (accessLock) {
			return this.linesCleared;
		}
	}
	public void plummit() {
		synchronized (accessLock) {
			while(!this.drop(true,false));
		}
		this.callBackMessenger.ringBell();		
	}
	public boolean forceRotateCW() {
		synchronized (accessLock) {
			boolean res = !this.currentShape.rotateForward();
			this.setDropShadow();
			this.callBackMessenger.ringBell();
			return res;
		}
	}
	public boolean rotateClockwise() {
		synchronized (accessLock) {
			this.holdDrops = true;
			boolean result = this.currentShape.rotateForward();
			this.wasThereNewShape = false;
			this.setDropShadow();
			this.callBackMessenger.ringBell();
			this.holdDrops = false;
			return !result;
		}
	}
	public boolean rotateCounterClockwise() {
		synchronized (accessLock) {
			this.holdDrops = true;
			boolean result = this.currentShape.rotateBackward();
			this.wasThereNewShape = false;
			this.setDropShadow();
			this.callBackMessenger.ringBell();
			this.holdDrops = false;
			return !result;
		}
	}
	public boolean forceShiftLeft() {
		boolean res = this.currentShape.shiftLeft();
		this.setDropShadow();
		this.callBackMessenger.ringBell();
		return res;
	}
	public boolean forceShiftRight() {
		boolean res = this.currentShape.shiftRight();
		this.setDropShadow();
		this.callBackMessenger.ringBell();
		return res;
	}
	public boolean shiftLeft() {
		synchronized (accessLock) {
			this.holdDrops = true;
			if (this.dropDone) return false;
			boolean result = this.currentShape.shiftLeft();
			this.wasThereNewShape = false;
			this.setDropShadow();
			this.callBackMessenger.ringBell();
			this.holdDrops = false;
			return result;
		}
	}
	public boolean shiftRight() {
		synchronized (accessLock) {
			this.holdDrops = true;
			if (this.dropDone) return false;
			boolean result = this.currentShape.shiftRight();
			this.wasThereNewShape = false;
			this.setDropShadow();
			this.callBackMessenger.ringBell();
			this.holdDrops = false;
			return result;
		}
	}
	public boolean dropShape() { // for the interface
		synchronized (accessLock) {
			boolean result = drop(true , true);
			this.callBackMessenger.ringBell();
			return result;
		}
	}
	private boolean drop(boolean frominside,boolean pushUpdate) { //returns true if collision, or done drop
		if (this.pause && !frominside) return false;

		if (this.currentShape == null || this.swappedShape == null || this.nextShape == null) return true; // needed to initialize the engine properly
		this.holdDrops = true;
		boolean result = this.currentShape.drop();	
		if (!frominside) this.wasThereNewShape = false;
		if (result) {
			//LOGGER.info("Drop Done in Engine");
			this.dropDone = true;
			this.manageFullRows();
			newShape();
			this.holdDrops = false;
			return frominside;
		} else this.setDropShadow();
		this.holdDrops = false;
		if (pushUpdate) this.callBackMessenger.ringBell();
		return result;
	}
	public boolean isGameLost() {
		return this.gameOver;
	}
	public SHAPETYPE getCurrentShape() {
		return this.convertShapeToShapeType(this.currentShape);
	}
	public SHAPETYPE getNextShape() {
		return this.convertShapeToShapeType(this.nextShape);
	}
	public SHAPETYPE getSwapShape() {
		return this.convertShapeToShapeType(this.swappedShape);
	}
	public void setGravity(int time_milliseconds) {

		if (time_milliseconds < 0) return; // no anti-gravity
		if (this.gravity.getGravity() == 0) {
			this.startDropThread(time_milliseconds);
		} else {
			this.gravity.setDelay(time_milliseconds);
		}
	}
	public int getGravity() {
		return this.gravity.getGravity();
	}
	public boolean checkNewShape() {
		return this.wasThereNewShape;
	}
	public void impossible() {
		this.impossibleMode = !this.impossibleMode;
		if (this.impossibleMode) {
			this.gravity.setDelay(400);
		}
		else {
			this.gravity.setDelay(800);
		}
	}
	public boolean swapShapes() {  		// returns true if succeeded
		synchronized (accessLock) {
			if (this.swapUsed) return false;
			this.swapShapes = true;
			this.currentShape.delete();
			this.newShape();
			this.callBackMessenger.ringBell();
			return true;
		}
	}
	public void pause() {
		synchronized (accessLock) {
			this.pause = !this.pause;
		}
	}
	public boolean isPaused() {
		return this.pause;
	}
	private void manageFullRows(){
		synchronized (accessLock) {
			boolean found = false;
			int[] startRow = new int[] {-1,-1,-1,-1};		// 4 cause the max number of full rows does not exceed 4 (height of tallest shape)
			int numFound = 0;
			for (int row=0;row<this.rows;row++) {
				int num = 0;
				for (int column=0;column<this.columns;column++) {
					if (this.gameSpaceArray[row][column].getColor() != EMPTYCOLOR) {
						num++;
					}
				}
				if (num == this.columns) {
					found = true;
					numFound++;
					for (int i=0;i<startRow.length;i++) {
						if (startRow[i] < 0) {
							startRow[i] = row;
						}
					}
					for (int column=0;column<this.columns;column++) {
						this.gameSpaceArray[row][column].setColor(EMPTYCOLOR);
					}
				}
			}
			if (found) {
				this.linesCleared += numFound;

				for (int i=0;i<numFound;i++) {
					for (int row=this.rows-1;row>-1;row--) {
						int num =0;
						for (int column=0;column<this.columns;column++) {
							if (this.gameSpaceArray[row][column].getColor() == EMPTYCOLOR) {
								num++;
							}
						}
						if (num == this.columns) {				
							for (int subRow=row;subRow>0;subRow--) {
								for (int subColumn=0;subColumn<this.columns;subColumn++) {
									if (this.gameSpaceArray[subRow-1][subColumn].getColor() != EMPTYCOLOR) {
										this.gameSpaceArray[subRow][subColumn].setColor(this.gameSpaceArray[subRow-1][subColumn].getColor());
										this.gameSpaceArray[subRow-1][subColumn].setColor(EMPTYCOLOR);
									}
								}
							}
							break;
						}

					}
				}
			}
			for (int row=0;row<this.rows;row++) {
				for (int col=0;col<this.columns;col++) {
					this.gameSpaceArray[row][col].setShadow(false);
				}
			}
		}
	}
	public SHAPETYPE convertShapeToShapeType(Shape shape) {
		if (shape != null) {
			String shapeClass = shape.getClass().getSimpleName();
			switch (shapeClass) {
			case "LeftL" : {
				return SHAPETYPE.J;
			}
			case "RightL" : {
				return SHAPETYPE.L;
			}
			case "LeftS" : { 
				return SHAPETYPE.Z;
			}
			case "RightS" : {
				return SHAPETYPE.S;
			}
			case "Square" : {
				return SHAPETYPE.O;
			}
			case "Straight" : {
				return SHAPETYPE.I;
			}
			case "Tee" : {
				return SHAPETYPE.T;
			}
			}
		} 
		return SHAPETYPE.NONE;
	}
	private void setDropShadow() { 
		if (this.currentShape == null) {
			return;
		}
		for (Space[] row : this.gameSpaceArray) {
			for (Space sp : row) {
				if (sp.getShadow()) {
					sp.setShadow(false);;
				}
			}
		}
		Space[] curShapeSpaces = this.currentShape.getSpaces();
		int minCol = this.columns;
		int maxCol = 0; // represents distance from left (usual GUI coordinate orientation)
		int minRow = this.rows; // used to help place the shadow
		int maxRow = 0; // 0=top, represents distance from top 		
		for (Space sp : curShapeSpaces) {
			if (sp.getCoords()[1] < minCol) minCol = sp.getCoords()[1];
			if (sp.getCoords()[1] > maxCol) maxCol = sp.getCoords()[1];
			if (sp.getCoords()[0] < minRow) minRow = sp.getCoords()[0];
			if (sp.getCoords()[0] > maxRow) maxRow = sp.getCoords()[0];
		}
		int displacement = this.rows - maxRow - 1;
		if (maxRow != this.rows-1) { // so a shape will stay at the bottom after a drop
			FindLowestRow : {
			int[][] shapeCoords = new int[4][2];
			for (int shape=0;shape<curShapeSpaces.length;shape++) {
				shapeCoords[shape] = curShapeSpaces[shape].getCoords();
			}
			if (maxRow>=minRow) {
				for (int disp=2;(disp+maxRow)<this.rows;disp++) {
					for (Space sp : curShapeSpaces) {
						int row = sp.getCoords()[0]+disp;
						int col = sp.getCoords()[1];							
						int color = this.gameSpaceArray[sp.getCoords()[0]+disp][sp.getCoords()[1]].getColor();
						if (color != EMPTYCOLOR) {
							boolean itsOurs = false;
							for (int space=0;space<curShapeSpaces.length;space++) {
								if (curShapeSpaces[space].getCoords()[0] == row && curShapeSpaces[space].getCoords()[1] == col) {
									itsOurs = true;
									break;
								}
							}
							if (itsOurs) continue;
							else {
								displacement = disp - 1;
								break FindLowestRow;
							}
						} 
					}
				}
			} else LOGGER.severe("MAX ROW LESS THAN MIN ROW!");			
		}
		if(displacement > maxRow-minRow+1) {
			for (Space sp : curShapeSpaces) {						
				this.gameSpaceArray[sp.getCoords()[0]+displacement][sp.getCoords()[1]].setShadow(true);
			}
		}
		}
	}
	private void startDropThread(int initialGravity) {
		Gravity.start(initialGravity,this);				
	}
	protected void connectGravity(Gravity g) {
		this.gravity = g;
	}
	public void printShapeStats () {
		if (this.shapeStats != null) {
			HashMap<ShapeType, Double> stats = shapeStats.getStats();
			System.out.println("Shape Stats: for: " + this.shapeStats.getHistoryLength() + " shapes:");
			System.out.println("\tTYPE\tPERCENTAGE");
			for (Map.Entry<Engine.ShapeType, Double> entry : stats.entrySet()) {
				System.out.println("\t" + entry.getKey() + "\t" + String.format("%.2f",entry.getValue()*100) + "%");
			}
		}
	}
	private void newShape() {
		if (this.isGameLost()) return;
		LOGGER.info("NEW Shape");
		this.holdDrops = true;		
		synchronized (accessLock) {
			for (Space sp : this.startSpaces) {
				if (sp.getColor() != EMPTYCOLOR) {
					
					//printShapeStats ();
					this.gameOver = true;
					this.holdDrops = false;	
					LOGGER.warning("GAME OVER");
					this.wasThereNewShape = true;
					return; // so the last lines of this method are still executed on gameover event
				}
			}
			numShapes++;

			if (numShapes % 101 == 0) {
				//printShapeStats ();
			}
			if (this.swappedShape == null) {
				LOGGER.info("Generating swap shape...");
				this.swapUsed = false;
				this.currentShape = this.drawShape(this.gameSpaceArray, this.chooseShape());
				this.unDrawOnSwapShapeGrid();
				this.unDrawOnNewShapeGrid();
				this.swappedShape = drawShape(this.swapShapeGrid, this.chooseShape());
				this.nextShape = drawShape(this.nextShapeGrid, this.chooseShape());
			}
			else if (this.swapShapes){
				LOGGER.info("Swapping");
				this.swapUsed = true;
				String auxShapeClass = this.swappedShape.getClass().getSimpleName();
				String currentShapeClass = this.currentShape.getClass().getSimpleName();
				switch (auxShapeClass) {
				case "LeftL" : {
					this.currentShape = new LeftL(this.gameSpaceArray);
					break;
				}
				case "RightL" : {
					this.currentShape = new RightL(this.gameSpaceArray);
					break;
				}
				case "LeftS" : { 
					this.currentShape = new LeftS(this.gameSpaceArray);
					break;
				}
				case "RightS" : {
					this.currentShape = new RightS(this.gameSpaceArray);
					break;
				}
				case "Square" : {
					this.currentShape = new Square(this.gameSpaceArray);
					break;
				}
				case "Straight" : {
					this.currentShape = new Straight(this.gameSpaceArray);
					break;
				}
				case "Tee" : {
					this.currentShape = new Tee(this.gameSpaceArray);
					break;
				}
				}
				this.unDrawOnSwapShapeGrid();
				switch (currentShapeClass) {
				case "LeftL" : {
					this.swappedShape = new LeftL(this.swapShapeGrid);
					break;
				}
				case "RightL" : {
					this.swappedShape = new RightL(this.swapShapeGrid);
					break;
				}
				case "LeftS" : { 
					this.swappedShape = new LeftS(this.swapShapeGrid);
					break;
				}
				case "RightS" : {
					this.swappedShape = new RightS(this.swapShapeGrid);
					break;
				}
				case "Square" : {
					this.swappedShape = new Square(this.swapShapeGrid);
					break;
				}
				case "Straight" : {
					this.swappedShape = new Straight(this.swapShapeGrid);
					break;
				}
				case "Tee" : {
					this.swappedShape = new Tee(this.swapShapeGrid);
					break;
				}
				default : LOGGER.severe("ERROR: Could not determine name of swap shape!");
				}
				this.swapShapes = false;
			}
			else if (!this.swapShapes) {
				LOGGER.info("Getting New shape...");
				this.swapUsed = false;
				/*TODO This is the worst kind of code. Must fix it...*/
				String nextShapeClass = this.nextShape.getClass().getSimpleName();
				switch (nextShapeClass) {
				case "LeftL" : {
					this.currentShape = new LeftL(this.gameSpaceArray);
					break;
				}
				case "RightL" : {
					this.currentShape = new RightL(this.gameSpaceArray);
					break;
				}
				case "LeftS" : { 
					this.currentShape = new LeftS(this.gameSpaceArray);
					break;
				}
				case "RightS" : {
					this.currentShape = new RightS(this.gameSpaceArray);
					break;
				}
				case "Square" : {
					this.currentShape = new Square(this.gameSpaceArray);
					break;
				}
				case "Straight" : {
					this.currentShape = new Straight(this.gameSpaceArray);
					break;
				}
				case "Tee" : {
					this.currentShape = new Tee(this.gameSpaceArray);
					break;
				}
				default : LOGGER.severe("ERROR: Could not determine next shape!");
				}
				this.unDrawOnNewShapeGrid();
				this.nextShape = drawShape(this.nextShapeGrid, this.chooseShape()); 
				this.swapShapes = false;
			}
			if (this.currentShape!=null)shapeStats.addShape(this.currentShape.getType());
			this.holdDrops = false;
			this.wasThereNewShape = true;
			this.callBackMessenger.newShape();
			this.setDropShadow();
		}
	}
	private void unDrawOnNewShapeGrid() {
		for (int row=0;row<this.nextShapeGrid.length;row++) {
			for (int column=0;column<this.nextShapeGrid[0].length;column++) {
				this.nextShapeGrid[row][column].setColor(EMPTYCOLOR);
			}
		}
		this.callBackMessenger.ringBell();
	}
	private void unDrawOnSwapShapeGrid() {
		for (int row=0;row<this.swapShapeGrid.length;row++) {
			for (int column=0;column<this.swapShapeGrid[0].length;column++) {
				this.swapShapeGrid[row][column].setColor(EMPTYCOLOR);
			}
		}
		this.callBackMessenger.ringBell();
	}
	private int chooseShape() {
		if (this.nextShapeRequest != null) {
			int result = this.nextShapeRequest.toInt();
			this.nextShapeRequest = null;
			return result;
			//			ShapeType shape = this.nextShapeRequest;
			//			this.nextShapeRequest = null;
			//			switch (shape) {			
			//			case L : return 3;
			//			case J : return 0;
			//			case I : return 2;
			//			case S : return 5;
			//			case Z : return 6;
			//			case O : return 1;
			//			case T : return 4;
			//			}
		}

		if (this.impossibleMode) {
			boolean random_bool = random.nextBoolean();
			if (random_bool) {
				this.dropDone = false;
				this.holdDrops = false;
				return 6;
			}
			else {
				this.dropDone = false;
				this.holdDrops = false;
				return 5;
			}
		}
		int choose = random.nextInt(7);
		if (choose == this.previousChosenShape) { // more pseudo-randomness
			if (random.nextInt(11) > 2) {
				return chooseShape();
			}
		}
		this.previousChosenShape = choose;
		return choose;
	}
	private Shape drawShape(Space[][] drawArray, int shape) {
		Shape theChosenOne = null;

		switch (shape) {
		case 0 : {
			theChosenOne = new LeftL(drawArray);		
			break;
		}
		case 1 : {
			theChosenOne = new Square(drawArray);
			break;
		}
		case 2 : {
			theChosenOne = new Straight(drawArray);
			break;
		}
		case 3 : {
			theChosenOne = new RightL(drawArray);
			break;
		}
		case 4 : { 
			theChosenOne = new Tee(drawArray);
			break;
		}
		case 5 : { 
			theChosenOne = new RightS(drawArray);
			break;
		}
		case 6 : { 
			theChosenOne = new LeftS(drawArray);
			break;
		}
		}
		this.dropDone = false;
		this.holdDrops = false;
		return theChosenOne;
	}
	private Space[] getStartSpaces(Space[][] gameBoard) { // assumes a min height of 2  and min width of 4
		int columnStart = (gameBoard [0].length / 2) - 2;
		/*  Game boards with odd numbers of columns will spawn shapes slightly to the left of center, due to the
		 *  even number of spaces required to spawn a shape */		 
		return new Space[] {	
				gameBoard[0][columnStart], gameBoard[0][columnStart+1],	gameBoard[0][columnStart+2], gameBoard[0][columnStart+3],
				gameBoard[1][columnStart], gameBoard[1][columnStart+1],	gameBoard[1][columnStart+2], gameBoard[1][columnStart+3]
		};
	}
	public Engine(int rows, int columns, int gravity,CallBack callBackMessenger) {
		this.impossibleMode = false;
		this.pause = false;
		this.previousChosenShape = -1;
		this.swapUsed = false;
		this.holdDrops = false;
		this.rows = rows;
		this.columns = columns;
		this.swapShapes = false;
		this.swapShapeGrid = new Space[4][4];
		this.nextShapeGrid = new Space[4][4];
		this.gameSpaceArray = new Space[this.rows][this.columns];
		this.shapeBuffer = new Shape[2];
		for (int row=0;row<this.swapShapeGrid.length;row++){
			for (int column=0;column<this.swapShapeGrid[row].length;column++) {
				this.swapShapeGrid[row][column] = new Space(row,column,EMPTYCOLOR);
			}
		}
		for (int row=0;row<this.nextShapeGrid.length;row++){
			for (int column=0;column<this.nextShapeGrid[row].length;column++) {
				this.nextShapeGrid[row][column] = new Space(row,column,EMPTYCOLOR);
			}
		}
		for (int row=0;row<this.rows;row++) {
			for (int column=0;column<this.columns;column++) {
				this.gameSpaceArray[row][column] = new Space(row,column,EMPTYCOLOR);
			}
		}
		this.startSpaces = this.getStartSpaces(this.gameSpaceArray);
		this.linesCleared = 0;
		this.callBackMessenger = callBackMessenger;
		this.startDropThread(gravity);

	}


}
final class Gravity {
	private Timer timer;
	private int delay;
	private boolean tryingToDrop = true;
	private boolean easySpin = false;
	public static void start(int gravity, Engine inTetris) {
		final Engine tetris = inTetris;

		final int G = gravity;
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					new Gravity(G,tetris);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

	}
	public void setEasySpin(boolean val) {
		this.easySpin = val;
	}
	public boolean getEasySpin() {
		return this.easySpin;
	}
	public void setDelay(int time) {
		boolean doEasySpin = false;
		if (this.easySpin) doEasySpin = true;
		if (doEasySpin) this.timer.stop();
		this.timer.setDelay(time);
		if (doEasySpin) this.timer.start();
	}
	public int getGravity() {
		return this.delay;
	}
	public boolean needToDrop() {
		return tryingToDrop;
	}
	private void setDropRequestStatus(boolean status) {
		tryingToDrop = status;
	}
	public Gravity(int delay,Engine inTetris) {
		final Engine tetris = inTetris;
		inTetris.connectGravity(this);
		this.delay = delay;
		if (delay != 0) { // to allow no gravity
			ActionListener taskPerformer = new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					setDropRequestStatus(true);
					tetris.gravityTick();
					setDropRequestStatus(false);
				}
			};
			this.timer = new Timer(delay, taskPerformer);
			this.timer.start();
		} else {
			while (tetris.gravityTick());
		}
	}
}

