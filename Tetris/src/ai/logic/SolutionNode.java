package ai.logic;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import ai.state.RotationManager;
import ai.state.SHAPETYPE;
import ai.state.GameState;
import ai.state.ShapeState;
import ai.transformations.ShapeTransforms;


public class SolutionNode {
	private GameState ourState;
	private int recursionDepth = 0;
	private String hash;
	private ArrayList<int []> solutionPattern = new ArrayList<int []>();
	private SolutionNode solutionOwner = null;

	
	private static int numBottoms = 0;
	private SolutionNode next = null;

	private final int NOSOL = Integer.MIN_VALUE + 1;
	private double ourSolution = NOSOL;
	private int distance = Integer.MAX_VALUE;
	private SolutionDir parentToUs;
	private SolutionDir nextMove;
	
	//Log Equation
	private String message = "ERROR: SolutionNode: no Solution message found";
	//Temporary variable
	private static double highestScore = Double.NEGATIVE_INFINITY;
	
	private int distR     = Integer.MAX_VALUE;
	private int distL     = Integer.MAX_VALUE;
	private int distD     = Integer.MAX_VALUE;
	private int distR1    = Integer.MAX_VALUE;

	private double solFINAL = Double.NEGATIVE_INFINITY;
	
	private ShapeState nextShape = null; 
	
	private SolutionNode r  = null;
	private SolutionNode l  = null;
	private SolutionNode d  = null;
	private SolutionNode r1 = null;
	
	private final static Logger LOGGER = Logger.getLogger(SolutionNode.class.getName());
	static {
		LOGGER.setLevel(Level.SEVERE);		
	}	
	public enum SolutionDir {
		DOWN,RIGHT,LEFT,ROTATE1,ROTATE2,ROTATE3,START,PLUMMIT
	}
	/*public SolutionNode(GameState inState, SolutionNode parent, SolutionDir parentToUs, SHAPETYPE nextShapeType) {
		this (inState,parent,parentToUs);
	//	if (nextShapeType != null) this.nextShape = RotationManager.getStartState(nextShapeType);
		else this.nextShape = null;
	}*/
	/*private SolutionNode(GameState inState, SolutionNode parent, SolutionDir parentToUs, ShapeState _nextShape) {
		this (inState,parent,parentToUs);
		//if (_nextShape != null) this.nextShape = new ShapeState (_nextShape);
		else this.nextShape = null;
	}*/
	public SolutionNode(GameState inState, SolutionNode parent, SolutionDir parentToUs) {
		if (ourState != null && ourState.getShape() != null) hash = ourState.getShape().dumpState(null);
		else hash = "start";
		this.ourState = inState;
		this.parentToUs = parentToUs;
		if (this.parentToUs == SolutionDir.START) {
			KnownNodes.clear ();   // this is very important for correct operation without re-instantiation each time
		} else {
			KnownNodes.checkIn(this);
			recursionDepth = parent.recursionDepth+1;
		}
	}
	public double Solve() {
		if (this.ourSolution != NOSOL) return this.ourSolution; // if we have a solution, return it	
		if (this.parentToUs == SolutionDir.START) {
			message = "EMPTY MESSAGE";
			highestScore = Integer.MIN_VALUE;
			solutionOwner = null;
		}
		boolean canDrop = false;
		int numChildren = 0;
		// SOLVE THE RIGHT NODE
		GameState reuseable = new GameState (this.ourState);
		//if (ShapeTransforms.canShiftRight(this.ourState) && this.parentToUs != SolutionDir.LEFT) {
		if (ShapeTransforms.canShiftRight(this.ourState)) {			
			reuseable = ShapeTransforms.predictShiftRight(reuseable);
			GameState right = reuseable;
			//GameState right = ShapeTransforms.predictShiftRight(new GameState (this.ourState));
			this.r = KnownNodes.getNode(right);
			if (this.r == null) {
				numChildren++;
				this.r = new SolutionNode(right,this,SolutionDir.RIGHT/*,nextShape*/);
				this.r.Solve();
				this.distR = this.r.distance;
			}
			reuseable = ShapeTransforms.predictShiftLeft(reuseable);
		}
		// SOLVE THE LEFT NODE
		//if (ShapeTransforms.canShiftLeft(this.ourState)  && this.parentToUs != SolutionDir.RIGHT) {
		if (ShapeTransforms.canShiftLeft(this.ourState)) {
			reuseable = ShapeTransforms.predictShiftLeft(reuseable);
			GameState left = reuseable;
			//GameState left = ShapeTransforms.predictShiftLeft(new GameState (this.ourState));
			this.l = KnownNodes.getNode(left);
			if (this.l == null) {
				numChildren++;
				this.l = new SolutionNode(left,this,SolutionDir.LEFT/*,nextShape*/);
				this.l.Solve();
				this.distL = this.l.distance;
			}
			reuseable = ShapeTransforms.predictShiftRight(reuseable);
		}
		// SOLVE THE ROTATE1 NODE
		if (this.parentToUs != SolutionDir.ROTATE1) { // prevent infinite recursion
			if (ShapeTransforms.canRotate(this.ourState)) {
				GameState rotate1 = ShapeTransforms.predictRotate(new GameState (this.ourState));
				//reuseable = ShapeTransforms.predictRotate(rotate);
				this.r1 = KnownNodes.getNode(rotate1);
				if (this.r1 == null) {
					numChildren++;
					this.r1 = new SolutionNode(rotate1,this,SolutionDir.ROTATE1/*,nextShape*/);
					this.r1.Solve();
					this.distR1 = this.r1.distance;
				}
			}
		}
		// SOLVE THE DROP NODE --- note should be last
		if (ShapeTransforms.canDrop(this.ourState)) {
			reuseable = ShapeTransforms.predictDropOnce(reuseable);
			canDrop = true;
			GameState drop = reuseable;
			//GameState drop = ShapeTransforms.predictDropOnce(new GameState (this.ourState));
			this.d = KnownNodes.getNode(drop);
			if (this.d == null) {
				numChildren++;
				this.d = new SolutionNode(drop,this,SolutionDir.DOWN/*,nextShape*/);
				this.d.Solve();
				this.distD = this.d.distance;
			}
		}	
		double bestSolution = Double.NEGATIVE_INFINITY;
		// GET OUR SOLUTION AS IS
		if (!canDrop){//numChildren < 4) {
		/*	if (nextShape != null) {
				System.out.println ("Following trail");
				SolutionNode branch = new SolutionNode(new GameState (this.ourState.getBoardWithCurrentShape(),new ShapeState(nextShape)), null, SolutionDir.START);
				System.out.println ("branch next\n");
				System.out.println (branch.next);
				this.solFINAL = branch.Solve();
			} else {*/
				numBottoms++;
				//System.out.println ("Not following trail, numBottoms: " + numBottoms);
				reuseable = ShapeTransforms.predictCompleteDrop(reuseable);
				// REmoved when SolutionMaster was deleted
			//	this.solFINAL = (new SolutionValue()).calculateSolution(this.ourState, SolutionValue.getSolutionParameters(reuseable));
			//}
		}
		
		// COMBINE SOLUTION
		if (this.r  != null && this.r.hasFoundSolution()  && this.r.getSolutionVal()  >= bestSolution)  {bestSolution = this.r.getSolutionVal();}
		if (this.l  != null && this.l.hasFoundSolution()  && this.l.getSolutionVal()  >= bestSolution)  {bestSolution = this.l.getSolutionVal();}
		if (this.d  != null && this.d.hasFoundSolution()  && this.d.getSolutionVal()  >= bestSolution)  {bestSolution = this.d.getSolutionVal();}
		if (this.r1 != null && this.r1.hasFoundSolution() && this.r1.getSolutionVal() >= bestSolution)  {bestSolution = this.r1.getSolutionVal();}		
		
		//this.distance = 0;
		// The drop node is evaluated first with a (>) then the rest follow with a (>=) so that all other moves are done before any dropping unless absolutely necessary (safer)
		if (this.d  != null && this.d.hasFoundSolution()  && this.d.getSolutionVal()  == bestSolution && this.distance >= this.d.distance)  {this.distance = this.d.distance +1;}
		if (this.r  != null && this.r.hasFoundSolution()  && this.r.getSolutionVal()  == bestSolution && this.distance >= this.r.distance)  {this.distance = this.r.distance +3;}
		if (this.l  != null && this.l.hasFoundSolution()  && this.l.getSolutionVal()  == bestSolution && this.distance >= this.l.distance)  {this.distance = this.l.distance +3;}
		if (this.r1 != null && this.r1.hasFoundSolution() && this.r1.getSolutionVal() == bestSolution && this.distance >= this.r1.distance) {this.distance = this.r1.distance+3;}		
	
		if (this.solFINAL >= bestSolution) {
			bestSolution = this.solFINAL; 
			this.distance = 0;
			solutionOwner = this;
			LOGGER.info("WE WERE THE VERY BEST,\n  LIKE NO ONE EVER WAS: " + this.parentToUs.toString());
		}
		
		this.ourSolution = bestSolution;
		if (this.ourSolution > highestScore) {
			highestScore = this.ourSolution;
		}
		return bestSolution;
	}
	public String getMessage() {
		return message;
	}
	public void followSolution() {
		this.solutionOwner = followSolution (this.solutionPattern, 0);
	}
	private SolutionNode followSolution (ArrayList<int[]> solutionPattern, int solutionPatternPointer) {
		//solutionOwner = null;
		int bestDistance = Integer.MAX_VALUE - 1;
		this.nextMove = SolutionDir.PLUMMIT;
		this.next = null;
		
		if (this.d  != null) { this.distD  = this.d.distance;}  else {this.distD  = Integer.MAX_VALUE;}
		if (this.r  != null) { this.distR  = this.r.distance;}  else {this.distR  = Integer.MAX_VALUE;}
		if (this.l  != null) { this.distL  = this.l.distance;}  else {this.distL  = Integer.MAX_VALUE;}
		if (this.r1 != null) { this.distR1 = this.r1.distance;} else {this.distR1 = Integer.MAX_VALUE;}
		// Check for solution from neighbors equal to ours, with shortest path
		if (this.solFINAL == this.ourSolution) {
			this.next = null;
			this.nextMove = SolutionDir.PLUMMIT;
//		if (solutionOwner == this) {
//			this.next = null;
//			this.nextMove = SolutionDir.PLUMMIT;
		} else {			
			if (this.distR  < bestDistance && this.r.getSolutionVal()  == this.ourSolution) {bestDistance = this.distR;  this.next = this.r;this.nextMove  = SolutionDir.RIGHT;}
			if (this.distL  < bestDistance && this.l.getSolutionVal()  == this.ourSolution) {bestDistance = this.distL;  this.next = this.l;this.nextMove  = SolutionDir.LEFT;}
			if (this.distR1 < bestDistance && this.r1.getSolutionVal() == this.ourSolution) {bestDistance = this.distR1; this.next = this.r1;this.nextMove = SolutionDir.ROTATE1;}
			if (this.distD  < bestDistance && this.d.getSolutionVal()  == this.ourSolution) {bestDistance = this.distD;  this.next = this.d;this.nextMove  = SolutionDir.DOWN;}
		}
		switch (this.nextMove) {
			//TODO replace {0,0,-1}arrays with enums and/or constants defined in Solution Master;
			case LEFT : 	{ solutionPattern.add( new int[] {0,-1,0}); /**LOGGER.info("LEFT")**/; break;}
			case RIGHT : 	{ solutionPattern.add( new int[] {0,1,0}); /**LOGGER.info("RIGHT");**/ break;}
			case DOWN : 	{ solutionPattern.add( new int[] {0,0,1}); /**LOGGER.info("DOWN")**/;break;}
			case ROTATE1 : 	{ solutionPattern.add( new int[] {1,0,0}); /**LOGGER.info("ROTATE1");**/break;}
			case PLUMMIT : 	{ solutionPattern.add( new int[] {0,0,-1}); /**LOGGER.info("PLUMMIT");**/LOGGER.info("Notice: adding a plummit to the pattern");if (this.next != null) LOGGER.info("was PLUMMIT, but have next move?");break;}
			default : 		{ solutionPattern.add( new int[] {0,0,0}); LOGGER.info("Notice: using default for some reason");break;}
		}
		if (this.next != null) {
			this.next.followSolution(solutionPattern, solutionPatternPointer);
		} // THE END OF THE RECURSION, ALSO THE LAST MOVE FOR THE CURRENT SHAPE
		else {
			solutionOwner = this;
		}
		return this;
	}
	public boolean hasFoundSolution() {
		return this.ourSolution > NOSOL;
	}
	public double getSolutionVal() {
		return this.ourSolution;
	}
	private int[][] getCoordsNoCopy() {
		return this.ourState.getShape().getCoords();
	}
	public int[][] getSolutionCoords () {
		if (solutionOwner == null) {
			return new int[1][3];
		} else {
			return solutionOwner.getCoordsNoCopy();
		}
	}
	public int[][] getSolution () {
		return Arrays.copyOf(solutionPattern.toArray(new int [][] {{}}), solutionPattern.size());
		//int end = solutionPatternPointer;
		/* [][] output = new int[solutionPattern.length][3];
		for (int i=0;i<output.length;i++) {
			output[i][0] = solutionPattern.get(i)[0];
			output[i][1] = solutionPattern.get(i)[1];
			output[i][2] = solutionPattern.get(i)[2];
		}
		return output;*/
	}
	public String getLastKnownMessage () {
		if (solutionOwner == null) return "Null Solution Owner";
		return solutionOwner.message;
	}
	private static class KnownNodes {
		private static Map<String, SolutionNode> knownNodes = new HashMap<String, SolutionNode> ();
		protected static void checkIn(SolutionNode node) {
			knownNodes.put(node.ourState.getShape().dumpState(null), node);
		}
		protected int size () {
			return knownNodes.size();
		}
		protected static SolutionNode getNode(GameState inState) {
			return knownNodes.get(inState.getShape().dumpState(null));
		}
		protected static void clear () {
			knownNodes.clear(); // this is important for correct operation without re-instantiation each time
		}
	}
}
