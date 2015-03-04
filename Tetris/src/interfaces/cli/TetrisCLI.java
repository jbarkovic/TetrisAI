package interfaces.cli;

import java.awt.EventQueue;

import ai.state.GameState;
import interfaces.UI;

public class TetrisCLI extends UI {
	private final int printInterval = 200;
	private int printCount = 0;
	public static void main (String [] args) {
		final UI ui = UI.produceUI(args);
		if (ui != null) {
			//EventQueue.invokeLater(new Runnable () {
				//public void run () {
					TetrisCLI tcli = new TetrisCLI (ui);
					tcli.start();
					tcli.cback.ringBell();
				//}				
			//});
		}
	}
	private void start () {
		if (eng != null && !eng.isPaused()) eng.pause();
		if (ai != null) {
			ai.start();
			eng.plummit();
			//ai.step();
		} else {
			System.err.println ("NULL AI");
		}
	}
	@Override
	public void update () {
		if (eng == null) {
			System.err.println ("NULL ENGINE in CLI update()");
		} else if (printCount == printInterval) {
			System.out.println ("Lines Cleared: " + eng.getLinesCleared());
			printCount = 0;
		} else if (eng.isGameLost()) {
			if (!eng.isPaused()) eng.pause();
			GameState finalState = new GameState (engine);
			System.err.println ("Game Over: " + eng.getLinesCleared() + " lines");
			GameState.dumpState(finalState, true);
		}
		printCount++;
	}
	public TetrisCLI (UI ui) {
		super (ui.rows,ui.cols,ui.historyFile,ui.AISpeed,ui.usePlummit);
		eng.pause();
		System.out.println("Starting CLI.. ");	
	}
}
