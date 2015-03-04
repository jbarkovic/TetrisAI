package interfaces.gui;

import java.awt.EventQueue;

import interfaces.UI;

public class GUI extends UI{
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
		gw.updateScreen();
	}
	public GUI (UI ui) {
		super (ui.rows,ui.cols,ui.historyFile,ui.AISpeed,ui.usePlummit);
		if (!ui.eng.isPaused()) ui.eng.pause();
		System.out.println ("GUI Constructor");
		gw = new GameWindow (this);
		gw.setVisible(true);
	}
}

