package ai.logic;

import interfaces.ControlMovements;



import ai.state.GameState;
import ai.state.RotationManager;
import ai.transformations.ShapeTransforms;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AIBacktrack {
		private static final Logger LOGGER = Logger.getLogger(AI.class.getName());
		SolutionValue solVal;
		
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
		public synchronized Solution decideSOL(GameState state) {
			Solution solution = new Solution();
				if (!RotationManager.doWeKnowShapeYet(state.getShape().getType())) {
					state = ShapeTransforms.predictCompleteDrop(new GameState(state));
					solution.finalState = state;
					solution.setValue(-1);
				} else {				
					
					GameGraph graph = GraphBuilder.buildGraph(state);
					graph.calculateSolutions();
					//graph.audit();
					PathNode destination = GraphTools.getBestSolution(graph, solVal);
					if (destination==null) System.out.println("ERROR: SolutionMaster.java: Graphtools produced a null best node");
					Path shortestPath = GraphTools.getShortestPath(graph, destination);
					
					ArrayList<int []> shortestNew = shortestPath.toControlSequence();
					solution.finalState = graph.getState(destination);
					for (int [] patt : shortestNew ) {
						solution.steps.add(patt);
					}
					solution.setValue(solVal.calculateSolution(graph.getValue(destination)));	

					
			}
			return solution;
		}
		public synchronized ControlMovements decideCM(GameState state) {
			if (!RotationManager.doWeKnowShapeYet(state.getShape().getType())) {
				return new ControlMovements ();
			} else {
				Solution solution = decideSOL(state);
				ControlMovements sequence = solution.getSequence();
				sequence.setAI(this);
				return sequence;
			}
		}
	}
