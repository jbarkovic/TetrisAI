package ai.state;

public class GameState {
	private BoardState boardWithCurrentShape 	= null;
	private BoardState boardWithoutCurrentShape	= null;
	
	private ShapeState currentShape 		= null;
	
	public GameState () {}
	public void setState (BoardState boardState, ShapeState shapeState) { // Will take gameboard with or without current shape		
		int i = boardState.getState().length;
		int j = boardState.getState()[0].length;
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
	public GameState (GameState old) {		
		this.setState(new BoardState (old.getBoardWithoutCurrentShape()), new ShapeState (old.currentShape));
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
