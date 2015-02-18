package ai.logic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.PriorityQueue;

import ai.logic.Solution.DIRECTION;

public class ShapeMemory {
	private ShapeNode [] nodes = new ShapeNode [10];
	private ShapeNode start = null;
	private int end = 0;
	public ShapeMemory () {		
	}
	public void add (ShapeNode node) {
		if (end >= nodes.length-1) {
			System.out.println("Increasing size");
			ShapeNode [] newNodeList = new ShapeNode [(nodes.length+1)*2];
			System.out.println ("newNodeList length: " + newNodeList.length*2);
			newNodeList = Arrays.copyOf(nodes, nodes.length*2);
			System.out.println ("newNodeList length: " + newNodeList.length);
			nodes = newNodeList;
			System.out.println ("new length: " + nodes.length);
		}
		if (start == null) start = node;
		nodes[end] = node;
		end++;
	}
	public int length () {
		return end;
	}
	public DIRECTION [] findPath (ShapeNode start, ShapeNode endNode) {
		ArrayList<DIRECTION> path = new ArrayList<DIRECTION> ();
		int [] prev = new int [nodes.length];
		DIRECTION [] prevDirections = new DIRECTION [nodes.length];
		PriorityQueue<PathNode> unvisited = new PriorityQueue<PathNode> ();
		PathNode pathEnd = null;
		for (int i=0;i<end;i++) {
			PathNode currentNode;
			if (!nodes[i].equals(start)) {
				 currentNode = new PathNode(i,nodes[i],null,Integer.MAX_VALUE);
				 prev[i] = -1;
			} else {
				currentNode = new PathNode(i,nodes[i],null,0);
				prev[i] = -2;
			}
			prevDirections[i] = null;
			unvisited.add(currentNode);
			if (nodes[i].equals(endNode)) {
				pathEnd = currentNode;
			}
		}
		while (unvisited.size() > 0) {
			PathNode u = unvisited.poll();
			for (int i=0;i<u.current.neighbours.length;i++) {
				if (!unvisited.contains(u.current.neighbours[i])) continue;
				ShapeNode v = u.current.neighbours[i]; 
				int altDist = u.distance() + 1;
				if (altDist < v.distance) {
					v.distance = altDist;
					for (int j=0;j < end;j++) {
						if (nodes[j].equals(v)) {
							prev[j] = u.index;
							prevDirections[j] = u.current.dirs[i];
							break;
						}
					}
				}
			}			
		}
		if (pathEnd != null) {
			int index = pathEnd.index;
			while (index != -2) {
				path.add(0, prevDirections[index]);
				index = prev[index];
				if (index == -1) System.err.println ("PathFinding could not reach source, index value is -1");
			}
		}
		return path.toArray(new DIRECTION [] {});
	}
	public ShapeNode getInitialState () {
		return start;
	}
	public ShapeNode get (int index) {
		return nodes[index];
	}
	public void sort () {
		int length_unsorted = end;
		while (length_unsorted > 0) {
			heapify (0,length_unsorted);
			swap (0,--length_unsorted);
		}		
	}
	public ShapeNode getLast () {
		if (end -1 < 0) return null;
		return nodes [end-1];
	}
	public boolean contains (ShapeNode node) {
		for (int i=0;i<end;i++) {
			if (nodes[i].equals(node)) return true;
		}
		return false;
	}
	private int getChild (int child, int node) {
		return node * ShapeNode.NUM_CHILDREN + child;
	}
	private void swap (int item1, int item2) {
		ShapeNode temp = nodes [item1];
		nodes[item1] = nodes[item2];
		nodes[item2] = temp;
	}
	private ShapeNode [] heapify (int start, int length) {
		for (int i=1;i<=length;i*= 4) {
			recursiveHeapify(start,length);
		}
		return nodes;
	}
	private void recursiveHeapify (int node, int length) {
		for (int i=1;i<=ShapeNode.NUM_CHILDREN;i++) {
			if (getChild (i,node) < length && nodes[node].value < nodes[getChild(i,node)].value) {
				swap (node, getChild(node,i));
			}
		}
		for (int i=1;i<=ShapeNode.NUM_CHILDREN;i++) {
			if (getChild (i,node) < length) {
				recursiveHeapify(getChild(i,node),length);
			}
		}
	}
	private class PathNode implements Comparable<PathNode>{	
		ShapeNode current;
		ShapeNode prev;
		int index;
		public PathNode (int index, ShapeNode current, ShapeNode previous, int distance) {
			this.index = index; this.current = current; this.prev = previous; this.current.distance = distance;
		}
		public int compareTo (PathNode other) {
			if (this.current.distance == other.current.distance) return 0;
			else return (this.current.distance > other.current.distance) ? 1 : -1;
		}
		public int distance() {
			return this.current.distance;
		}
		public boolean equals (PathNode other) {
			return this.current.equals(other.current);
		}
	}
}
