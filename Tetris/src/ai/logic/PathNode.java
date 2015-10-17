package ai.logic;

import java.util.Arrays;

public class PathNode implements Comparable<PathNode> {
	private PathNode left;
	private PathNode right;
	private PathNode down;
	private PathNode up;
	private PathNode unRotate;
	private PathNode rotate;
	private boolean isLeaf;
	private int [] location;
	{
		left = null;
		right = null;
		down = null;
		up = null;
		unRotate = null;
		rotate = null;
		location = new int [3];
	}
	public enum Direction {
		DOWN, RIGHT, LEFT, ROTATE;
	}
	public PathNode (PathNode other) {
		this.setLocation(other.location[0], other.location[1], other.location[2]);
		this.left = other.left;
		this.right = other.right;
		this.down = other.down;
		this.rotate = other.rotate;
		this.up = other.up;
		this.unRotate = other.unRotate;
	}
	public PathNode (int vertical, int horizontal, int rotate) {
		this.setLocation(vertical, horizontal, rotate);
	}
	protected PathNode (int [] location) {
		this.setLocation(location[0], location[1], location[2]);
	}
	public PathNode (PathNode parent, Direction direction) {
		this (parent.location);
		switch (direction) {
		case DOWN : {
			setLocation(location[0]+1,location[1],location[2]);
			break;
		}
		case RIGHT : {
			setLocation(location[0],location[1]+1,location[2]);
			break;
		}
		case LEFT : {
			setLocation(location[0],location[1]-1,location[2]);
			break;
		}
		case ROTATE : {
			setLocation(location[0],location[1],location[2]+1);
			break;
		}
		}
	}
	public int setBariors () {
		int count = 0;
		if (down==null) {down = new BariorNode(this); count++;}
		if (unRotate==null) {unRotate = new BariorNode(this); count++;}
		return count;
	}
	public void setLeaf() {
		this.isLeaf = true;
	}
	public boolean isLeaf() {
		return this.isLeaf;
	}
	public String toString() {
		//return "down: " + down + ", right: " + right + ", left: " + left + ", rotate: " + rotate;
		return "<not null>";
	}
	public boolean isHead () {
		return location [0] == 0
				&& location [1] == 0
				&& location [2] == 0;
	}
	public int getVerticalAmount() {
		return location [0];
	}
	public int getHorizontalAmount() {
		return location [1];
	}
	public int getRotateAmount() {
		return location [2];
	}
	public PathNode getUp () {
		return this.up;
	}
	private void setLocation (int v, int h, int r) {
		this.location[0] = v;
		this.location[1] = h;
		this.location[2] = r%4;
	}
	public PathNode getUnRotate () {
		return this.unRotate;
	}
	public PathNode getLeft() {
		return left;
	}
	public PathNode getRight() {
		return right;
	}
	public PathNode getDown() {
		return down;
	}
	public PathNode getRotate() {
		return rotate;
	}
	public boolean complete () {
		return down != null
				&& left != null
				&& right != null
				&& rotate != null;
	}
	private boolean nodeCheck (PathNode node) {
		if (node==null) {
			System.out.println("WARNING: PathNode.java: A <null> node was added to path_node: "+Arrays.toString(location));
			return false;
		}
		return true;
	}
	protected void setLeft(PathNode left) {
		if (nodeCheck(left)) {
			this.left = left;
			left.right = this;
		}
	}
	protected void setRight(PathNode right) {
		if (nodeCheck(right)) {
			this.right = right;
			right.left = this;
		}
	}
	protected void setDown(PathNode down) {
		if (nodeCheck(down)) {
			this.down = down;
			down.up = this;
		}
	}
	protected void setRotate(PathNode rotate) {
		if (nodeCheck(rotate)) {
			this.rotate = rotate;
			rotate.unRotate = this;
		}
	}
	protected int [] getLocation () {
		return location;
	}
	public int compareTo (PathNode other) {
		/* =============================================
		 * location array has left-to-right significance
		 * =============================================
		 * ie: Two PathNode objects with identical "down"
		 * values need to be compared based on further criteria
		 * from the location array, while two PathNode objects 
		 * with differing down values will return a comparison 
		 * integer right away.
		 */
		for (int i=0;i<location.length;i++) {
			int thisVal = this.location[i];
			int otherVal = other.location[i];
			if (thisVal < otherVal) {
				return -1;
			} else if (thisVal > otherVal) {
				return 1;
			} else {
				continue;
			}
		}
		return 0;
	}
	public int hashCode () {
		return Arrays.hashCode(location);
	}
	public boolean equals(PathNode other) {
		return Arrays.equals(this.location, other.location);
	}
}
