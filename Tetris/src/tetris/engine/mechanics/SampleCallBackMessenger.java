package tetris.engine.mechanics;


public class SampleCallBackMessenger extends CallBack{
private SampleInterface si;
public SampleCallBackMessenger(SampleInterface si) {
	this.si = si;
}
public void ringBell(){
	this.si.updateScreen();
}
}
