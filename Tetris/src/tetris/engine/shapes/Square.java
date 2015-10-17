package tetris.engine.shapes;
import tetris.engine.mechanics.Engine;
import tetris.engine.mechanics.Space;

public class Square extends Shape{
	private static int LocalShapeColor = 5;
	protected final Engine.ShapeType type = Engine.ShapeType.O; 
	public Square(Space[][] gameBoard) {
		super(gameBoard);	
		Space[] shapeSpaces = new Space[] {gameBoard[1 + this.verticalOffset][this.startColumnLeftMost],gameBoard[1 + this.verticalOffset][startColumnRightMost],gameBoard[0 + this.verticalOffset][this.startColumnLeftMost],gameBoard[0 + this.verticalOffset][startColumnRightMost]};
		this.setSpaces(shapeSpaces);
		this.ShapeColor = LocalShapeColor;
		this.drawSpaces(true);
	}
	public Square getInstance(Space[][] gameBoard) {
		return new Square (gameBoard);
	}
	public boolean rotateForward() {
		return true;		
	}
	public boolean rotateBackward() {
		return true;		
	}
	public Engine.ShapeType getType () {
		return type;
	}
}
