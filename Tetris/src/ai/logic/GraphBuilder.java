package ai.logic;

import ai.state.GameState;
import ai.state.RotationManager;
import ai.transformations.ShapeTransforms;

public class GraphBuilder {

	private GraphBuilder () {
	}
	
	public static GameGraph buildGraph (GameState startState, RotationManager rotationManager) {
		if (startState == null) return null;
		return recursiveBuild(new GameGraph(startState), rotationManager);
	}
	private static GameGraph recursiveBuild (GameGraph graph, RotationManager rotationManager) {
		recursiveNodeExplore(graph, graph.getStart(), rotationManager);
		//System.out.println("Built a graph with " + graph.size() + " nodes");
		return graph;
	}
	private static PathNode recursiveNodeExplore (GameGraph graph, PathNode node, RotationManager rotationManager) {
		if (node!=null && graph!=null && !node.complete()) {	
			
			recursiveNodeExplore (graph, tryDrop(graph, node), rotationManager);

			recursiveNodeExplore (graph, tryShiftRight(graph, node), rotationManager);
			recursiveNodeExplore (graph, tryShiftLeft(graph, node), rotationManager);
			recursiveNodeExplore (graph, tryRotate(graph, node, rotationManager), rotationManager);
			node.setBariors();
			

		} else {
		}
		return node;
	}
	private static PathNode tryDrop (GameGraph graph, PathNode parentNode) {
		if (parentNode==null || graph==null) return null;
		PathNode dropNode = null;
		if ((dropNode=parentNode.getDown())==null) {
			GameState nodeState = graph.getState(parentNode);
			//System.out.println("GraphBuilder.java: Trying to drop: " + Arrays.toString(parentNode.getLocation()));
			if (nodeState==null) {
				System.out.println("ERROR: GraphBuilder.java: No state could be found in tryDrop");
				return null;
			}
			else if (ShapeTransforms.canDrop(nodeState)) {
				nodeState = new GameState (nodeState);
				ShapeTransforms.predictDropOnce(nodeState);
				dropNode = new PathNode (parentNode, PathNode.Direction.DOWN);
				dropNode = graph.getCanonical(dropNode); // No duplicates
				graph.addNode(dropNode);
				graph.attatchState(dropNode, nodeState);
			} else {
				// Can't drop, need to evaluate this solution later, so make it leaf node
				parentNode.setLeaf();
				parentNode.setDown(new BariorNode(parentNode));
				return null;
			}
			if (dropNode==null) System.out.println("Null dropnode");
			parentNode.setDown(dropNode);
			return dropNode;
		} else {
			//System.out.println("\tGraphBuilder.java: Has a drop");
		}
		return null;
	}
	private static PathNode tryShiftRight (GameGraph graph, PathNode parentNode) {
		if (parentNode==null || graph==null) return null;
		PathNode rightNode = null;
		if ((rightNode=parentNode.getRight())==null) {
			GameState nodeState = graph.getState(parentNode);
			//System.out.println("GraphBuilder.java: Trying to right "+ Arrays.toString(parentNode.getLocation()));
			if (nodeState==null) {
				System.out.println("ERROR: GraphBuilder.java: No state could be found in tryShiftRight");
				return null;
			}
			else if (ShapeTransforms.canShiftRight(nodeState)) {
				nodeState = new GameState (nodeState);
				ShapeTransforms.predictShiftRight(nodeState);
				rightNode = new PathNode (parentNode, PathNode.Direction.RIGHT);
				rightNode = graph.getCanonical(rightNode); // No duplicates
				
				graph.addNode(rightNode);
				graph.attatchState(rightNode, nodeState);
			} else {
				//System.out.println("Can no longer go right");
				rightNode = new BariorNode (parentNode);
				parentNode.setRight(rightNode);
				return null;
			}
			parentNode.setRight(rightNode);
			return rightNode;
		} else {
			//System.out.println("\tGraphBuilder.java: Has a right");
		}
		return null;
	}
	private static PathNode tryShiftLeft (GameGraph graph, PathNode parentNode) {
		if (parentNode==null || graph==null) return null;
		PathNode leftNode = null;
		if ((leftNode=parentNode.getLeft())==null) {
			GameState nodeState = graph.getState(parentNode);
			//System.out.println("GraphBuilder.java: Trying to left "+ Arrays.toString(parentNode.getLocation()));
			if (nodeState==null) {
				System.out.println("ERROR: GraphBuilder.java: No state could be found in tryShiftLeft");
				return null;
			}
			else if (ShapeTransforms.canShiftLeft(nodeState)) {
				nodeState = new GameState (nodeState);
				ShapeTransforms.predictShiftLeft(nodeState);
				leftNode = new PathNode (parentNode, PathNode.Direction.LEFT);
				leftNode = graph.getCanonical(leftNode); // No duplicates
				graph.addNode(leftNode);
				graph.attatchState(leftNode, nodeState);
			} else {
				//System.out.println("Can no longer go left");
				leftNode = new BariorNode (parentNode);
				parentNode.setLeft(leftNode);
				return null;
			}
			parentNode.setLeft(leftNode);
			return leftNode;
		} else {
			//System.out.println("\tGraphBuilder.java: Has a left");
		}
		return null;
	}
	private static PathNode tryRotate (GameGraph graph, PathNode parentNode, RotationManager rotationManager) {
		if (parentNode==null || graph==null) return null;
		PathNode rotateNode = null;
		if (parentNode.getLocation()[2] > 10 ) {
			System.out.println ("Rotate too large");
			return null;
		}
		if ((rotateNode=parentNode.getRotate())==null) {
			GameState nodeState = graph.getState(parentNode);
		//	System.out.println("GraphBuilder.java: Trying to rotate "+ Arrays.toString(parentNode.getLocation()));
			if (nodeState==null) {
				System.out.println("ERROR: GraphBuilder.java: No state could be found in tryRotate");
				return null;
			}
			else if (ShapeTransforms.canRotate(nodeState, rotationManager)) {
				nodeState = new GameState (nodeState);
				ShapeTransforms.predictRotate(nodeState, rotationManager);
				rotateNode = new PathNode (parentNode, PathNode.Direction.ROTATE);
				rotateNode = graph.getCanonical(rotateNode, nodeState); // No duplicates
				
				graph.addNode(rotateNode);
				graph.attatchState(rotateNode, nodeState);
			} else {
				rotateNode = new BariorNode (parentNode);
				parentNode.setRotate(rotateNode);
			//	System.out.println("Can no longer rotate");
				return null;
			}
			parentNode.setRotate(rotateNode);
			return rotateNode;
		} else {
			//System.out.println("\tGraphBuilder.java: Has a rotate");
		}
		return null;
	}
}
