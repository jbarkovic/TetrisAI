package ai.logic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import ai.state.GameState;
import ai.transformations.BoardTransforms;
import ai.transformations.ShapeTransforms;

public class SolutionNodeFullBackTrack {
	protected GameState ourState;
	
	private int recursionLevel = 0;
	private static int [][] solutionPattern = new int[30][3];
	private static int solutionPatternPointer = 0;
	private static SolutionNodeFullBackTrack solutionOwner = null;
	//private final SolutionNode parent;
	private SolutionNodeFullBackTrack next = null;
	//private final SolutionMaster master;
	private static final int NEGINF = -900000000;
	private static final int POSINF = 900000000;
	private static final int NOSOL = NEGINF + 1;
	private int ourSolution = NOSOL;
	private int distance = POSINF;
	private SolutionDir parentToUs;
	private SolutionDir nextMove;
	
	//Log Equation
	private String message = "ERROR: SolutionNode: no Solution message found";
	//Temporary variable
	private static int highestScore = POSINF;
	
	private int distR     = POSINF;
	private int distL     = POSINF;
	private int distD     = POSINF;
	private int distR1    = POSINF;
	private int distR2    = POSINF;
	private int distR3    = POSINF;

	private int solFINAL = NEGINF;
	
	private SolutionNodeFullBackTrack r  = null;
	private SolutionNodeFullBackTrack l  = null;
	private SolutionNodeFullBackTrack d  = null;
	private SolutionNodeFullBackTrack r1 = null;
	private SolutionNodeFullBackTrack r2 = null;
	private SolutionNodeFullBackTrack r3 = null;
	
	private final static Logger LOGGER = Logger.getLogger(SolutionNode.class.getName());
	static {
		LOGGER.setLevel(Level.INFO);		
	}
	
	public enum SolutionDir {
		DOWN,RIGHT,LEFT,ROTATE1,ROTATE2,ROTATE3,START,PLUMMIT
	}
	
	public SolutionNodeFullBackTrack(GameState inState, SolutionNodeFullBackTrack parent, SolutionDir parentToUs) {
		this.ourState = inState;
		KnownNodes.checkIn(this);
		this.parentToUs = parentToUs;
		if (this.parentToUs == SolutionDir.START) {
			this.distance = POSINF;
			KnownNodes.clear ();   // this is very important for correct operation without re-instantiation each time
		}
	}
	public int Solve() {
		this.ourSolution = 0;
		if (this.parentToUs == SolutionDir.START) {
			message = "EMPTY MESSAGE";
			highestScore = NEGINF;
			solutionPatternPointer = 0;
			solutionOwner = null;
		}
		System.out.println("Solving At Recursion Level: " + this.recursionLevel);;
		// SOLVE THE RIGHT NODE
		if (ShapeTransforms.canShiftRight(this.ourState) && this.parentToUs != SolutionDir.LEFT) {
			//System.out.println("CAN RIGHT");
			GameState right = ShapeTransforms.predictShiftRight(new GameState (this.ourState));
			this.r = KnownNodes.getNode(right);
			if (this.r == null) {
			//	LOGGER.info("Right...");				
				this.r = new SolutionNodeFullBackTrack(right,this,SolutionDir.RIGHT);
				KnownNodes.checkIn(this.r);
				this.r.Solve();
				this.distR = this.r.getDistance();
				if (this.r.hasFoundSolution() && this.r.getSolutionVal() < this.ourSolution) {
					this.ourSolution = this.r.getSolutionVal();
					this.next = this.r;
				}
				//LOGGER.info("SR sol: " + this.solR);
			} else {
				//if (!this.r.hasFoundSolution()) this.r = null;
			}
		} else {
			this.r = null;
		}
		// SOLVE THE LEFT NODE
		if (ShapeTransforms.canShiftLeft(this.ourState)  && this.parentToUs != SolutionDir.RIGHT) {
			//System.out.println("CAN LEFT");
			GameState left = ShapeTransforms.predictShiftLeft(new GameState (this.ourState));
			this.l = KnownNodes.getNode(left);
			if (this.l == null) {
			//	LOGGER.info("Left...");
				this.l = new SolutionNodeFullBackTrack(left,this,SolutionDir.LEFT);
				KnownNodes.checkIn(this.l);
				this.l.Solve();
				this.distL = this.l.getDistance();
				if (this.l.hasFoundSolution() && this.l.getSolutionVal() < this.ourSolution) {
					this.ourSolution = this.l.getSolutionVal();
					this.next = this.l;
				}
				//LOGGER.info("LSOL: " + this.solL);
			} else {
				//if (!this.l.hasFoundSolution()) this.l = null;
			}
		} else {
			this.l = null;
		}
		if ((this.parentToUs == SolutionDir.START) || (this.parentToUs != SolutionDir.ROTATE1 && this.parentToUs != SolutionDir.ROTATE2 && this.parentToUs!= SolutionDir.ROTATE3)) { // prevent infinite recursion
			// SOLVE THE ROTATE1 NODE
			if (ShapeTransforms.canRotate(this.ourState)) {
			//	System.out.println("CAN ROT1");
				GameState rotate1 = ShapeTransforms.predictRotate(new GameState (this.ourState));
				this.r1 = KnownNodes.getNode(rotate1);
				if (this.r1 == null) {
				//LOGGER.info("Rotate1...");
					this.r1 = new SolutionNodeFullBackTrack(rotate1,this,SolutionDir.ROTATE1);
					KnownNodes.checkIn(this.r1);
					this.r1.Solve();
					this.distR1 = this.r1.getDistance();
					if (this.r1.hasFoundSolution() && this.r1.getSolutionVal() < this.ourSolution) {
						this.ourSolution = this.r1.getSolutionVal();
						this.next = this.r1;
					}
				} else {
					this.r1 = null;
					//if (!this.r1.hasFoundSolution()) this.r1 = null;
				}
			} else {
				this.r1 = null;
			}
		} else {
			this.r1 = null;
		}
		// SOLVE THE ROTATE2 NODE
		if (this.parentToUs == SolutionDir.ROTATE1) { // prevent infinite recursion
			//LOGGER.info("In rotate 2");
			if (ShapeTransforms.canRotate(this.ourState)) {
				//System.out.println("CAN ROT2");
				//LOGGER.info("Could rotate R2");
				GameState rotate2 = ShapeTransforms.predictRotate(new GameState (this.ourState));
				this.r2 = KnownNodes.getNode(rotate2);
				if (this.r2 == null) {
				//	LOGGER.info("Rotate2...");
					this.r2 = new SolutionNodeFullBackTrack(rotate2 ,this,SolutionDir.ROTATE2);
					KnownNodes.checkIn(this.r2);
					this.r2.Solve();
					this.distR2 = this.r2.getDistance();
					if (this.r2.hasFoundSolution() && this.r2.getSolutionVal() < this.ourSolution) {
						this.ourSolution = this.r2.getSolutionVal();
						this.next = this.r2;
					}
				} else {
					//if (!this.r2.hasFoundSolution()) this.r2 = null;
				}
			} else {
				this.r2 = null;
			}
		} else {
			this.r2 = null;
		}
		// SOLVE THE ROTATE3 NODE
		if (this.parentToUs == SolutionDir.ROTATE2) { // prevent infinite recursion
			if (ShapeTransforms.canRotate(this.ourState)) {
				//System.out.println("CAN ROT3");
				//LOGGER.info("Could rotate R3");
				GameState rotate3 = ShapeTransforms.predictRotate(new GameState (this.ourState));
				this.r3 = KnownNodes.getNode(rotate3);
				if (this.r3 == null) {
				//	LOGGER.info("Rotate3...");
					this.r3 = new SolutionNodeFullBackTrack(rotate3,this,SolutionDir.ROTATE3);
					KnownNodes.checkIn(this.r3);
					this.r3.Solve();
					this.distR3 = this.r3.getDistance();
					if (this.r3.hasFoundSolution() && this.r3.getSolutionVal() < this.ourSolution) {
						this.ourSolution = this.r3.getSolutionVal();
						this.next = this.r3;
					}
				} else {
				//	if (!this.r3.hasFoundSolution()) this.r3 = null;
				}
			} else {
				this.r3 = null;
			}
		} else {
			this.r3 = null;
		}
		
		// SOLVE THE DROP NODE --- note should be last
		if (ShapeTransforms.canDrop(this.ourState)) {
			//System.out.println("CAN DRP");
			GameState drop = ShapeTransforms.predictDropOnce(new GameState (this.ourState));
			this.d = KnownNodes.getNode(drop);
			if (this.d == null) {
			//	LOGGER.info("Drop...");
				this.d = new SolutionNodeFullBackTrack(drop,this,SolutionDir.DOWN);
				KnownNodes.checkIn(this.d);
				this.d.Solve();
				this.distD = this.d.getDistance() + 1;
				if (this.d.hasFoundSolution() && this.d.getSolutionVal() < this.ourSolution) {
					this.ourSolution = this.d.getSolutionVal();
					this.next = this.d;
				}
			} else {
				//if (!this.d.hasFoundSolution()) this.d = null;
			}
		} else {
			this.d = null;
			GameState dropState = ShapeTransforms.predictCompleteDrop(new GameState (this.ourState));
			int numCleared = BoardTransforms.clearFullRows(dropState); 
			int retVal = 0;
			if (numCleared > 0) retVal = -numCleared;
			else if (numCleared < 0) {
				System.err.println("ERROR: SOLUTIONNODEBACKTRACK: Negative line cleared!");			
			} else if (this.recursionLevel < 2){
				try {
					SolutionNodeFullBackTrack nextLevel = new SolutionNodeFullBackTrack (dropState, this, SolutionDir.START);
					nextLevel.recursionLevel = this.recursionLevel + 1;
					this.ourSolution = 1 + nextLevel.Solve ();
					retVal = this.ourSolution;
				} catch (StackOverflowError e) {
					System.out.println ("Recursion Level: " + this.recursionLevel);
				}
			} else {
				retVal = POSINF;
			}
			if (retVal <= highestScore && recursionLevel == 0) {
				solutionOwner = this;
				highestScore = this.ourSolution;
			}
			return retVal;
		}
		return this.ourSolution;
	}
	public int getDistance() {
		return this.distance;
	}
	public SolutionNodeFullBackTrack getLeft() {
		return this.l;
	}
	public SolutionNodeFullBackTrack getDown() {
		return this.d;
	}
	public SolutionNodeFullBackTrack getRight() {
		return this.r;
	}
	public SolutionNodeFullBackTrack getRotate1() {
		return this.r1;
	}
	public SolutionNodeFullBackTrack getRotate2() {
		return this.r2;
	}
	public SolutionNodeFullBackTrack getRotate3() {
		return this.r3;
	}
	public String getMessage() {
		return message;
		//return "ERROR: SolutionNode: Owner not found";
	}
	public void followSolution() {	
		solutionOwner = null;
		int bestDistance = POSINF-1;
		this.nextMove = SolutionDir.PLUMMIT;
		this.next = null;
		
		int nullCheck = 0; 
		int directionCheck = 1;
		if (this.d  != null) { this.distD  = this.d.getDistance() + 1; nullCheck |= 1;}  else {this.distD  = POSINF;}
		if (this.r  != null) { this.distR  = this.r.getDistance() + 1; nullCheck |= 2;}  else {this.distR  = POSINF;}
		if (this.l  != null) { this.distL  = this.l.getDistance() + 1; nullCheck |= 4;}  else {this.distL  = POSINF;}
		if (this.r1 != null) { this.distR1 = this.r1.getDistance() + 1; nullCheck |= 8;} else {this.distR1 = POSINF;}
		if (this.r2 != null) { this.distR2 = this.r2.getDistance() + 1; nullCheck |= 16;} else {this.distR2 = POSINF;}
		if (this.r3 != null) { this.distR3 = this.r3.getDistance() + 1; nullCheck |= 32;} else {this.distR3 = POSINF;}
		// Check for solution from neighbors equal to ours, with shortest path
		if (this.solFINAL == this.ourSolution) {
			LOGGER.info("We were the highest solution: " + this.parentToUs.toString());
			this.next = null;
			this.nextMove = SolutionDir.PLUMMIT;
		} else {
			if (this.distR  < bestDistance && this.r.getSolutionVal()  == this.ourSolution) {directionCheck &= 0; bestDistance = this.distR;  this.next = this.r;this.nextMove  = SolutionDir.RIGHT;}
			if (this.distD  < bestDistance && this.d.getSolutionVal()  == this.ourSolution) {directionCheck &= 0; bestDistance = this.distD;  this.next = this.d;this.nextMove  = SolutionDir.DOWN;}
			if (this.distL  < bestDistance && this.l.getSolutionVal()  == this.ourSolution) {directionCheck &= 0; bestDistance = this.distL;  this.next = this.l;this.nextMove  = SolutionDir.LEFT;}
			if (this.distR1 < bestDistance && this.r1.getSolutionVal() == this.ourSolution) {directionCheck &= 0; bestDistance = this.distR1; this.next = this.r1;this.nextMove = SolutionDir.ROTATE1;}
			if (this.distR2 < bestDistance && this.r2.getSolutionVal() == this.ourSolution) {directionCheck &= 0; bestDistance = this.distR2; this.next = this.r2;this.nextMove = SolutionDir.ROTATE2;}
			if (this.distR3 < bestDistance && this.r3.getSolutionVal() == this.ourSolution) {directionCheck &= 0; bestDistance = this.distR3; this.next = this.r3;this.nextMove = SolutionDir.ROTATE3;}
		}
		LOGGER.info("bestDistance: " + bestDistance + " this.OurSolution: " + this.ourSolution + " , this.solFinal: " + this.solFINAL+ " ThisnextMove: " + this.nextMove.toString() );
		switch (this.nextMove) {
			//TODO replace {0,0,-1}arrays with enums and/or constants defined in Solution Master;
			case LEFT : 	{ solutionPattern[solutionPatternPointer++] = new int[] {0,-1,0}; /**LOGGER.info("LEFT")**/; break;}
			case RIGHT : 	{ solutionPattern[solutionPatternPointer++] = new int[] {0,1,0}; /**LOGGER.info("RIGHT");**/ break;}
			case DOWN : 	{ solutionPattern[solutionPatternPointer++] = new int[] {0,0,1}; /**LOGGER.info("DOWN")**/;break;}
			case ROTATE1 : 	{ solutionPattern[solutionPatternPointer++] = new int[] {1,0,0}; /**LOGGER.info("ROTATE1");**/break;}
			case ROTATE2 : 	{ solutionPattern[solutionPatternPointer++] = new int[] {1,0,0}; /**LOGGER.info("ROTATE2");**/break;}
			case ROTATE3 : 	{ solutionPattern[solutionPatternPointer++] = new int[] {1,0,0}; /**LOGGER.info("ROTATE3");**/break;}
			case PLUMMIT : 	{ solutionPattern[solutionPatternPointer++] = new int[] {0,0,-1}; /**LOGGER.info("PLUMMIT");**/LOGGER.info("Notice: adding a plummit to the pattern");if (this.next != null) LOGGER.info("was PLUMMIT, but have next move?");break;}
			default : 		{ solutionPattern[solutionPatternPointer++] = new int[] {0,0,0}; LOGGER.info("Notice: using default for some reason");break;}
		}
		if (solutionPatternPointer == solutionPattern.length) {
			int newLength = bestDistance >= 5 ? bestDistance : 5;
			int [][] temp = Arrays.copyOf(solutionPattern, solutionPattern.length + newLength);
			solutionPattern = temp;
		}
		if (this.next != null) {
			if (this.next.equals(this)) {
				System.out.println("The next node is ourself, prolly going to stack overflow...");
			}
			this.next.followSolution();
		} else { // THE END OF THE RECURSION, ALSO THE LAST MOVE FOR THE CURRENT SHAPE
			solutionOwner = this;
			for (int i=0;i<solutionPattern.length;i++) {
				message += "{"+solutionPattern[i][0]+","+solutionPattern[i][1]+"," + solutionPattern[i][2]+"}\n";
			}
			System.out.println("Final Solution Had the following Values: \n"+this.message);		
			
			LOGGER.warning ("PARENT TO SOLUTION OWNER: " + this.parentToUs);
			if (nullCheck == 0) {
				LOGGER.severe("ERROR: SolutionNode: <all options are null>");
				this.nextMove = null;
			}
			if (directionCheck != 0) {
				LOGGER.severe("ERROR: SolutionNode: We were supposed to chose a direction; could not decide.");
			}
			return;
		}		
	}
	public boolean hasFoundSolution() {
		return this.ourSolution > NOSOL;
	}
	public int getSolutionVal() {
		return this.ourSolution;
	}
	public static int[][] getSolutionCoords () {
		if (solutionOwner == null) {
			return new int[1][3];
		} else {
			return solutionOwner.ourState.getShape().getCoords();
		}
	}
	public static int[][] getSolution () {
		int end = solutionPatternPointer;
		int [][] output = new int[end+1][3];
		for (int i=0;i<output.length;i++) {
			output[i][0] = solutionPattern[i][0];
			output[i][1] = solutionPattern[i][1];
			output[i][2] = solutionPattern[i][2];
		}
		LOGGER.info("Solution as it left solution node...");
		String message = "";
		for (int[] step : output) {
			message += "  {"+step[0]+","+step[1]+","+step[2]+"},\n";
		}
		LOGGER.info(message);
		LOGGER.info("last element of output || SP: {" + output[output.length-1][0] + "," + output[output.length-1][1] + "," + output[output.length-1][2] + "} || {"+ solutionPattern[output.length-1][0] + "," + solutionPattern[output.length-1][1] + "," + solutionPattern[output.length-1][2] + "}");
		LOGGER.info("DONE");
		return output;
	}
	public static String getLastKnownMessage () {
		return solutionOwner.message;
	}
	private static class KnownNodes {
		private static ArrayList<SolutionNodeFullBackTrack> knownSolutionNodes = new ArrayList<SolutionNodeFullBackTrack> ();
		private static void checkIn(SolutionNodeFullBackTrack node) {
			int sizebef = knownSolutionNodes.size();
			if (!knownSolutionNodes.contains(node))knownSolutionNodes.add(node);
			int sizeaft = knownSolutionNodes.size();
			if (sizeaft != sizebef && sizeaft%100 == 0) System.out.println("Number Of Known Solution Nodes: " + sizeaft);
		}
		protected static SolutionNodeFullBackTrack getNode(GameState inState) {
			int [][] coords = inState.getShape().getCoords();
			for (SolutionNodeFullBackTrack node : knownSolutionNodes) {
			//	if(Arrays.deepEquals(node.getCoords(),coords)) {
				//	return node;
				//}
			}
			return null; // does not exist yet
		}
		protected static void clear () {
			System.out.println("SIZE AT CLEAR" +knownSolutionNodes.size());
			knownSolutionNodes = new ArrayList<SolutionNodeFullBackTrack> (); // this is very important for correct operation without re-instantiation each time
		}
	}
}
