package ai.logic;

import java.util.Iterator;

public class GraphTools {
	public static PathNode getBestSolution (GameGraph graph, SolutionValue evaluator) {
		Iterator<PathNode> itr = graph.getNodes().iterator();
		double bestSolution = Double.NEGATIVE_INFINITY;
		PathNode best = null;
		while (itr.hasNext()) {
			PathNode current = itr.next();
			ComputedValue cv = null;
			if ((cv=graph.getValue(current))!=null) {
				double currentValue = evaluator.calculateSolution(cv);
				if (currentValue > bestSolution) {
					bestSolution = currentValue;
					best = current;
				}
			}
		}
		return best;
	}
	public static Path getShortestPath (GameGraph graph, PathNode node) {
		Dijkstras dkij = new Dijkstras(graph);
		dkij.shortestTo(node);
		return dkij.getPathToLastDestination();
	}
}
