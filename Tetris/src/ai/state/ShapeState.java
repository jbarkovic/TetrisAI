package ai.state;

import java.util.Arrays;

public class ShapeState implements java.io.Serializable {
	// integer arrays are serializable though integers themselves are not 
	private int [][] coords;
	private SHAPETYPE type;
	public ShapeState (int [][] coords, SHAPETYPE type) {
		this.setState (coords, type);
	}
	public ShapeState (ShapeState old) {
		this.setState (old.getCoordsCopy(), old.type);
	}
	protected void setState (int [][] coords, SHAPETYPE type) {
		this.coords = coords;
		this.coords = this.getCoordsCopy();
		this.type = type;
	}
	public int [][] getCoords () {
		return this.coords;
	}
	public int [][] getCoordsCopy () {
		int [][] copy = new int [this.coords.length][2];
		for (int i=0;i<copy.length;i++) {
			copy[i] = new int [] {this.coords[i][0], this.coords[i][1]};
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
	public boolean equals (ShapeState other) {
		if (this.type == other.type) {
			if (this.coords.length == other.coords.length) {
				for (int i=0;i<this.coords.length;i++) {
					if (!Arrays.equals(this.coords[i], other.coords[i])) return false;
				}
			}
		}
		return true;
	}
}
