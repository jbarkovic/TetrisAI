package tetris.engine.shapes;
import tetris.engine.mechanics.Space;

public class Straight extends Shape{
	private RotatePositions rotateState;
	private static int LocalShapeColor = 6;
	private enum RotatePositions {
		UP, RIGHT
	}
	public Straight(Space[][] gameBoard) {
		super(gameBoard);
		this.verticalOffset = 0;
		Space shapeSpaces[] = new Space[] {gameBoard[3 + this.verticalOffset][this.startColumnLeftMost],gameBoard[2 + this.verticalOffset][this.startColumnLeftMost],gameBoard[1 + this.verticalOffset][this.startColumnLeftMost],gameBoard[0 + this.verticalOffset][this.startColumnLeftMost]};
		this.setSpaces(shapeSpaces);
		this.rotateState = RotatePositions.UP;
		this.ShapeColor = LocalShapeColor;
		this.drawSpaces(true);	
		this.rotateForward(); // Added to comply with the horizontal spawn requirement 
		this.shiftRight();
	}
	public Straight getInstance(Space[][] gameBoard) {
		return new Straight (gameBoard);
	}


	public boolean rotateForward() {
		if (!this.canRotate()) return false;
		int[] coordsOfOld_0_Space = this.getCoordsOfSpace(this.spaces[0]);
		int[] coordsOfOld_1_Space = this.getCoordsOfSpace(this.spaces[1]);
		int[] coordsOfOld_2_Space = this.getCoordsOfSpace(this.spaces[2]);
		int[] coordsOfOld_3_Space = this.getCoordsOfSpace(this.spaces[3]);
		int[] coordsOfNew_0_Space = new int[2];
		int[] coordsOfNew_1_Space = new int[2];
		int[] coordsOfNew_2_Space = new int[2];
		int[] coordsOfNew_3_Space = new int[2];
		switch (this.rotateState) {
		case UP : {
			this.drawSpaces(false);
			coordsOfNew_0_Space = new int[] {coordsOfOld_0_Space[0]-2,coordsOfOld_0_Space[1]-2};
			coordsOfNew_1_Space = new int[] {coordsOfOld_1_Space[0]-1,coordsOfOld_1_Space[1]-1};
			coordsOfNew_2_Space = coordsOfOld_2_Space;
			coordsOfNew_3_Space = new int[] {coordsOfOld_3_Space[0]+1,coordsOfOld_3_Space[1]+1};
			this.rotateState = RotatePositions.RIGHT;
			break;
		}
		case RIGHT : {
			this.drawSpaces(false);
			coordsOfNew_0_Space = new int[] {coordsOfOld_0_Space[0]+2,coordsOfOld_0_Space[1]+2};
			coordsOfNew_1_Space = new int[] {coordsOfOld_1_Space[0]+1,coordsOfOld_1_Space[1]+1};
			coordsOfNew_2_Space = coordsOfOld_2_Space;
			coordsOfNew_3_Space = new int[] {coordsOfOld_3_Space[0]-1,coordsOfOld_3_Space[1]-1};
			this.rotateState = RotatePositions.UP;
			break;
		}
		}
		this.spaces[0] = this.gameBoard[coordsOfNew_0_Space[0]][coordsOfNew_0_Space[1]];
		this.spaces[1] = this.gameBoard[coordsOfNew_1_Space[0]][coordsOfNew_1_Space[1]];
		this.spaces[2] = this.gameBoard[coordsOfNew_2_Space[0]][coordsOfNew_2_Space[1]];
		this.spaces[3] = this.gameBoard[coordsOfNew_3_Space[0]][coordsOfNew_3_Space[1]];
		this.drawSpaces(true);
		return true;
	}

	protected boolean canRotate() {	
		try {
			switch (this.rotateState) {
			case UP : {
				if (this.spaces.length <4) {
					return false;
				}		
				int[] coordsOfSpace_2 = this.getCoordsOfSpace(this.spaces[2]);
				if (this.gameBoard[coordsOfSpace_2[0]][coordsOfSpace_2[1]+1].getColor() == 0 && this.gameBoard[coordsOfSpace_2[0]][coordsOfSpace_2[1]-1].getColor() == 0 && this.gameBoard[coordsOfSpace_2[0]][coordsOfSpace_2[1]-2].getColor() == 0) {
					return true;
				}
				else return false;
			}
			case RIGHT : {
				if (this.spaces.length <4) {
					return false;
				}
				int[] coordsOfSpace_2 = this.getCoordsOfSpace(this.spaces[2]);
				if (this.gameBoard[coordsOfSpace_2[0]-1][coordsOfSpace_2[1]].getColor() == 0 && this.gameBoard[coordsOfSpace_2[0]+1][coordsOfSpace_2[1]].getColor() == 0 && this.gameBoard[coordsOfSpace_2[0]-2][coordsOfSpace_2[1]].getColor() == 0) {
					return true;
				}
				else {
					return false;
				}
			}
			}
			return false;
		} catch (ArrayIndexOutOfBoundsException e) {
			return false;
		}
	}


}
