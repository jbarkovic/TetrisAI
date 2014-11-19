package source.tetris.engine.mechanics;

public class Space {
private boolean shadow = false;
private int color;
private int[] coordinates = new int[] {0,0};
public Space(int row, int column, int color){
	this.color = color;
	this.coordinates = new int[] {row,column};
}
public int getColor(){
	return this.color;
}
public void setShadow(boolean val) {
	this.shadow = val;
}
public boolean getShadow() {
	return this.shadow;
}
public void setColor(int color){
	this.color = color;
}
public int[] getCoords() {
	return this.coordinates;
}
}