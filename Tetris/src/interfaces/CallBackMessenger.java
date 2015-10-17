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
private ArrayList<Watcher> watchers;
private EngineInterface engine;
public CallBackMessenger(ArrayList<Watcher> watchers) {
	this.watchers = new ArrayList<Watcher>(watchers);
	runAI = false;
}
public void linkEngine (EngineInterface engineInterface) {
	this.engine = engineInterface;
}
public void addWatcher (Watcher watcher) {
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
	/* After notifying all watchers, get decision from AI */
	if (engine != null && ai!=null && runAI) {
		GameState state = new GameState(engine);
		ShapeState rotationLearnedShape =  new ShapeState (RotationManager.learnShape(state, engine, false),state.getShape().getType());
		state = new GameState (state.getBoardWithoutCurrentShape(), rotationLearnedShape);
		
		ControlMovements ctrSeq = ai.decideCM(state);
		engine.executeSequence(ctrSeq);
	}
}
public void ringBell() {
	ListIterator<Watcher> li = watchers.listIterator();
	while (li.hasNext()) {
		li.next().notifyWatcher();
	}

}
}
