package ai.state;

import interfaces.EngineInterface;

public class GameState implements java.io.Serializable { 
	/**
	 * 
	 */
	private static final long serialVersionUID = 8546689583783367961L;
	private BoardState boardWithCurrentShape 	= null;
	private BoardState boardWithoutCurrentShape	= null;
	private ShapeState currentShape 			= null;
	private String message = null;
	private Integer [] otherShapeData; // Data about nextshape and swapshape etc... to be filled by other classes as needed
	
	private GameState () {}
	public GameState (EngineInterface engine) {
		this (new BoardState (engine.getGameBoard()), new ShapeState (engine.getCoordsOfCurrentShape(), engine.getCurrentShape()));
	}
	public GameState (BoardState bState, ShapeState sState) {
		this();
		init (bState, sState);
	}
	public GameState (GameState old) {
		this();
		init (old.getBoardWithoutCurrentShape(), old.currentShape);
	}
	public void setState (BoardState bState, ShapeState sState) {
		this.boardWithCurrentShape.setState(bState);
		this.boardWithoutCurrentShape.setState(bState);
		this.currentShape.setState(sState);
		this.getBoardSize()[0] = bState.size()[0];
		this.getBoardSize()[1] = bState.size()[1];

		this.boardWithCurrentShape.setSpaces(currentShape.getCoords(),currentShape.getType().toInt());
		this.boardWithoutCurrentShape.clearSpaces(currentShape.getCoords());
	}
	private void init (BoardState boardState, ShapeState shapeState) { // Will take gameboard with or without current shape		
		this.currentShape = new ShapeState(shapeState);
		this.boardWithCurrentShape = new BoardState (boardState);
		this.boardWithoutCurrentShape = new BoardState (boardState);
		setState (boardState, shapeState);
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
	public int numColumns() {
		return this.getBoardSize()[1];
	}
	public int numRows() {
		return this.getBoardSize()[0];
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
	
	public boolean equals (GameState other) {
		return other.getBoardWithCurrentShape().equals(this.getBoardWithCurrentShape());
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
	public int hashCode () {
		return (new String ("" + this.boardWithCurrentShape.getState())).hashCode();
	}
	public static String dumpState (GameState inState, boolean printToOut) {
		StringBuilder message = new StringBuilder ("");
		message.append("===============================================V\n");
		message.append("Game State Dump: \n");
		message.append("BoardWithCurrentShape\n");
		message.append(inState.boardWithCurrentShape.dumpBoard());
		message.append("\nBoardWithOUTCurrentShape\n");
		message.append(inState.boardWithoutCurrentShape.dumpBoard());
		message.append("TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT\n");
		if (printToOut) System.out.println(message.toString());
		return message.toString();
	}
}
