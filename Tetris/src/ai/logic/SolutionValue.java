package ai.logic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;

import ai.state.GameState;
import ai.transformations.ShapeTransforms;
import tetris.engine.shapes.SHAPETYPE;

public class SolutionValue {
	protected static double AC, MB, EC, DG, RC, RD, BF, BG;
	//private double[] defaults = new double[] {2.5,1,1,1, 2.5, 3, 1,1};
	//private double[] defaults = new double[] {1,2,2,1, 1, 0, 1,1};
	protected static double[] defaults = new double[] {3,13,3,2,1,0 , 1,1};	
	private final static Logger LOGGER = Logger.getLogger(SolutionMaster.class.getName());
	static {
		LOGGER.setLevel(Logger.getGlobal().getLevel());		
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
			AC = config[0];
			MB = config[1]; 
			EC = config[2]; 
			DG = config[3]; 
			RC = config[4]; 
			RD = config[5]; 
			BF = config[6]; 
			BG = config[7];
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
		}
		LOGGER.info("AC : " + AC);
		LOGGER.info("MB : " + MB);
		LOGGER.info("EC : " + EC);
		LOGGER.info("DG : " + DG);
		LOGGER.info("RC : " + RC);
		LOGGER.info("RD : " + RD);
		LOGGER.info("BF : " + BF); 
		LOGGER.info("BG : " + BG); 
	}

	protected static int calculateSolution(GameState inState, int [] parameters, boolean takeRisk) {
		int adjacentCount 	= parameters [0];
		int missingBelow	= parameters [1];
		int edgeCount		= parameters [2];
		int distanceFromTop	= parameters [3];
		int numRowsThatWillClear	= parameters [4];
		int roughness	= parameters [5];
		int percentDown = parameters[6];
		int buriedFactor = parameters[7];
		int badGapBesideShape = parameters[8];
		int val = -90000000;
		double bonus = 0;
		//int [][] gB = this.getGameBoard();
		/*Special consideration for straight pieces*/
		if (inState.getShape().getType() == SHAPETYPE.I) {
			if (edgeCount + adjacentCount >= 5) {
				/*Best Solution*/
				if (missingBelow == 0) bonus += edgeCount + adjacentCount;
				else if (numRowsThatWillClear >= 1) bonus += 5;
			} else {
				/*<== Avoid the worst Solution ==>
				 * - subtract the number of squares "dangling" or otherwise in the air
				 * - if the answer is negative, then it is probably a good thing (like on the bottom)*/
				int dangling = 10 - (edgeCount + adjacentCount);
				bonus -= dangling;				
			}
			bonus += distanceFromTop;
		} else if (inState.getShape().getType() == SHAPETYPE.O) {
			bonus -= missingBelow;
		} else {		
			if (percentDown < 33 || distanceFromTop < 5) {
				bonus += 3*EC*numRowsThatWillClear;
				bonus -= 2*MB;
				bonus += 2*EC;
			}
		}	
		val = (int) ((int) AC*adjacentCount - (MB)*missingBelow + EC*edgeCount - ((int)DG*distanceFromTop) - BF*buriedFactor - BG*badGapBesideShape);
		SolutionMaster.solutionText = "HIGHLIGHTED SOLUTION = " + val + "\n= bonus + AC*adjacentCount - MB*missingBelow + EC*edgeCount + DG*distanceFromTop + RC*numRowsThatWillClear \n" +
				"AC = " + AC + " MB = " + MB + " EC = " + EC + " DG " + DG + " RC = " + RC + "\n" +
				"\nbonus = " + bonus +
				"\nadjacentCount = " + adjacentCount + 
				"\nmissingBelow = " + missingBelow +
				"\nedgeCount = " + edgeCount + 
				"\ndistanceFromBottom = " + distanceFromTop +
				"\nnumRowsThatWillClear = " + numRowsThatWillClear +
				"\nroughness = " + roughness +
				"\nburiedFactor = " + buriedFactor +
				"\nbadGapBesideShape = " + badGapBesideShape + 
				"\n\n";	
		//System.out.println(this.solutionText);
		val += Math.abs(RC*numRowsThatWillClear*val);
		
		return val;
	}
	public static int[] getSolutionParameters(GameState inState) {
		ArrayList<Integer> rowsThatWillClear = new ArrayList<Integer> (); 
		int [][] gB = inState.getBoardWithCurrentShape().getState();
		
		final int gameWidth = gB[0].length;
		final int maxDepth = gB.length;

		// Check which rows will clear
		for (int[] coord : inState.getShape().getCoords()) {
			if (rowsThatWillClear.contains(new Integer(coord[0]))) continue;
			if (coord[0] < 0 || coord[1] < 0) {
				return new int[] {0,0,0,0,0};
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
		int numRowsThatWillClear = rowsThatWillClear.size();
		//GameState.dumpState(inState, true);
		adjacentCount = getAdjacentCount(inState);
//		return new int[] {adjacentCount,0,0,0,0,0, 0}; // two times edge count so the edges are taken before others
		int buriedFactor = getBurriedFactor (inState);
		int badGapBesideShape = getBadGapBesideShape (inState);
		return new int[] {adjacentCount,missingBelow,edgeCount,distanceToGround,numRowsThatWillClear,roughness, percentDown, buriedFactor, badGapBesideShape}; // two times edge count so the edges are taken before others
	}
	private static int getBurriedFactor (GameState inState) {
		int gB [][] = inState.getBoardWithCurrentShape().getState();
		int buriedFactor = 0;
		for (int [] shCoord : inState.getShape().getCoords()) {
			int numSolid = 0;
			int numEmpty = 0;
			for (int row = shCoord[0]+1;row<gB.length;row++) {
				if (gB[row][shCoord[1]] != 0);
				else numEmpty++;
			}
			for (int row = shCoord[0];row>=0;row--) {
				if (gB[row][shCoord[1]] != 0) numSolid++;
				else break;
			}
			buriedFactor += numEmpty;
		}
		return buriedFactor;
	}
	private static int getRoughness (GameState inState) {
		return getRoughness (inState, 0, inState.getBoardWithCurrentShape().getState()[0].length);
	}
	private static int getAdjacentCount (GameState inState) {
		int adjacentCount = 0;
		int [][] gB = inState.getBoardWithoutCurrentShape().getState();
		for (int [] coord : inState.getShape().getCoords()) {
//			for (int j=-1;j<2;j++) {
//				for (int k=-1;k<2;k++) {
//					try {
//						//if (ShapeTransforms.isCoordPartOfCurrentShape(inState, new int [] {coord[0]+j,coord[1]+k}));
//						if (gB [coord[0]+j][coord[1]+k] != 0) {
//							if (j==0 && k==0) System.out.println("ERROR: SOLUTION VALUE: NOT DETECTING PART OF SHAPE PROPERLY");
//							adjacentCount ++;
//						}
//					} catch (ArrayIndexOutOfBoundsException e) {
//						//adjacentCount ++;
//					}
//				}
//			}
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
	private static int getBadGapBesideShape (GameState inState) {
		int badHindsight = 0;
		int [][] gB = inState.getBoardWithCurrentShape().getState();
		for (int [] coord : inState.getShape().getCoords()) {
			int localBad = 0;
			for (int j=-1;j>-3;j--) {
				try {
					if (gB [coord[0]][coord[1]+j] == 0) {
						localBad ++;
					} else break;
				} catch (ArrayIndexOutOfBoundsException e) {}
			}
			badHindsight = (localBad == 1) ? ++badHindsight : badHindsight;
			localBad = 0;
			for (int j=1;j<3;j++) {
				try {
					if (gB [coord[0]][coord[1]+j] == 0) {
						badHindsight ++;
					} else break;
				} catch (ArrayIndexOutOfBoundsException e) {}
			}
			badHindsight = (localBad == 1) ? ++badHindsight : badHindsight;
		}
		return badHindsight;
	}
	private static int getRoughness (GameState inState, int startColumn, int endColumn) {
		int oldDepth = getDepthOfColumn (inState.getBoardWithCurrentShape().getState(), 0);
		int roughness = 0;
		final int maxCol = endColumn;
		return 0;
//		for (int c=startColumn;c<maxCol;c++) {
//			int depth = getDepthOfColumn (boardCoords, c);		 
//			int diff = depth - oldDepth;			
//			if (diff < -1) {
//				for (int subCol=c+1;subCol < maxCol;subCol++) {
//					if (getDepthOfColumn (boardCoords, subCol) >= )
//				}
//				roughness += 
//			}
//
//		}
//		return roughness;
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