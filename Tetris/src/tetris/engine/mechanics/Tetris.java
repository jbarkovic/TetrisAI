package source.tetris.engine.mechanics;



interface Tetris {

	boolean rotateCounterClockwise();
	boolean rotateClockwise();
	boolean shiftLeft();
	boolean shiftRight();
	boolean dropShape();
	boolean isGameLost();
	void plummit();
	void impossible();
	boolean swapShapes();
	void pause();
	void setGravity(int time_milliseconds);
	int[][] getGameBoard();
	int[][] getSwapBoard();
}
