package interfaces;

import tetris.engine.mechanics.Engine;

public class EngineInterface {
	private Engine engine;
	
	public EngineInterface (Engine engine) {
		this.engine = engine;
	}
	public ai.state.SHAPETYPE getCurrentShape () {
		return convertShapetypes (this.engine.getCurrentShape());
	}
	public ai.state.SHAPETYPE getNextShape () {
		return convertShapetypes (this.engine.getNextShape());
	}
	public ai.state.SHAPETYPE getSwapShape () {
		return convertShapetypes (this.engine.getSwapShape());
	}
	public boolean dropShape () {
		return this.engine.dropShape ();
	}
	public boolean shiftLeft () {
		return this.engine.shiftLeft ();
	}
	public boolean shiftRight () {
		return this.engine.shiftRight ();
	}
	public int [][] getGameBoard () {
		return this.engine.getGameBoard();
	}
	public int [][] getCoordsOfCurrentShape () {
		return this.engine.getCoordsOfCurrentShape();
	}
	public boolean isGameLost () {
		return this.engine.isGameLost();
	}
	public boolean swapShapes () {
		return this.engine.swapShapes();
	}
	public boolean isPaused () {
		return this.engine.isPaused();
	}
	public boolean rotate () {
		return this.engine.rotateClockwise ();
	}
	public void plummit () {
		this.engine.plummit ();
	}
	public boolean wasThereANewShape () {
		return this.engine.wasThereANewShape();
	}
	public boolean rotateCounterClockwise () {
		return this.engine.rotateCounterClockwise();
	}
	public boolean forceShiftLeft () {
		return this.engine.forceShiftLeft ();
	}
	public boolean forceShiftRight () {
		return this.engine.forceShiftRight ();
	}
	public boolean forceRotate () {
		return this.engine.forceRotateCW ();
	}
	private ai.state.SHAPETYPE convertShapetypes (tetris.engine.shapes.SHAPETYPE engineType) {
		return ai.state.SHAPETYPE.intToShapeType(engineType.toInt());
	}
}
