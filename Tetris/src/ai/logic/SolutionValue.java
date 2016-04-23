package ai.logic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import ai.state.GameState;

public class SolutionValue {
	
	/** AC = config[0];  ==> Adjacent Count 		**/
	/** MB = config[1];  ==> Missing Below  		**/
	/** EC = config[2];  ==> Edge Count     		**/
	/** DG = config[3];  ==> Distance From Bottom 	**/
	/** RC = config[4];  ==> Rows Cleared   		**/ 
	/** RD = config[5];  ==> Density				**/
	/** BF = config[6];  ==> Buried Factor  		**/
	/** BG = config[7];  ==> Bad Gap Beside Shape	**/
	/** CF = config[8];  ==> Covered Factor			**/
	/** PS = config[9];  ==> ProximityToSpawn			**/
	
	private boolean configLoaded = false;
	protected double AC, MB, EC, DG, RC, DS, BF, BG, CF, PS;
	//protected static double[] defaults = new double[] {10,10,10,1, 2.5, 3, 1,2.5, 2.};
	//protected static double[] defaults = new double[] {10,20,8,1, 2.5, 3, 1,4,0};
	//protected static double[] defaults = new double[] {10,11,10,0, 1, 0, 0,0,0};
	//protected static double[] defaults = new double[] {10,300,10,1, 2.5, 3, 1,5,0};
	//protected static double[] defaults = new double[] {20,30,21,0,430000, 0, 0,0,1, 0}; //Holly cow 95 000
	
	//protected static double[] defaults = new double[] {20,30,21,0,430000, 0, 0,0,1, 0};
//	protected static double[] defaults = new double[] {20,30,21,0, 4300, 0, 0, 2,1,0}; //36000
	//protected static double[] defaults = new double[] {1,1,1,1, 5, 0, 0, 1,0,0}; 
	
	//protected static double[] defaults = new double[] {10,12,10,3, 15, 0, 0,0,0,0}; //36000, not 36 000
	//protected static double[] defaults = new double[] {15,16,11,0, 100, 3, 0,1,1,2}; //56000
	//protected static double[] defaults = new double[] {1000,0,1050,10, 155, 0, 0,0,0}; //56000
	//protected static double[] defaults = new double[] {15,30,11,2, 20, 2, 0,0,2,5}; // Jan 3 2016
	//protected static double[] defaults = new double[] {15,46000000000d,2000,3, 8, 1, 1,1,1,1};
	protected static double[] defaults = new double[] {300, 3000, 100, 0, 100, 0,0,0,0,0}; // 82518 lines on Feb 1, 2016
	//protected static double[] defaults = new double[] {15,30,15,0, 100, 3, 0,0,1,2};
	//protected static double[] defaults = new double[] {10,11,10,3, 20, 0, 0,0,0,0}; //20 000
//	protected static double[] defaults = new double[] {10,11,10,0,10,0,0,4,4}; //Holly cow 95 000
//	protected static double[] defaults = new double[] {10,12,11,1, 10, 3, 1,5,0,0}; //Holly cow 95 000
	//protected static double[] defaults = new double[] {4,9,5,1, 2, 0, 0,1};
	//protected static double[] defaults = new double[] {5,5,65,0,1,0 , 1,0};	
	//protected static double[] defaults = new double[] {10,10,8,1,0,0 , 1,0};	
	private final static Logger LOGGER = Logger.getLogger(SolutionValue.class.getName());
	
	private InternalGameBoard gBWithShape, gBWithoutShape, shapeCoords;
	
	static {
		LOGGER.setLevel(Level.OFF);	
		//loadConfiguration(null);
		//LOGGER.setLevel(Level.SEVERE);	
	}
	public SolutionValue () {
		Weights weights = new Weights();
		defaults = Arrays.copyOf(weights.getConfig(), defaults.length);
		loadConfiguration("");
	}
	public SolutionValue (Weights weights) {
		defaults = Arrays.copyOf(weights.getConfig(), defaults.length);
		loadConfiguration("");
	}
	protected void loadConfiguration(String configFile) {		// loads the weightings into the decision function, to be used for rapid/automated algorithm improvement
		double[] config = Arrays.copyOf(defaults, defaults.length);
		if (configFile != null || configFile != "") {			
			File file = new File(configFile);
			if (file.exists()) {			
				try {
					BufferedReader input = new BufferedReader(new FileReader(file));
					int i = 0;
					for (i=0;i<config.length;i++) {
						String line = input.readLine();
						int commentStart = line.indexOf("#");			// need to strip the comments in the config file

						if (commentStart >= 0) line = line.substring(0, commentStart);
						line = line.trim();

						try {
							config[i] = Double.parseDouble(line);
						}	catch (NumberFormatException e) {
							LOGGER.severe("Invalid value in config file: could not parse double: " + line);
							LOGGER.severe("Defaults will be loaded instead");
							config = SolutionValue.defaults.clone();
							break;
						} catch (NullPointerException e2) {
							LOGGER.severe("Internal error occured while trying to parse double (was null): " + line);
							LOGGER.severe("Defaults will be loaded instead");
							config = SolutionValue.defaults.clone();
							break;
						}
					}	       
					if (i != config.length) {
						LOGGER.severe("Not enough arguments were found in the config file!");
					}
					input.close();
				} catch ( IOException e ) {
					e.printStackTrace();
				}
			}
			else {
				LOGGER.severe("Configuration file " + configFile + " not found, defaults will be used instead");
			}
		}
		try {
			AC = config[0];  /** Adjacent Count 		**/
			MB = config[1];  /** Missing Below  		**/
			EC = config[2];  /** Edge Count     		**/
			DG = config[3];  /** Distance From Bottom 	**/
			RC = config[4];  /** Rows Cleared   		**/ 
			DS = config[5];  /** Density		**/
			BF = config[6];  /** Buried Factor  		**/
			BG = config[7];  /** Bad Gap Beside Shape	**/
			CF = config[8];	 /** Covered Factor			**/
			PS = config[9];	 /** Proximity To Spawn		**/
		} catch (Exception e) {			// likely either ArrayIndexOutOfBounds or NullPointer Exceptions
			LOGGER.severe("Could not load optimized configuration into AI, using equal weightings... good luck");
			AC = 1.0;
			MB = 1.0;
			EC = 1.0;
			DG = 1.0;
			RC = 1.0;
			DS = 1.0;
			BF = 1.0;
			BG = 1.0;
			CF = 1.0;
			PS = 1.0;
		}
		LOGGER.info("AC : " + AC);
		LOGGER.info("MB : " + MB);
		LOGGER.info("EC : " + EC);
		LOGGER.info("DG : " + DG);
		LOGGER.info("RC : " + RC);
		LOGGER.info("DS : " + DS);
		LOGGER.info("BF : " + BF); 
		LOGGER.info("BG : " + BG); 
		configLoaded = true;
	}
	public double calculateSolution(GameState inState, ComputedValue values) {
		return calculateSolution(values);
	}

	public double calculateSolution(ComputedValue values) {
		if (values == null) return Double.NEGATIVE_INFINITY;
		double val = 0d;
		
		val += AC*values.adjacentCount; 
		val -= MB*values.missingBelow;
		val += EC*values.edgeCount;
		val += Math.pow(RC,values.numRowsThatWillClear);
		val -= BF*values.buriedFactor; 
		val -= BG*values.badGapBesideShape; 
		val += CF*values.coveredFactor;
		val += PS*values.proximityToSpawn;
		
		return val;
	}
	private double getDensity (GameState inState) {
		int [][] gB = inState.getBoardWithCurrentShape().getState();
		
		int totalSpacesFilled = 0;
		int totalSpacesChecked = 0;
		for (int row=0;row<gB.length; row++) {
			int numFilled = 0;  // Per row
			for (int col=0;col<gB[row].length;col++) {
				if (gB[row][col] != 0) numFilled++;
			}
			
			if (numFilled > 0) {
				totalSpacesFilled += numFilled;
				totalSpacesChecked += gB[row].length; 
			}
			
		}
		return totalSpacesFilled / ((double) totalSpacesChecked); 
	}
	public ComputedValue getSolutionParameters(GameState inState) {
		gBWithShape = new InternalGameBoard (inState.getBoardWithCurrentShape().getState());
		gBWithoutShape = new InternalGameBoard (inState.getBoardWithoutCurrentShape().getState());
		shapeCoords = new InternalGameBoard (inState.getShape().getCoords());
		
		ComputedValue values = new ComputedValue ();
		if (AC != 0d) values.adjacentCount = getAdjacentCount(inState);
		if (BF != 0d) values.buriedFactor  = getBurriedFactor(inState);
		if (CF != 0d) values.coveredFactor = getCoveredFactor (inState);
		if (DG != 0d) values.distanceFromBottom = getLowestShapePoint (inState);
		if (BG != 0d) values.badGapBesideShape = getBadGapBesideShape();
		if (MB != 0d) values.missingBelow = getSimpleMissingBelow ();
		if (EC != 0d) values.edgeCount = getEdgeCount (inState);
		if (DS != 0d) values.density = getDensity(inState);
		if (RC != 0d) values.numRowsThatWillClear = getNumRowsThatWillClear(inState);
		if (PS != 0d) values.proximityToSpawn = getProximityToSpawn(inState);
		
		return values;
	}
	public double getProximityToSpawn (GameState inState) {
		/* Accounts for engine variability in 
		 * how the engine centers a shape with odd number of columns
		 */
		final int nCols = Math.max(inState.numColumns(),1);
		int spawnColStart = Math.max((nCols / 2) - 2,0); 
		int spawnColEnd = Math.min((nCols / 2) + 1, nCols-1);
		int spawnRowStart = 0;
		int spawnRowEnd = 1;
		
		double minProximity = Double.POSITIVE_INFINITY;
		
		for (int [] coord: inState.getShape().getCoords()) {
			double colDiff = 0d;
			if (coord[1] < spawnColStart) colDiff = spawnColStart - coord[1];
			else if (coord[1] > spawnColEnd) colDiff = coord[1] - spawnColEnd;
			else colDiff = Math.abs((nCols/2) - coord[1]);
			
			double rowDiff = 0d;
			if (coord[0] > spawnRowEnd) rowDiff = coord[0] - spawnRowEnd;
			else if (coord[0] < spawnRowStart) rowDiff = spawnRowStart - coord[0];
			else rowDiff = Math.abs((spawnRowEnd - spawnRowStart) - coord[0]);
			
			
			double thisProximity = Math.sqrt(colDiff*colDiff + rowDiff*rowDiff);
			minProximity = Math.min(minProximity,  thisProximity);
		}
		return minProximity;
	}
	private double packingValue (GameState inState) {
		double factor = 0d;
		final int nCols = inState.numColumns();
		final int nRows = inState.numRows();
		
		int [][] gBWith = inState.getBoardWithCurrentShape().getState();
		int [][] gBWOut = inState.getBoardWithoutCurrentShape().getState();
		
		int [] rowFillBef = new int [nRows];
		int [] rowFillAft = new int [nRows];
		
		for (int row=0;row<nRows;row++) {
			for (int col=0;col<nCols;col++) {
				if (gBWith[row][col] != 0) rowFillAft[row]++;
				if (gBWOut[row][col] != 0) rowFillBef[row]++;
			}
			
			if (rowFillBef[row] == 0 && rowFillAft[row] != 0) factor+=rowFillAft[row];
		}
		return factor;
	}
	private int getNumRowsThatWillClear (GameState inState) {
		int [][] gB = inState.getBoardWithCurrentShape().getState();
		int clearCount = 0;
		final int nRows = inState.numRows();
		final int nCols = inState.numColumns();
		for (int row=0;row<nRows;row++) {
			int rowCount = 0;
			for (int col=0;col<nCols;col++) {
				if (gB[row][col] != 0) rowCount ++;
			}
			if (rowCount == nCols) clearCount++;
		}
		return clearCount;
	}
	private int getLowestShapePoint (GameState inState) {
		int lowestRow = 0;
		for (int [] coord: inState.getShape().getCoords()) {
			lowestRow = Math.max(lowestRow,  coord[0]);
		}
		return lowestRow;
	}
	private int getSimpleMissingBelow () {
		int count = 0;
		for (int coord=0; coord<4;coord++) {
			if (gBWithShape.getWithCheck(shapeCoords.get(coord, 0)+1, shapeCoords.get(coord, 1)) == 0) count++;
		}
		return count;
	}
	private int getMissingBelow (GameState inState) {
		int [] rowsThatWillClear = getRowsThatWillClear (inState);
		int [][] gB = inState.getBoardWithCurrentShape().getState();
		int missingBelow = 0;

		for (int [] coord : inState.getShape().getCoords()) {
			int incrementAmount = (rowsThatWillClear[coord[0]] != 0) ? 1 : 2;
			if (inBounds(coord[0]+1,coord[1],inState) && gB [coord[0]+1][coord[1]] == 0) {		
				if (rowsThatWillClear[coord[0]] == 0) {
					boolean stillCovered = false;
					for (int [] otherCoord : inState.getShape().getCoords()) {
						if (otherCoord[0] > coord[0] && otherCoord[1] == coord[1]) stillCovered = true;
					}
					if (!stillCovered) missingBelow++;
				} else {
					missingBelow++;
				}
			}
			/*for (int row=coord[0]+1;row<gB.length;row++) {
				if (gB[row][coord[1]] == 0) {
					missingBelow += incrementAmount;
				} else break;
			}*/
			
			
		}
		return missingBelow;
	}
	private boolean inBounds (final int r, final int c, final GameState gState) {
		int maxRows = gState.numRows();
		int maxCols = gState.numColumns();
		if (r >= maxRows || c >= maxCols || (r | c) < 0) return false;
		return true;
	}
	private int getCoveredFactor (GameState inState) {
		int coveredCount = 0;
		int [][] gB = inState.getBoardWithoutCurrentShape().getState();
		for (int [] coord : inState.getShape().getCoords()) {
			if (inBounds (coord[0]-1,coord[1],inState) && gB[coord[0]-1][coord[1]] != 0) {
				coveredCount ++;
			}
		}
		return coveredCount;
	}
	private int getEdgeCount (GameState inState) {
		int count = 0;
		final int maxCol = inState.numColumns() - 1; // Cause array indexes
		final int maxRow = inState.numRows() - 1;
		for (int [] coord : inState.getShape().getCoords()) {
			if (coord[0] == maxRow || coord[0] == 0) count++;
			if (coord[1] == 0 || coord[1] == maxCol) count++; 
		}
		return count;
	}
	private int getBurriedFactor (GameState inState) {
		int gB [][] = inState.getBoardWithCurrentShape().getState();
		int buriedFactor = 0;
		int [] visitedColumns = new int [8];
		for (int [] shCoord : inState.getShape().getCoords()) {
			boolean skip = false;
			for (int i=0;i<visitedColumns.length;i+=2) {
				if (visitedColumns [i+1] == shCoord [1]) {
					//System.out.println ("Found a previously visited column");
					skip = true;
				}
			}
			if (skip) continue;
			else {
				for (int i=0;i<visitedColumns.length;i+=2) {
					if (visitedColumns [i] == 0) {
						visitedColumns [i] = 1;
						visitedColumns [i+1] = shCoord[1];
					}
				}
				int numSolid = 0;
				int numEmpty = 0;
				for (int row = shCoord[0]+2;row<gB.length;row++) {
					if (gB[row][shCoord[1]] == 0)
						numEmpty++;
				}
		//	for (int row = shCoord[0];row>=0;row--) {
			//	if (gB[row][shCoord[1]] != 0) numSolid++;
	//			else break;
		//	}
				buriedFactor += numEmpty;
			}
		}
		return buriedFactor;
	}
	private int getRoughness (GameState inState) {
		return getRoughness (inState, 0, inState.getBoardWithoutCurrentShape().getState()[0].length);
	}
	private int getAdjacentCount (GameState inState) {
		int count = 0;
		int [][] gB = inState.getBoardWithoutCurrentShape().getState();
		for (int [] coord : inState.getShape().getCoords()) {
			if (inBounds(coord[0]+1,coord[1],inState) && gB[coord[0]+1][coord[1]] != 0) {
				count++;
			}
			if (inBounds(coord[0]-1,coord[1],inState) && gB[coord[0]-1][coord[1]] != 0) {
				count++;
			}
			if (inBounds(coord[0],coord[1]+1,inState) && gB[coord[0]][coord[1]+1] != 0) {
				count++;
			}
			if (inBounds(coord[0],coord[1]-1,inState) && gB[coord[0]][coord[1]-1] != 0) {
				count++;
			}
		}
		return count;
	}
	private int [] getRowsThatWillClear (GameState gState) {
		//System.out.println("GameBoard Size: " + Arrays.toString(gState.getBoardSize()));
		//System.out.println ("AS Rows cleard saw it: " + GameState.dumpState(gState, false));
		int [] rowsThatWillClear = new int [gState.getBoardSize()[0]];
		for (int i=0;i<rowsThatWillClear.length;i++) {
			//System.out.println ("ROW: " + i + Arrays.toString(gState.getBoardWithCurrentShape().getState()[i]));
			rowsThatWillClear [i] = 1;
			for (int j=0;j<gState.getBoardSize()[1];j++) {
				if (gState.getBoardWithCurrentShape().getState()[i][j] == 0) {
					rowsThatWillClear [i] = 0;
					break;
				}
			}
		}
		return rowsThatWillClear;
	}
	private int getBadGapBesideShape () {
		int numBadGaps = 0;
		int rows = gBWithShape.getRows();
		int cols = gBWithShape.getCols();
		
		
		int countSinceLast = 0;
		for (int r=0;r<rows;r++) {
			for (int c=0;c<cols;c++) {
				if (gBWithShape.get(r, c) == 0) countSinceLast++;
				else {
					if (countSinceLast == 1) numBadGaps++;
					countSinceLast = 0;
				}
			}
			if (countSinceLast == 1) numBadGaps++;
			countSinceLast = 0;
		}
		return numBadGaps;
	}
	private int getRoughness (GameState inState, int startColumn, int endColumn) {
		int oldDepth = getDepthOfColumn (inState.getBoardWithCurrentShape().getState(), 0);
		int roughness = 0;
		final int maxCol = endColumn;
		return 0;
	}
	private int getDepthOfColumn (int [][] gameBoard, int column) {
		for (int r=0;r<gameBoard.length;r++) {				
			if (gameBoard[r][column] != 0) {
				return r;
			}
		}
		return gameBoard.length;
	}
	private class InternalGameBoard {
		final int [] board;
		final int rows, cols;
		public InternalGameBoard (int [][] sqBoard) {
			rows = sqBoard.length;
			cols = sqBoard[0].length;
			
			board = new int [rows*cols];
			
			int r,c;
			for (int i=0;i<board.length;i++) {
				r = i / cols;
				c = i % cols;
				
				board[i] = sqBoard[r][c];
			}
		}
		
		public final int getRows () {
			return rows;
		}
		public final int getCols () {
			return cols;
		}
		public final int getWithCheck (final int r, final int c) {
			if (r >= rows || c >= cols || (r | c) < 0) {
				return -1;
			} else {
				return get (r, c);
			}
		}
		public final int get (final int r, final int c) {
			return board[r*cols + c];
		}
	}
}
