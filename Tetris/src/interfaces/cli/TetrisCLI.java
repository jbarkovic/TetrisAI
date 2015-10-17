package interfaces.cli;

import ai.state.GameState;
import interfaces.UI;

public class TetrisCLI extends UI {
	private final int printInterval = 200;
	private int printCount = 0;
	private TetrisCurses progressWriter = new TetrisCurses();
	public static void main (String [] args) {
		final UI ui = UI.produceUI(args);
		if (ui != null) {
			TetrisCLI tcli = new TetrisCLI (ui);
			tcli.start();
			tcli.cback.ringBell();
		}
	}
	private void start () {
		if (ai != null) {
			cback.startAI();
			engine.swapShapes();
		} else {
			System.err.println ("NULL AI");
		}
	}
	@Override
	public void update () {
		if (engine == null) {
			System.err.println ("NULL ENGINE in CLI update()");
		} else if (printCount == printInterval) {
			progressWriter.printMessage("Lines Cleared: " + engine.getNumberOfRowsCleared());
			printCount = 0;
		} else if (engine.isGameLost()) {
			GameState finalState = new GameState (engine);
			System.err.println ("Game Over: " + engine.getNumberOfRowsCleared() + " lines");
			GameState.dumpState(finalState, true);
		}
		printCount++;
	}
	public TetrisCLI (UI ui) {
		super (ui.rows,ui.cols,ui.historyFile,ui.AISpeed,ui.usePlummit);
		engine.pause();
		System.out.println("Starting CLI.. ");	
	}
}
