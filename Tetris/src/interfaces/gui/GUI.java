package interfaces.gui;

import java.awt.EventQueue;

import interfaces.UI;

public class GUI extends UI {
	private GameWindow gw;
	public static void main (String [] args) {
		final UI ui = UI.produceUI(args);
		if (ui != null) {
			EventQueue.invokeLater(new Runnable () {
				public void run () {
					new GUI (ui);
				}				
			});
		}
	}
	@Override
	public void update () {
		//System.out.println("GUI - > update");
		if (gw != null) gw.updateScreen();
		else System.err.println("GameWindow: null");
	}
	public GUI (UI ui) {
		super (ui.rows,ui.cols,ui.historyFile,ui.AISpeed,ui.usePlummit);
		cback.addWatcher(this);
		System.out.println("Added watcher");
		gw = new GameWindow (this);
		gw.setVisible(true);
	}

}

