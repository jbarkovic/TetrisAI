package ai.logic;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import ai.state.GameState;

public class GameGraph {
	private PathNode startNode;
	private Set<PathNode> knownNodes;
	private TreeMap<PathNode, GameState> nodeStates;
	private TreeMap<PathNode, ComputedValue> nodeValues;
	
	{
		startNode = new PathNode (0,0,0);
	}
	public GameGraph (GameState startState) {
		knownNodes = new TreeSet<PathNode> ();
		nodeValues = new TreeMap<PathNode, ComputedValue>();
		nodeStates = new TreeMap<PathNode, GameState>();

		addNode(startNode);
		attatchState(startNode, startState);
		
	}
	public void addNode (PathNode node) {
		if (node!=null) {
			knownNodes.add(node);
		}
	}
	public void replaceNode (PathNode node) {
		if (node==null) return;
		else if (knownNodes.contains(node)){
			knownNodes.remove(node);
			knownNodes.add(node);
		} else {
			knownNodes.add(node);
		}
	}
	public GameState getState (PathNode node) {
		return nodeStates.get(node);
	}
	public ComputedValue getValue (PathNode node) {
		return nodeValues.get(node);
	}
	public Set<PathNode> getNodes() {
		return this.knownNodes;
	}
	public int size () {
		return this.knownNodes.size();
	}
	public boolean contains (PathNode node) {
		return knownNodes.contains(node);
	}
	public PathNode getStart () {
		if (startNode == null) System.out.println("ERROR: GameGraph.java: startNode is null");
		return startNode;
	}
	public void calculateSolutions () {
		Iterator<PathNode> itr = knownNodes.iterator();
		int count=0;
		SolutionValue solVal = new SolutionValue();
		while (itr.hasNext()) {
			PathNode current = itr.next();
			if (current != null && current.isLeaf()) {
				count++;
				GameState state = nodeStates.get(current);
				if (state != null) {
					attatchValue(current, solVal.getSolutionParameters(state));
				} else {
					System.out.println("ERROR: GameGraph.java: A leaf node was not assigned a GameState at :" + Arrays.toString(current.getLocation()));
				}
			}
		}
	//	System.out.println("GameGraph.java: Found " + count + " LeafNodes");
	//	System.out.println("GameGraph.java: Calculated " + nodeValues.size() + " Solutions");
	}
	public PathNode getCanonical (PathNode node) {
		Iterator<PathNode> itr = knownNodes.iterator();
		while (itr.hasNext()) {
			PathNode check = itr.next();
			if (check != null && node.equals(check)) {
				node = check;
				break;
			}
			
		}
		return node;
	}
	public PathNode getCanonical (PathNode node, GameState state) {
		if (state == null) return getCanonical (node);
		else if (state != null && nodeStates.containsValue(state)) {
			Iterator<Entry<PathNode, GameState>> itr = nodeStates.entrySet().iterator();
			while (itr.hasNext()) {
				Entry<PathNode, GameState> current = itr.next();
				if (current.getValue().equals(state)) {
					return current.getKey();
				}
			}
		}
		return getCanonical(node);
	}
	public boolean audit () {
		int nullChildrenCount = 0;
		int nullLeaf = 0;
		int nullBarior = 0;
		int nullPathNode = 0;
		int down = 0;
		int right = 0; 
		int left = 0;
		int rotate = 0;
		int up = 0;
		int unRotate = 0;
		for (PathNode pn : knownNodes) {
			int old = nullChildrenCount;
			if (pn.getDown()==null) {nullChildrenCount++; down++;}
			if (pn.getLeft()==null) {nullChildrenCount++; left++;}
			if (pn.getRight()==null) {nullChildrenCount++; right++;}
			if (pn.getRotate()==null) {nullChildrenCount++; rotate++;}
			if (pn.getUnRotate()==null) {nullChildrenCount++; unRotate++;}
			if (pn.getUp()==null) {nullChildrenCount++; up++;}
			if (nullChildrenCount > old) {
				if (pn.isLeaf()) {
					nullLeaf++;
				} else if (pn instanceof BariorNode) {
					nullBarior++;
				} else {
					nullPathNode++;
				}
			}
		}
		if (nullChildrenCount > 0) {
			System.out.println(nullChildrenCount + " null children were found");
			System.out.println("\t"+nullLeaf+" of those were children of LeafNodes");
			System.out.println("\t"+nullBarior+" of those were children of BariorNodes");
			System.out.println("\t"+nullPathNode+" of those were children of PathNodes\n");
			System.out.println("\t\t" + down+" of the null children were down nodes");
			System.out.println("\t\t" + left+" of the null children were left nodes");
			System.out.println("\t\t" + right+" of the null children were right nodes");
			System.out.println("\t\t" + rotate+" of the null children were rotate nodes");
			System.out.println("\t\t" + unRotate+" of the null children were unRotate nodes");
			System.out.println("\t\t" + up+" of the null children were up nodes");
			return true;
		}
		return false;
	}
	public void clearAll () {
		knownNodes.clear();
		nodeValues.clear();
		nodeStates.clear();
		knownNodes.add(startNode);
	}
	public boolean removeNode (PathNode node) {
		if (node.equals(startNode)) {
			return nodeValues.remove(node) != null
					|| nodeStates.remove(node) != null;
		}
		return knownNodes.remove(node)
				|| nodeValues.remove(node) != null
				|| nodeStates.remove(node) != null;
		}
	public void attatchState (PathNode node, GameState state) {
		nodeStates.put(node, state);
	}
	public void attatchValue (PathNode node, ComputedValue value) {
		nodeValues.put(node, value);
	}
}
