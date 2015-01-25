package ai.state;

public class GameState implements java.io.Serializable { 
	private BoardState boardWithCurrentShape 	= null;
	private BoardState boardWithoutCurrentShape	= null;
	private ShapeState currentShape 			= null;
	private String message = null;
	private Integer [] otherShapeData; // Data about nextshape and swapshape etc... to be filled by other classes as needed
	
	public GameState () {}
	public GameState (GameState old) {		
		this.setState(new BoardState (old.getBoardWithoutCurrentShape()), new ShapeState (old.currentShape));
	}
	
	public void setState (BoardState boardState, ShapeState shapeState) { // Will take gameboard with or without current shape		
		this.currentShape = new ShapeState(shapeState);
		int [][] boardWithout = boardState.getStateCopy();
		int [][] boardWith = boardState.getStateCopy();
		
		int [][] shapeCoords = this.currentShape.getCoordsCopy();
		for (int [] coord : shapeCoords) {
			boardWith 		[coord[0]] [coord[1]] 	= this.currentShape.getType().toInt();
			boardWithout 	[coord[0]] [coord[1]] 	= 0;
		}
		this.boardWithCurrentShape 		= new BoardState (boardWith);
		this.boardWithoutCurrentShape 	= new BoardState (boardWithout);
	}
	public int [] getBoardSize () {
		return this.boardWithCurrentShape.size();
	}
	public BoardState getBoardWithCurrentShape () {
		return this.boardWithCurrentShape;
	}
	public BoardState getBoardWithoutCurrentShape () {
		return this.boardWithoutCurrentShape;
	}
	public ShapeState getShape () {
		return this.currentShape;
	}
	public void setCurrentShape (ShapeState newCurrentShape) {
		this.setState(this.boardWithoutCurrentShape, newCurrentShape);		
	}
	public Integer[] getOtherShapeData() {
		return otherShapeData;
	}
	public void setOtherShapeData(Integer[] otherShapeData) {
		this.otherShapeData = otherShapeData;
	}
	public void setMessage (String message) {
		if (this.message == null) this.message = message;
		this.message += message;
	}
	public String getMessage () {
		return this.message;
	}
	public void invalidate () {
		setState (this.boardWithoutCurrentShape, currentShape);
	}
	public static String dumpState (GameState inState, boolean printToOut) {
		String message = "Game State Dump: \n";
		for (int row = 0; row<inState.getBoardWithCurrentShape().getState().length;row++) {
			message += "||";
			for (int col=0;col<inState.getBoardWithCurrentShape().getState()[0].length;col++) {
				message += inState.getBoardWithCurrentShape().getState()[row][col];
			}
			message += "||\n";
		}
		if (printToOut) System.out.println(message);
		return message;
	}
}
