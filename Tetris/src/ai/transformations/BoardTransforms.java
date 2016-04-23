package ai.transformations;

import ai.state.*;

public class BoardTransforms {
	public static double getPercentFull(GameState inState) {
		int[][] gB = inState.getBoardWithoutCurrentShape().getState();
		int fullCount=0;
		for (int row=0;row<gB.length;row++) {
			for (int col=0;col<gB[row].length;col++) {
				if (gB[row][col] != 0){
					fullCount++;
				}
			}
		}
		return fullCount/((double)(Math.max(1, (gB.length*gB[0].length))));
	}
	public static boolean isCoordPartOfList(int[] coord, int[][] list) {
		for (int[] cor : list) {
			if (cor[0] == coord[0] && cor[1] == coord[1]) {				
				return true;				
			}
		}
		return false;
	}
	public static int clearFullRows (GameState inState) {
		int numRowsCleared = 0;
		int [][] gB = inState.getBoardWithCurrentShape().getState();
		final int fullVal = gB[0].length;
		for (int row=gB.length-1;row>=0;row--) {			
			int fullCount = 0;
			for (int col=0;col<gB[row].length;col++) {
				if (gB[row][col] != 0) fullCount ++;
			}
			if (fullCount == fullVal) {
				numRowsCleared ++;
				for (int subRow=row;subRow>0;subRow--) {
					for (int col=0;col<gB[subRow].length;col++) {
						gB[subRow][col] = gB[subRow-1][col];
					}
				}
				for (int col=0;col<gB[0].length;col++) {
					gB[0][col] = 0;
				}
				
				row++; // need to check the row that just dropped from above as well since it may be full too
			}
		}
		return numRowsCleared;
	}
}
