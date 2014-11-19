package interfaces;
import megatetris.interfaces.gui.*;
import tetris.engine.mechanics.*;

public class CallBackMessenger extends CallBack {
private GameWindow gw;
public CallBackMessenger(GameWindow gw) {
	this.gw = gw;
}
public void ringBell() {
	this.gw.updateScreen();
}
}
