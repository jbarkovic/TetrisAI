package ai.logic;

import java.util.Iterator;

import ai.state.SHAPETYPE;

public class GraphTools {
	private static double [] avgSolData = new double [200];
	private static int [] avgNullRate = new int [200];
	private static double avgSol = 0d;
	private static double avgNullCount = 0d;
	static {
		for (int i=0;i<avgSolData.length;i++) {
			avgSolData[i] = Double.NaN;
		}
		for (int i=0;i<avgNullRate.length;i++) {
			avgNullRate[i] = -1;
		}
	}
	
	public static PathNode getBestSolution (GameGraph graph, SolutionValue evaluator) {
		Iterator<PathNode> itr = graph.getNodes().iterator();
		double bestSolution = Double.NEGATIVE_INFINITY;
		PathNode best = null;
		
		
		int nullCount = 0;
		int solCount = 0;
		while (itr.hasNext()) {
			PathNode current = itr.next();
			ComputedValue cv = null;
			
			solCount ++;
			if ((cv=graph.getValue(current))!=null) {
				double currentValue = evaluator.calculateSolution(cv);
				if (currentValue >= bestSolution) {
					bestSolution = currentValue;
					best = current;
				}
			} else {
				nullCount++;
				//System.err.println("There was a null ComputedValue when finding the best solution");
			}
		}
		updateAvg(bestSolution);
		updateAvgNull(nullCount);

		
		SHAPETYPE sType = (best!= null) ? graph.getState(best).getShape().getType() : null;
		if (best == null) System.err.println("WARNING: Graph tools could not find a best solution, potential causes include no available solutions, graph had " + graph.size() + " nodes");
		/*else if (Math.abs((avgSol - bestSolution) / avgSol) >= 0.50d) {
			System.err.println(String.format("The best solution for a shape type: [%s] was below the cutoff relative to the average solutions", graph.getState(best).getShape().getType()));
		}*/
		
		double nullRateIncrease = ((double) nullCount)/ avgNullCount;
		/*if (nullRateIncrease >= 1.20d) {
			System.err.println(String.format("The number of null values (%d) of (%d) solutions is %2.4f greater than the average: %4.4f", nullCount, solCount, nullRateIncrease, avgNullCount ));
		}*/
		return best;
	}
	
	private static double updateAvgNull (int nullcount) {
		double avg = 0d;
		
		for (int i=0;i<avgNullRate.length-1;i++) {
			avgNullRate[i] = avgNullRate[i+1];
			
			if (avgNullRate[i] != Double.NaN) avg += avgNullRate[i];
		}
		
		avgNullRate[avgNullRate.length-1] = nullcount;
		avg += nullcount;
		
		avg /= avgNullRate.length;
		avgNullCount = avg;
		
		return avg;
	}
	
	private static double updateAvg (double solVal) {
		avgSol = 0d;
		for (int i=0;i<avgSolData.length-1;i++) {
			avgSolData[i] = avgSolData [i+1];
			if (avgSolData[i] != Double.NaN) avgSol += avgSolData[i];
		}
		
		avgSolData[avgSolData.length-1] = solVal;
		avgSol += solVal;
		avgSol /= avgSolData.length;
		
		return avgSol;
	}
	
	public static Path getShortestPath (GameGraph graph, PathNode node) {
		Dijkstras dkij = new Dijkstras(graph);
		dkij.shortestTo(node);
		return dkij.getPathToLastDestination();
	}
}
