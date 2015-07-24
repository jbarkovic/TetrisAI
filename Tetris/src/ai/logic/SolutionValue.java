package ai.logic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;

import ai.state.GameState;
import ai.state.SHAPETYPE;

public class SolutionValue {
	
	/** AC = config[0];  ==> Adjacent Count 		**/
	/** MB = config[1];  ==> Missing Below  		**/
	/** EC = config[2];  ==> Edge Count     		**/
	/** DG = config[3];  ==> Distance From Bottom 	**/
	/** RC = config[4];  ==> Rows Cleared   		**/ 
	/** RD = config[5];  ==> Roughness Delta		**/
	/** BF = config[6];  ==> Buried Factor  		**/
	/** BG = config[7];  ==> Bad Gap Beside Shape	**/
	/** CF = config[8];  ==> Covered Factor			**/
	/** PS = config[9];  ==> ProximityToSpawn			**/
	
	private static boolean configLoaded = false;
	protected static double AC, MB, EC, DG, RC, RD, BF, BG, CF, PS;
	//protected static double[] defaults = new double[] {10,10,10,1, 2.5, 3, 1,2.5, 2.};
	//protected static double[] defaults = new double[] {10,20,8,1, 2.5, 3, 1,4,0};
	//protected static double[] defaults = new double[] {10,11,10,0, 1, 0, 0,0,0};
	//protected static double[] defaults = new double[] {10,300,10,1, 2.5, 3, 1,5,0};
	protected static double[] defaults = new double[] {20,23,20,0,46.1, 0, 0,0,1, 0}; //Holly cow 95 000
	//protected static double[] defaults = new double[] {10,10,10,1, 1, 1, 1,1,0}; //Holly cow 95 000
//	protected static double[] defaults = new double[] {10,11,10,0,10,0,0,4,4}; //Holly cow 95 000
//	protected static double[] defaults = new double[] {10,12,11,1, 10, 3, 1,5,0}; //Holly cow 95 000
	//protected static double[] defaults = new double[] {4,9,5,1, 2, 0, 0,1};
	//protected static double[] defaults = new double[] {5,5,65,0,1,0 , 1,0};	
	//protected static double[] defaults = new double[] {10,10,8,1,0,0 , 1,0};	
	private final static Logger LOGGER = Logger.getLogger(SolutionMaster.class.getName());
	static {
		LOGGER.setLevel(Logger.getGlobal().getLevel());	
		//loadConfiguration(null);
		//LOGGER.setLevel(Level.SEVERE);	
	}
	protected static void loadConfiguration(String configFile) {		// loads the weightings into the decision function, to be used for rapid/automated algorithm improvement
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
			RD = config[5];  /** Roughness Delta		**/
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
			RD = 1.0;
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
		LOGGER.info("RD : " + RD);
		LOGGER.info("BF : " + BF); 
		LOGGER.info("BG : " + BG); 
		configLoaded = true;
	}

	public static double calculateSolution(GameState inState, ComputedValue values) {
		double val = -90000000;
		if (values.proximityToSpawn < 6.1d) {
			System.out.println("SolutionValue.java: Too close to spawn ["+values.proximityToSpawn+"] returning neg inf");
			val = Double.NEGATIVE_INFINITY;
		} else {
			val = 0d;
		}
		val += ( AC*values.adjacentCount -MB*values.missingBelow + EC*values.edgeCount + RC*values.numRowsThatWillClear - BF*values.buriedFactor - BG*values.badGapBesideShape + CF*values.coveredFactor);//);
		val += values.proximityToSpawn;
		return val;
	}
	public static ComputedValue getSolutionParameters(GameState inState) {
		ComputedValue values = new ComputedValue ();
		values.adjacentCount = getAdjacentCount(inState);
		values.buriedFactor  = getAdjacentCount(inState);
		values.coveredFactor = getCoveredFactor (inState);
		values.distanceFromBottom = getLowestShapePoint (inState);
		values.badGapBesideShape = getAdjacentCount(inState);
		values.missingBelow = getMissingBelow (inState);
		values.edgeCount = getEdgeCount (inState);
		values.roughness = 0;
		values.percentDown = 0;
		values.density = 0;
		values.numRowsThatWillClear = getNumRowsThatWillClear(inState);
		values.proximityToSpawn = getProximityToSpawn(inState);
		
		return values;
	}
	public static double getProximityToSpawn (GameState inState) {
		/* Accounts for engine variability in 
		 * how the engine centers a shape with odd number of columns
		 */
		final int nCols = Math.max(inState.numColumns(),1);
		final int nRows = Math.max(inState.numRows(),1);
		int spawnColStart = Math.max((nCols / 2) - 3,0); 
		int spawnColEnd = Math.min((nCols / 2) + 2, nCols-1);
		int spawnRowStart = 0;
		int spawnRowEnd = 1;
		
		double minProximity = Math.sqrt(Math.pow(nCols, 2) + Math.pow(nRows, 2));
		
		for (int [] coord: inState.getShape().getCoords()) {
			int colDiff = Math.min(Math.abs(coord[1]-spawnColStart), Math.abs(coord[1]-spawnColEnd));
			int rowDiff = Math.min(Math.abs(coord[0]-spawnRowStart), Math.abs(coord[0]-spawnRowEnd));
			
			double thisProximity = Math.sqrt(Math.pow(colDiff, 2) + Math.pow(rowDiff, 2));
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
	private static int getNumRowsThatWillClear (GameState inState) {
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
	private static int getLowestShapePoint (GameState inState) {
		int lowestRow = 0;
		for (int [] coord: inState.getShape().getCoords()) {
			lowestRow = Math.max(lowestRow,  coord[0]);
		}
		return lowestRow;
	}
	private static int getMissingBelow (GameState inState) {
		int [] rowsThatWillClear = getRowsThatWillClear (inState);
		int [][] gB = inState.getBoardWithCurrentShape().getState();
		int missingBelow = 0;

		for (int [] coord : inState.getShape().getCoords()) {
			int incrementAmount = (rowsThatWillClear[coord[0]] != 0) ? 1 : 2;
			/*if (inBounds(coord[0]+1,coord[1],inState) && gB [coord[0]+1][coord[1]] == 0) {		
				if (rowsThatWillClear[coord[0]] == 0) {
					boolean stillCovered = false;
					for (int [] otherCoord : inState.getShape().getCoords()) {
						if (otherCoord[0] > coord[0] && otherCoord[1] == coord[1]) stillCovered = true;
					}
					if (!stillCovered) missingBelow++;
				} else {
					missingBelow++;
				}
			}*/
			for (int row=coord[0]+1;row<gB.length;row++) {
				if (gB[row][coord[1]] == 0) {
					missingBelow += incrementAmount;
				} else break;
			}
			
			
		}
		return missingBelow;
	}
	private static boolean inBounds (final int r, final int c, final GameState gState) {
		int [] bounds = gState.getBoardSize();
		if (r >= bounds [0] || r < 0) return false;
		if (c >= bounds [1] || c < 0) return false;
		return true;
	}
	private static int getCoveredFactor (GameState inState) {
		int coveredCount = 0;
		int [][] gB = inState.getBoardWithoutCurrentShape().getState();
		for (int [] coord : inState.getShape().getCoords()) {
			if (inBounds (coord[0]-1,coord[1],inState) && gB[coord[0]-1][coord[1]] != 0) {
				coveredCount ++;
			}
		}
		return coveredCount;
	}
	private static int getDensity (GameState inState) {
		int density = 0;
		int numNonEmptyRows = 0;
		int [][] gB = inState.getBoardWithCurrentShape().getState();		
		for (int row=0;row<gB.length;row++) {
			int numFilledInRow = 0;
			for (int col=0;col<gB[0].length;col++) {
				if (gB[row][col] != 0) numFilledInRow++;
			}
			if (numFilledInRow > 0) {
				numNonEmptyRows++;
				density += numFilledInRow;
			}
		}
		density *= 1000;
		density /= (numNonEmptyRows * gB[0].length);
		return density;
	}
	private static int getEdgeCount (GameState inState) {
		int count = 0;
		final int maxCol = inState.numColumns() - 1; // Cause array indexes
		final int maxRow = inState.numRows() - 1;
		for (int [] coord : inState.getShape().getCoords()) {
			if (coord[0] == maxRow) count+= 1;
			if (coord[1] == 0 || coord[1] == maxCol) count++; 
		}
		return count;
	}
	private static int getBurriedFactor (GameState inState) {
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
	private static int getRoughness (GameState inState) {
		return getRoughness (inState, 0, inState.getBoardWithCurrentShape().getState()[0].length);
	}
	private static int getAdjacentCount (GameState inState) {
		//System.out.println ("AS Adjacent saw it");
		//inState.dumpState(inState, true);
		int adjacentCount = 0;
		int [][] gB = inState.getBoardWithoutCurrentShape().getState();
		for (int [] coord : inState.getShape().getCoords()) {
			for (int j=-1;j<2;j++) {
				try {
					if (gB [coord[0]+j][coord[1]] != 0) {
						adjacentCount ++;
					}
				} catch (ArrayIndexOutOfBoundsException e) {}
				try {
					if (gB [coord[0]][coord[1]+j] != 0) {
						adjacentCount ++;
					}
				} catch (ArrayIndexOutOfBoundsException e) {}
			}
		}
		return adjacentCount;
	}
	private static int [] getRowsThatWillClear (GameState gState) {
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
	private static int getBadGapBesideShape (GameState inState) {
		int numBadGaps = 0;
		int [][] gB = inState.getBoardWithCurrentShape().getState();
		for (int [] coord : inState.getShape().getCoords()) {
			if (inBounds(coord[0],coord[1]+1,inState) && gB[coord[0]][coord[1]+1] == 0) {
				if (inBounds(coord[0],coord[1]+2,inState)) {
					if (gB[coord[0]][coord[1]+2] > 0) {
						numBadGaps++;
					}
				} else numBadGaps++;
			}
			if (inBounds(coord[0],coord[1]-1,inState) && gB[coord[0]][coord[1]-1] == 0) {
				if (inBounds(coord[0],coord[1]-2,inState)) {
					if (gB[coord[0]][coord[1]-2] > 0) {
						numBadGaps++;
					}
				} else numBadGaps++;
			}
		}
		return numBadGaps;
	}
	private static int getRoughness (GameState inState, int startColumn, int endColumn) {
		int oldDepth = getDepthOfColumn (inState.getBoardWithCurrentShape().getState(), 0);
		int roughness = 0;
		final int maxCol = endColumn;
		return 0;
	}
	private static int getDepthOfColumn (int [][] gameBoard, int column) {
		for (int r=0;r<gameBoard.length;r++) {				
			if (gameBoard[r][column] != 0) {
				return r;
			}
		}
		return gameBoard.length;
	}
}
