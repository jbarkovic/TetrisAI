package tetris.engine.shapes;
import tetris.engine.mechanics.Space;

public class Tee extends Shape{
	private RotatePositions rotateState;
	private static int LocalShapeColor = 7;
	private enum RotatePositions {
		UP, DOWN, LEFT, RIGHT
	}
	public Tee(Space[][] gameBoard) {
		super(gameBoard);
		Space shapeSpaces[] = new Space[] {gameBoard[0 + this.verticalOffset][this.startColumnLeftMost],gameBoard[1 + this.verticalOffset][startColumnRightMost],gameBoard[1 + this.verticalOffset][this.startColumnLeftMost],gameBoard[2 + this.verticalOffset][this.startColumnLeftMost]};
		this.setSpaces(shapeSpaces);
		this.rotateState = RotatePositions.RIGHT;
		this.ShapeColor = LocalShapeColor;
		this.drawSpaces(true);	
	}
	public Tee getInstance(Space[][] gameBoard) {
		return new Tee (gameBoard);
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
			coordsOfNew_0_Space = new int[] {coordsOfOld_0_Space[0]-1,coordsOfOld_0_Space[1]+1};
			coordsOfNew_1_Space = new int[] {coordsOfOld_1_Space[0]+1,coordsOfOld_1_Space[1]+1};
			coordsOfNew_2_Space = coordsOfOld_2_Space;
			coordsOfNew_3_Space = new int[] {coordsOfOld_3_Space[0]+1,coordsOfOld_3_Space[1]+-1};
			this.rotateState = RotatePositions.RIGHT;
			break;
		}
		case RIGHT : {
			this.drawSpaces(false);
			coordsOfNew_0_Space = new int[] {coordsOfOld_0_Space[0]+1,coordsOfOld_0_Space[1]+1};
			coordsOfNew_1_Space = new int[] {coordsOfOld_1_Space[0]+1,coordsOfOld_1_Space[1]-1};
			coordsOfNew_2_Space = coordsOfOld_2_Space;
			coordsOfNew_3_Space = new int[] {coordsOfOld_3_Space[0]-1,coordsOfOld_3_Space[1]-1};
			this.rotateState = RotatePositions.DOWN;
			break;
		}
		case DOWN : {
			this.drawSpaces(false);
			coordsOfNew_0_Space = new int[] {coordsOfOld_0_Space[0]+1,coordsOfOld_0_Space[1]-1};
			coordsOfNew_1_Space = new int[] {coordsOfOld_1_Space[0]-1,coordsOfOld_1_Space[1]-1};
			coordsOfNew_2_Space = coordsOfOld_2_Space;
			coordsOfNew_3_Space = new int[] {coordsOfOld_3_Space[0]-1,coordsOfOld_3_Space[1]+1};
			this.rotateState = RotatePositions.LEFT;
			break;
		}
		case LEFT : {
			this.drawSpaces(false);			
			coordsOfNew_0_Space = new int[] {coordsOfOld_0_Space[0]-1,coordsOfOld_0_Space[1]-1};
			coordsOfNew_1_Space = new int[] {coordsOfOld_1_Space[0]-1,coordsOfOld_1_Space[1]+1};
			coordsOfNew_2_Space = coordsOfOld_2_Space;
			coordsOfNew_3_Space = new int[] {coordsOfOld_3_Space[0]+1,coordsOfOld_3_Space[1]+1};
			this.rotateState = RotatePositions.UP;
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
				if (this.gameBoard[coordsOfSpace_2[0]+1][coordsOfSpace_2[1]].getColor() == 0) {
					return true;
				}
				else return false;
			}
			case RIGHT : {
				if (this.spaces.length <4) {
					return false;
				}
				int[] coordsOfSpace_2 = this.getCoordsOfSpace(this.spaces[2]);
				if (this.gameBoard[coordsOfSpace_2[0]][coordsOfSpace_2[1]-1].getColor() == 0) {
					return true;
				}
				else {
					return false;
				}
			}
			case DOWN : {
				if (this.spaces.length <4) {
					return false;
				}		
				int[] coordsOfSpace_2 = this.getCoordsOfSpace(this.spaces[2]);
				if (this.gameBoard[coordsOfSpace_2[0]-1][coordsOfSpace_2[1]].getColor() == 0) {
					return true;
				}
				else {
					return false;
				}
			}
			case LEFT : {
				if (this.spaces.length <4) {
					return false;
				}
				int[] coordsOfSpace_2 = this.getCoordsOfSpace(this.spaces[2]);	
				if (this.gameBoard[coordsOfSpace_2[0]][coordsOfSpace_2[1]+1].getColor() == 0) {
					return true;
				}
			}
			}
			return false;
		} catch (ArrayIndexOutOfBoundsException e) {
			return false;
		}
	}

}
