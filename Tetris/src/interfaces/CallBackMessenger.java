package interfaces;

import java.util.ArrayList;
import java.util.ListIterator;

import ai.logic.AIBacktrack;
import ai.state.GameState;
import ai.state.RotationManager;
import ai.state.ShapeState;
import tetris.engine.mechanics.*;

public class CallBackMessenger extends CallBack {
private AIBacktrack ai;
private boolean runAI;
long [] movementTimes = new long [101];
long lastBellRungTime = System.nanoTime();
long shapeCount = 0;
private int errCount = 0;
private ArrayList<Watcher> watchers;
private EngineInterface engine;
private RotationManager rotationManager;

public CallBackMessenger(ArrayList<Watcher> watchers) {
	rotationManager = new RotationManager();
	this.watchers = new ArrayList<Watcher>(watchers);
	runAI = false;
}
public void linkEngine (EngineInterface engineInterface) {
	this.engine = engineInterface;
}
public void addWatcher (Watcher watcher) {
	System.out.println("Adding watcher");
	this.watchers.add(watcher);
}
public void addAI (AIBacktrack ai) {	
	this.ai = ai;
}
public AIBacktrack getAI () {
	return ai;
}
public void startAI () {
	runAI = true;
}
public void stopAI () {
	runAI = false;
}
public boolean isAIRunning () {
	return runAI;
}
public void newShape () {
	ringBell ();
	//System.out.println("New Shape");
	/* After notifying all watchers, get decision from AI */
	if (engine != null && ai!=null && runAI) {
		long timeStart = System.nanoTime();
		
		shapeCount++;
		
		movementTimes[movementTimes.length-1] = System.nanoTime() - movementTimes[movementTimes.length-1];
		for (int i=0;i<movementTimes.length-1;i++) {
			movementTimes[i] = movementTimes[i+1];
		}
		


		GameState state = new GameState(engine);
		
		if (!ai.isFinalStateCorrrect(state)) {
			System.err.println("AI did not correctly place last shape");
			errCount ++;
		}
		
		ShapeState rotationLearnedShape =  new ShapeState (rotationManager.learnShape(state, engine, false),state.getShape().getType());
		state = new GameState (state.getBoardWithoutCurrentShape(), rotationLearnedShape);
		
		
		
		ControlMovements ctrSeq = ai.decideCM(state, rotationManager);
		//System.out.println("AI Done deciding");
		movementTimes[movementTimes.length-1] = System.nanoTime();
		if (errCount < 5) engine.executeSequence(ctrSeq);
		
		if (shapeCount % 31==0) {
			long timelapsed = System.nanoTime() - timeStart; 
			//System.out.println(String.format("Avg Time to move shape: %4.3f, time elapsed this round of calc %4.3f", calcAvgMovmentTime ()/ 1000000d, timelapsed/1000000d));
		}
		
	} else if (ai == null && runAI){
		System.out.println("ERROR: Ai is supposed to be running, but is null");
	}
}

public long calcAvgMovmentTime () {
	Double avg = 0d;
	for (int i=0;i<movementTimes.length-1;i++) {
		avg += movementTimes[i];
	}
	avg /= movementTimes.length-1;
	
	return avg.longValue();
}

public void ringBell() {
	//System.out.println("\nBell rung");
	//long diff =  System.nanoTime() - lastBellRungTime;
	
	//if (diff >= 0 && diff < 33000000l) return; // essure no more than 30 fps 
	//else lastBellRungTime = System.nanoTime();
			
	ListIterator<Watcher> li = watchers.listIterator();
	//System.out.println("There are " + watchers.size() + " watchers");
	while (li.hasNext()) {
		//System.out.println("Notified watcher: ");
		li.next().notifyWatcher();
	}
	//watchers.notifyAll();

}
}
