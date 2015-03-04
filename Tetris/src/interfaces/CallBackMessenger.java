package interfaces;
import java.awt.EventQueue;

import ai.logic.AIBacktrack;
import interfaces.gui.*;
import tetris.engine.mechanics.*;

public class CallBackMessenger extends CallBack {
private UI ui;
private AIBacktrack ai;
public CallBackMessenger(UI ui) {
	this.ui = ui;
}
public void addAI (AIBacktrack ai) {	
	this.ai = ai;
}
public void ringBell() {
	//new Thread (new Runnable() {
		//public void run() {
	ui.update();
	EventQueue.invokeLater(new Runnable () {
		public void run () {
			ai.step();
		}
	});
//	synchronized (ui) {
	//}

	//	}
	//}).start();


}
}
