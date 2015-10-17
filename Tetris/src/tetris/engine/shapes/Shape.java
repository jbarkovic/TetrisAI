package tetris.engine.shapes;
import tetris.engine.mechanics.Engine;
import tetris.engine.mechanics.Space;

public class Shape {
protected Space[] spaces; // needs to be an object instead of int for referencing purposes
protected Space[][] gameBoard;
protected int ShapeColor;
protected int startColumnLeftMost;
protected int startColumnRightMost;
protected int verticalOffset;
protected final Engine.ShapeType type = null;
protected final int EMPTYCOLOR = 0;
protected final int SHADOWCOLOR = 10;
public Shape(Space[][] gameBoard) {	
	this.gameBoard = gameBoard;
	this.startColumnLeftMost = gameBoard[0].length/2 - 1 ;
	this.startColumnRightMost = this.startColumnLeftMost + 1;
	if (gameBoard.length == 4) {
		this.verticalOffset = 1;
	}
	else {
		this.verticalOffset = 0;
	}
}
public Shape getInstance(Space[][] gameBoard) {
	return new Shape (gameBoard);
}
public Engine.ShapeType getType () {
	return type;
}
public boolean containsSpace(int coords[]) {
	for (Space sp : this.spaces) {
		if (sp.getCoords()[0] == coords[0] && sp.getCoords()[1] == coords[1]) {
			return true;
		}
	}
	return false;
}
public Space[] getSpaces() {
	return this.spaces;
}
public void setSpaces(Space[] spaces) {
	this.spaces = spaces;
	this.drawSpaces(true);
}
public boolean rotateForward(){
	return false;
}
public boolean rotateBackward() {	
	if (!this.canRotate()) return false;
	this.drawSpaces(false);			
	this.rotateForward();
	this.drawSpaces(false);
	this.rotateForward();
	this.drawSpaces(false);
	return this.rotateForward();
}
protected boolean canRotate() {
	return false;
}
public void removeSpace(Space space) {
	for (int i=0;i<this.spaces.length;i++) {
		if (this.spaces[i].equals(space)) {
			Space[] temp = new Space[this.spaces.length-1];
			int k = 0;
			for (int j=0;j<this.spaces.length;j++,k++) {
				if (!this.spaces[j].equals(space)){
					temp[k] = this.spaces[j];
				}
				else {
					k--;
				}
			}
			this.spaces = temp.clone();
		}
	}
}
public boolean shiftLeft() { // returns true if there is a collision, the shift is not performed 
	Space[] newSpaces = new Space[4];
	try {
		for (int i=0;i<this.spaces.length;i++) {
			int[] tempCoords = this.getCoordsOfSpace(this.spaces[i]);
			newSpaces[i] = this.gameBoard[tempCoords[0]][tempCoords[1]-1];
			if (collision(newSpaces[i])) { // collision check
				return true;
			}
		}
	} catch (ArrayIndexOutOfBoundsException e) { // this is triggered when a shape reaches the edge of the game board, this is normal		
		return true;
	}
	this.drawSpaces(false);
	this.spaces = newSpaces; // "move" the shape
	this.drawSpaces(true);
	return false;
}
protected int[] getCoordsOfSpace(Space space) {
	return space.getCoords();
}
private boolean collision(Space sh) { // reused code to determine if a collision with another shape will occur
	if (sh.getColor() == EMPTYCOLOR || sh.getColor() == SHADOWCOLOR) return false;	
	else {
		for (Space ourSpace : this.spaces) { // determines if this "other shape" is ourself
			if (ourSpace.equals(sh)) {
				return false;
			}
		}
	}
	return true;
}
public boolean shiftRight() { // returns true if there is a collision, the shift is not performed 
	Space[] newSpaces = new Space[4];
	try {				
		for (int i=0;i<this.spaces.length;i++) { // populate new spaces list
			int[] tempCoords = this.getCoordsOfSpace(this.spaces[i]);
			newSpaces[i] = this.gameBoard[tempCoords[0]][tempCoords[1]+1];
			if (collision(newSpaces[i])) {
				return true;
			}
		}
	} catch (ArrayIndexOutOfBoundsException e) { // this is triggered when shape reaches an edge of the game board, this is normal
		return true;
	}
	this.drawSpaces(false);
	this.spaces = newSpaces; // "move" the shape
	this.drawSpaces(true);
	return false;
}
public boolean drop() { //returns true if there is a collision, the drop is not performed if a collision occurs
	Space[] newSpaces = new Space[4];
	try {
		for (int i=0;i<this.spaces.length;i++) {
			int[] tempCoords = this.getCoordsOfSpace(this.spaces[i]);
			newSpaces[i] = this.gameBoard[tempCoords[0]+1][tempCoords[1]];
			if (collision(newSpaces[i])) { // collision check
				return true;
			}
		}
	} catch (ArrayIndexOutOfBoundsException e) { // when shape reaches the bottom, will trigger exception here, this is normal
		return true;
	}
	this.drawSpaces(false);
	this.spaces = newSpaces; // "move" the shape]
	
	this.drawSpaces(true);
	return false;
}
public void delete() {
	drawSpaces(false);
}
protected void drawSpaces(boolean toggle) {
	for (int space=0;space<this.spaces.length;space++) {
		if (toggle) {
			this.spaces[space].setColor(this.ShapeColor);
		}
		else {
			this.spaces[space].setColor(EMPTYCOLOR);
		}

	}
}

}
