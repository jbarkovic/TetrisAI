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
	
	private static boolean configLoaded = false;
	protected static double AC, MB, EC, DG, RC, RD, BF, BG, CF;
	//protected static double[] defaults = new double[] {10,10,10,1, 2.5, 3, 1,2.5, 2.};
	//protected static double[] defaults = new double[] {10,20,8,1, 2.5, 3, 1,4,0};
	//protected static double[] defaults = new double[] {10,11,10,0, 1, 0, 0,0,0};
	//protected static double[] defaults = new double[] {10,300,10,1, 2.5, 3, 1,5,0};
//	protected static double[] defaults = new double[] {10,15,11,1, 12, 0, 0,0,0}; //Holly cow 95 000
	//protected static double[] defaults = new double[] {10,10,10,1, 1, 1, 1,1,0}; //Holly cow 95 000
	protected static double[] defaults = new double[] {10,11,10,0,10,0,0,4,4}; //Holly cow 95 000
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
		} catch (Exception e) {			// likely either ArrayIndexOutOfBounds or NullPointer Exceptions
			LOGGER.severe("Could not load optimized configuration into AI, using equal weightings... god speed");
			AC = 1.0;
			MB = 1.0;
			EC = 1.0;
			DG = 1.0;
			RC = 1.0;
			RD = 1.0;
			BF = 1.0;
			BG = 1.0;
			CF = 1.0;
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
		double bonus = 0;
		//int [][] gB = this.getGameBoard();
		/*Special consideration for straight pieces*/
		/*if (inState.getShape().getType() == SHAPETYPE.I) {
			if (values.edgeCount + values.adjacentCount >= 5) {
				/*Best Solution*/
			//	if (values.missingBelow == 0) bonus += values.edgeCount + values.adjacentCount;
				//else if (values.numRowsThatWillClear >= 1) bonus += 5;
			//} else {
				/*<== Avoid the worst Solution ==>
				 * - subtract the number of squares "dangling" or otherwise in the air
				 * - if the answer is negative, then it is probably a good thing (like on the bottom)*/
//				int dangling = 10 - (values.edgeCount + values.adjacentCount);
	//			bonus -= dangling;				
		//	}
//			bonus += values.distanceFromBottom;
	//	} else*/ 
		if (inState.getShape().getType() == SHAPETYPE.O) {
			bonus -= 400*MB*values.missingBelow;
		} else {		
			/**if (percentDown < 33 || distanceFromBottom < 5) {
				bonus += 3*EC*numRowsThatWillClear;
				bonus -= 2*MB;
				bonus += 2*EC;
			}**/
		}	
		//System.out.println (inState.getShape().getType() + ": Covered factor: " + coveredFactor);
		val = ( AC*values.adjacentCount -MB*values.missingBelow + EC*values.edgeCount + RC*values.numRowsThatWillClear - BF*values.buriedFactor - BG*values.badGapBesideShape + CF*values.coveredFactor);//);
		//val -= (inState.getBoardSize()[0] -values.distanceFromBottom < 7) ? inState.getBoardSize()[0] : 0;
	//	val -= values.distanceFromBottom;
	/*	SolutionMaster.solutionText = "HIGHLIGHTED SOLUTION = " + val + "\n= bonus + AC*adjacentCount - MB*missingBelow + EC*edgeCount + DG*distanceFromTop + RC*numRowsThatWillClear \n" +
				"AC = " + AC + " MB = " + MB + " EC = " + EC + " DG " + DG + " RC = " + RC + "\n" +
				"\nbonus = " + bonus +
				"\nadjacentCount = " + adjacentCount + 
				"\nmissingBelow = " + missingBelow +
				"\nedgeCount = " + edgeCount + 
				"\ndistanceFromBottom = " + distanceFromBottom +
				"\nnumRowsThatWillClear = " + numRowsThatWillClear +
				"\nroughness = " + roughness +
				"\nburiedFactor = " + buriedFactor +
				"\nbadGapBesideShape = " + badGapBesideShape + 
				"\ncoveredfactor = " + coveredFactor +
				"\n\n";*/	
		//System.out.println(this.solutionText);
		//val += RC*numRowsThatWillClear*Math.abs(val);
		//val += Math.abs (RC*numRowsThatWillClear*(missingBelow - 1));
		
		/* Scale the result by the height of the board */ 
		//val +=  DG*100*inState.getBoardSize() [0];
		//val -= ((int)DG*100*((double) values.distanceFromBottom));
		//val *= (CF > 0 && values.coveredFactor > 0) ? CF*values.coveredFactor : 1;
		return val;
	}
	public static ComputedValue getSolutionParameters(GameState inState) {
		ArrayList<Integer> rowsThatWillClear = new ArrayList<Integer> (); 
		int [][] gB = inState.getBoardWithCurrentShape().getState();
		
		final int gameWidth = gB[0].length;
		final int maxDepth = gB.length;

		// Check which rows will clear
		for (int[] coord : inState.getShape().getCoords()) {
			if (rowsThatWillClear.contains(new Integer(coord[0]))) continue;
			if (coord[0] < 0 || coord[1] < 0) {
				return new ComputedValue ();
			}	
			int solidCount = 0;
			for (int col=0;col<gameWidth;col++) {
				if (gB[coord[0]][col] != 0) {
					solidCount++;
				}
			}
			if (solidCount == gameWidth) {
				if (!rowsThatWillClear.contains(new Integer(coord[0]))) rowsThatWillClear.add(coord[0]);
			}
		}
		int adjacentCount = 0;
		int edgeCount = 0;
		int missingBelow = 0;
		int distanceToGround = 0;
		for (int[] coord  : inState.getShape().getCoords()) {
			distanceToGround = Math.max(distanceToGround, coord[0]);
			if (coord[0] == gB.length-1) {
				edgeCount++;
				adjacentCount--;
			}									
			if (coord[1] == gB[0].length-1) {
				edgeCount++;
				adjacentCount--;
			}
			if (coord[1] == 0) {
				edgeCount++;
				adjacentCount--;
			}

			// Check adjacent below and calculate missingBelow
			try {
				int mb_old = missingBelow;
				if (gB [coord[0]+1] [coord[1]] != 0) {
					if (inState.getShape().contains(new int [] {coord[0] + 1, coord[1]})) adjacentCount++;
				} else {
					missingBelow++;					
					if (rowsThatWillClear.contains(new Integer(coord[0])) && coord[0]-1 >= 0 && gB [coord[0]-1] [coord[1]] == 0) missingBelow--;
				}
			} catch (ArrayIndexOutOfBoundsException e) {					
			}
			// Check adjacent above
			try {
				if (gB [coord[0]-1] [coord[1]] != 0) {
					if (inState.getShape().contains(new int [] {coord[0] - 1, coord[1]})) adjacentCount++;					
				}
			} catch (ArrayIndexOutOfBoundsException e) {					
			}
			// Check adjacent to the right
			try {
				if (gB [coord[0]] [coord[1] + 1] != 0) {
					if (inState.getShape().contains(new int [] {coord[0], coord[1] + 1})) adjacentCount++;
				}
			} catch (ArrayIndexOutOfBoundsException e) {						
			}
			// Check adjacent to the left
			try {
				if (gB [coord[0]] [coord[1] - 1] != 0) {
					if (inState.getShape().contains(new int [] {coord[0], coord[1] - 1})) adjacentCount++;
				}
			} catch (ArrayIndexOutOfBoundsException e) {					
			}
		}	
		int height = gB.length;
		int percentDown = (distanceToGround*100)/(height); // Old "distanceTo..." is actually the min height of the shape coords (/ or max depth)
		distanceToGround = height - distanceToGround;
		int roughness = getRoughness (inState);
		int [] rowsClear = getRowsThatWillClear(inState);
		int numRowsThatWillClear = 0;
		for (int i=0;i<rowsClear.length;i++) {
			if (rowsClear[i] != 0) numRowsThatWillClear++;
		}

		//GameState.dumpState(inState, true);
		adjacentCount = getAdjacentCount(inState);
//		return new int[] {adjacentCount,0,0,0,0,0, 0}; // two times edge count so the edges are taken before others
		int buriedFactor = getBurriedFactor (inState);
		int badGapBesideShape = getBadGapBesideShape (inState);
		int coveredFactor = getCoveredFactor (inState);
		missingBelow = getMissingBelow (inState);
		int density = getDensity (inState);
		ComputedValue values = new ComputedValue ();
		values.adjacentCount = adjacentCount;
		values.buriedFactor  = buriedFactor;
		values.coveredFactor = coveredFactor;
		values.distanceFromBottom = distanceToGround;
		values.badGapBesideShape = badGapBesideShape;
		values.missingBelow = missingBelow;
		values.edgeCount = edgeCount;
		values.roughness = roughness;
		values.percentDown = percentDown;
		values.density = density;
		values.numRowsThatWillClear = numRowsThatWillClear;
		
		return values;
	}
	private static int getMissingBelow (GameState inState) {
		int [] rowsThatWillClear = getRowsThatWillClear (inState);
		int [][] gB = inState.getBoardWithCurrentShape().getState();
		int missingBelow = 0;
		for (int [] coord : inState.getShape().getCoords()) {
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
