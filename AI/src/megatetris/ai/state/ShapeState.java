package megatetris.ai.state;

import java.util.Arrays;
import tetris.engine.shapes.SHAPETYPE;

public class ShapeState {
	private int [][] coords;
	private SHAPETYPE type;
	public ShapeState (int [][] coords, SHAPETYPE type) {
		this.setState (coords, type);
	}
	public ShapeState (ShapeState old) {
		this.setState (old.getCoordsCopy(), old.type);
	}
	protected void setState (int [][] coords, SHAPETYPE type) {
		int [][] copy = Arrays.copyOf(coords, coords.length);
		for (int i=0;i<copy.length;i++) {
			copy[i] = Arrays.copyOf(copy[i], copy[i].length);
		}
		this.coords = copy;
		this.type = type;
	}
	public int [][] getCoords () {
		return this.coords;
	}
	public int [][] getCoordsCopy () {
		int [][] copy = Arrays.copyOf(this.coords, this.coords.length);
		for (int i=0;i<copy.length;i++) {
			copy[i] = Arrays.copyOf(copy[i], copy[i].length);
		}
		return copy;	
	}
	public SHAPETYPE getType () {
		return this.type;
	}
	public boolean contains (int [] coord) {
		for (int [] c : this.coords)
			if (c[0] == coord[0] && c[1] == coord[1]) return true;		
		return false;
	}
}
