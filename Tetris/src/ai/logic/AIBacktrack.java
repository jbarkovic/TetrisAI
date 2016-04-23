package ai.logic;

import interfaces.ControlMovements;



import ai.state.GameState;
import ai.state.RotationManager;
import ai.transformations.BoardTransforms;
import ai.transformations.ShapeTransforms;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AIBacktrack {
		private static final Logger LOGGER = Logger.getLogger(AI.class.getName());
		SolutionValue solVal;
		ArrayList<int []> shortestNew = new ArrayList<int []> (40);
		Solution lastSolution;
		
		long timeToBuildGraph = 0;
		long timeToCalcSols = 0;
		long totalTime = 0;
		
		long numSols = 0;
		
		static {
			//LOGGER.setLevel(Logger.getGlobal().getLevel());
			LOGGER.setLevel(Level.OFF);
		}

		public AIBacktrack() {
			this (10, false);
		}
		public AIBacktrack(int AISpeed, boolean usePlummit) {
			solVal = new SolutionValue();
		}
		public synchronized Solution decideSOL(GameState state, RotationManager rotationManager) {
			long startTime = System.nanoTime();
			Solution solution = new Solution();
			numSols++;
				if (!rotationManager.doWeKnowShapeYet(state.getShape().getType())) {
					state = ShapeTransforms.predictCompleteDrop(new GameState(state));
					solution.finalState = state;
					solution.setValue(-1);
				} else {				
					long graphBuildStart = System.nanoTime();
					GameGraph graph = GraphBuilder.buildGraph(state, rotationManager);
					long graphBuildEnd = System.nanoTime();
					graph.calculateSolutions();
					long graphCalcEnd = System.nanoTime();
					//graph.audit();
					PathNode destination = GraphTools.getBestSolution(graph, solVal);
					if (destination==null) System.out.println("ERROR: SolutionMaster.java: Graphtools produced a null best node");
					Path shortestPath = GraphTools.getShortestPath(graph, destination);
					
					shortestNew = shortestPath.toControlSequence();
					solution.finalState = graph.getState(destination);
					for (int [] patt : shortestNew ) {
						solution.steps.add(patt);
					}
					solution.setValue(solVal.calculateSolution(graph.getValue(destination)));	
					
					this.timeToBuildGraph += (graphBuildEnd - graphBuildStart);
					this.timeToCalcSols += (graphCalcEnd - graphBuildEnd);
			}
			long finishedTime = System.nanoTime();
			
			
			//if (numSols % 30 == 1) System.out.println(String.format("Avg Time to build:%6.5f, Avg time to Calc:%6.5f", timeToBuildGraph/(double)(numSols * 1000000), timeToCalcSols/(double) (numSols*1000000) ));
			lastSolution = solution;
			
			return solution;
		}
		public synchronized ControlMovements decideCM(GameState state, RotationManager rotationManager) {
			if (!rotationManager.doWeKnowShapeYet(state.getShape().getType())) {
				return new ControlMovements ();
			} else {
				Solution solution = decideSOL(state, rotationManager);
				ControlMovements sequence = solution.getSequence();
				return sequence;
			}
		}
		
		public synchronized boolean isFinalStateCorrrect (GameState finalState) {
			
			if (lastSolution == null || lastSolution.finalState == null || finalState == null) {
				System.err.println("Filan state null");
				return false;
			} else {
				GameState desiredState = new GameState (lastSolution.finalState);
				BoardTransforms.clearFullRows(desiredState);
				if (finalState.getBoardWithoutCurrentShape().colorBlindEquals(desiredState.getBoardWithCurrentShape())) {
					return true;
				} else {	
					System.out.print("Previous:");
					System.out.println(lastSolution.finalState.getBoardWithCurrentShape().dumpBoard());
					System.out.print("Cleared:");
					System.out.println(desiredState.getBoardWithCurrentShape().dumpBoard());
					System.out.print("Current:");
					System.out.println(finalState.getBoardWithCurrentShape().dumpBoard());
					return false;
				}
				
			}
		}
	}
