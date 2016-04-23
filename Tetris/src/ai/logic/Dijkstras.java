package ai.logic;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class Dijkstras {
	private PriorityQueue<DijkstraNode> incomplete;
	private TreeMap<PathNode, DijkstraNode> allNodes;
	private TreeSet<DijkstraNode> dNodes;
	private DijkstraNode startNode;
	private DijkstraNode lastDestination;
	public Dijkstras (GameGraph graph) {
		allNodes = new TreeMap<PathNode, DijkstraNode> ();
		for (PathNode pn : graph.getNodes()) {
			if (pn == null) {
				System.err.println("GameGraph supplied a null node within Djikstras");
			} else if(!(pn instanceof BariorNode) )allNodes.put(pn,new DijkstraNode(pn, Integer.MAX_VALUE));
		}
	//	System.out.println("Dijkstras.java: Received " + allNodes.size() + " nodes");
		startNode = new DijkstraNode(graph.getStart(), Integer.MAX_VALUE);
		allNodes.remove(startNode.node);
		allNodes.put(startNode.node, startNode);
		dNodes = new TreeSet<DijkstraNode> (allNodes.values());
		incomplete = new PriorityQueue<DijkstraNode> (allNodes.values());
		lastDestination = startNode;
	}
	public int shortestTo (PathNode destination) {
		Iterator<DijkstraNode> itr = dNodes.iterator();
		while (itr.hasNext()) {
			itr.next().distance = Integer.MAX_VALUE;
		}
		DijkstraNode target = allNodes.get(destination);
		target.distance = 0;
		allNodes.remove(target.node);
		allNodes.put(target.node, target);
		
		incomplete.clear();
		incomplete.addAll(allNodes.values());
		
		DijkstraNode current = target;
		while (!incomplete.isEmpty() && incomplete.peek().distance < Integer.MAX_VALUE) {
			relax(current, getInternalNode (current.node.getDown()));
			relax(current, getInternalNode (current.node.getLeft()));
			relax(current, getInternalNode (current.node.getRight()));
			relax(current, getInternalNode (current.node.getRotate()));
			relax(current, getInternalNode (current.node.getUnRotate()));
			relax(current, getInternalNode (current.node.getUp()));
			incomplete.remove(current);
			current = incomplete.peek();
		}
	//	System.out.println("INFO: Djikstras.java: Found shortest path of " + startNode.distance + " units");
			//recursivePrint(4,0,lastDestination.node, null);
		return startNode.distance;
	}
	private void recursivePrint (int limit, int depth, PathNode node, PathNode parent) {
		String tabSeq = "";
		for (int i=0;i<depth;i++) {
			tabSeq+= "-|";
		}
		System.out.println(tabSeq + "Node types leaving target: ["+Arrays.toString(node.getLocation())+"] [distance at target: " + getInternalNode(node).distance);
		if (node.getDown()!=null)System.out.println(tabSeq+" drop: " + getInternalNode(node.getDown()).distance + " class: " + node.getDown().getClass());
		if (node.getLeft()!=null)System.out.println(tabSeq+" left: " + getInternalNode(node.getLeft()).distance + " class: " + node.getLeft().getClass());
		if (node.getRight()!=null)System.out.println(tabSeq+" righ: " + getInternalNode(node.getRight()).distance + " class: " + node.getRight().getClass());
		if (node.getRotate()!=null)System.out.println(tabSeq+" rota: " + getInternalNode(node.getRotate()).distance + " class: " + node.getRotate().getClass());
		if (node.getUnRotate()!=null)System.out.println(tabSeq+" unRo: " + getInternalNode(node.getUnRotate()).distance + " class: " + node.getUnRotate().getClass());
		if (node.getUp() != null) System.out.println(tabSeq+ " up: " + getInternalNode(node.getUp()).distance + " class: " + node.getUp().getClass());
		if (depth < limit) {
			int newDepth = depth + 1;
			System.out.print(tabSeq+ "Down: ");
			if (node.getDown()!=null && node.getDown() != parent) recursivePrint(limit, newDepth, node.getDown(), node);
			else System.out.println("\t" + tabSeq + "null");
			System.out.print(tabSeq+ "Right: ");
			if (node.getRight()!=null && node.getRight() != parent) recursivePrint(limit, newDepth, node.getRight(), node);
			else System.out.println("\t" + tabSeq + "null");
			System.out.print(tabSeq+ "Left: ");
			if (node.getLeft()!=null && node.getLeft() != parent) recursivePrint(limit, newDepth, node.getLeft(), node);
			else System.out.println("\t" + tabSeq + "null");
			System.out.print(tabSeq+ "Rotate: ");
			if (node.getRotate()!=null && node.getRotate() != parent) recursivePrint(limit, newDepth, node.getRotate(), node);
			else System.out.println("\t" + tabSeq + "null");
			System.out.print(tabSeq+ "UnRotate: ");
			if (node.getUnRotate()!=null && node.getUnRotate() != parent) recursivePrint(limit, newDepth, node.getUnRotate(), node);
			else System.out.println("\t" + tabSeq + "null");
			System.out.print(tabSeq+ "Up: ");
			if (node.getUp()!=null && node.getUp() != parent) recursivePrint(limit, newDepth, node.getUp(), node);
			else System.out.println("\t" + tabSeq + "null");
		}
		}
	public Path getPathToLastDestination () {
		DijkstraNode current = startNode;
	//	System.out.println("INFO: Dijkstras.java: getting a path with length " + current.distance);
		Path path = new Path();
		path.add(current.node);
		while (current.distance != 0) {
			String equalsMsg = "";
			DijkstraNode next = current;
			
			DijkstraNode other =  getInternalNode (current.node.getDown());
			if (other != null && (other.distance < next.distance || next==null)) next = other;
			else if (other != null && (other.distance==next.distance)) equalsMsg += "Down ";
			
			other =  getInternalNode (current.node.getRight());
			if (other != null && (other.distance < next.distance || next==null)) next = other;
			else if (other!=null && other.distance==next.distance) equalsMsg += "Right ";
			
			other =  getInternalNode (current.node.getLeft());
			if (other != null && (other.distance < next.distance||next==null)) next = other;
			else if (other != null && other.distance==next.distance) equalsMsg += "Left ";
			
			other =  getInternalNode (current.node.getRotate());
			if (other != null && (other.distance < next.distance || next==null)) next = other;
			else if (other != null && other.distance==next.distance) equalsMsg += "Rotate ";
			
			if (next==current) {
				recursivePrint(3,0,current.node, null);
				
				System.out.println("ERROR: Dijkstras.java: Could not find a next Node " + Arrays.toString(current.node.getLocation()) + " , (was still current after searching) (distance of: " + current.distance);
				System.out.println("Was Equal to: " + equalsMsg);
				System.out.println("Distances: current: " + current.distance 
						+ " Drop : " + getInternalNode(current.node.getDown()).distance + " " + Arrays.toString(getInternalNode(current.node.getDown()).node.getLocation())
						+ " Right: " + getInternalNode(current.node.getRight()).distance+ " " + Arrays.toString(getInternalNode(current.node.getRight()).node.getLocation())
						+ " Left:  " + getInternalNode(current.node.getLeft()).distance+ " " + Arrays.toString(getInternalNode(current.node.getLeft()).node.getLocation())
						+ " Rotate:" + getInternalNode(current.node.getRotate()).distance+ " " + Arrays.toString(getInternalNode(current.node.getRotate()).node.getLocation()));
				break;
			} else if (next.distance >= current.distance) {
				System.out.println("ERROR: Dijkstras.java: Not approaching a solution: currentDistance: " + current.distance + " next: " + next.distance + " at c: " + Arrays.toString(current.node.getLocation()) + " n: " + Arrays.toString(next.node.getLocation()));
				break;
			} else {
				current = next;
			}
			path.add(current.node);
		}
	//	System.out.println("INFO: Dijkstras.java: Path finding stopped at node: " + Arrays.toString(current.node.getLocation()) + " and had length " + path.size());
		return path;
	}
	private DijkstraNode getInternalNode (PathNode external) {
		if (external==null) {
			//System.out.println("WARNING: Dijkstras.java: Tried to get an internal version of null node");
			return null;
		} else {
			DijkstraNode internal = allNodes.get(external);
			if (internal==null) {
				//System.out.println("WARNINIG: Dijkstras.java could not find an internal node in " + allNodes.size() + " nodes for: " + Arrays.toString(external.getLocation()));
			}
			return internal;
		}
	}
	private int relax (DijkstraNode start, DijkstraNode end) {
		if (start==null || end==null) return Integer.MAX_VALUE;
		else if (!incomplete.contains(start) || !incomplete.contains(end)) return Integer.MAX_VALUE;
		else if (start.node instanceof BariorNode || end.node instanceof BariorNode) {
			if (start.node instanceof BariorNode) {
				start.distance = Integer.MAX_VALUE;
			}
			if (end.node instanceof BariorNode) {
				end.distance = Integer.MAX_VALUE;
			}
			return Integer.MAX_VALUE;
		}
		else if (start.distance < end.distance) {
			if (end.node.equals(start.node.getRotate())) {
				end.distance = start.distance + 4;
			} else {
				end.distance = start.distance + 1;
			}
			incomplete.remove(end);
			incomplete.add(end);
		}
		return end.distance;
	}
	private class DijkstraNode implements Comparable<DijkstraNode> {
		public PathNode node;
		public int distance;
		public DijkstraNode (PathNode node, int distance) {
			this.node = node;
			this.distance = distance;
		}
		public int hashCode () {
			return this.node.hashCode();
		}
		@Override
		public int compareTo(DijkstraNode other) {
			int dist = Integer.compare(this.distance, other.distance);
			if (dist== 0 && this.node!= null && other.node!=null) return this.node.compareTo(other.node); 
			else return dist;
		}
		public boolean equals(DijkstraNode other) {
			return this.node.equals(other.node);
		}
	}
}
