package ai.logic;

import interfaces.EngineInterface;

public abstract class AI {
	public abstract void run(EngineInterface inInterface);
	public abstract void run(EngineInterface inInterface, String configFile);
	public abstract void stop();
	public abstract boolean isRunning();
	public abstract void step();	
	public abstract void setSwap(boolean val);
	public abstract int testSolution ();
	public abstract void dumpSolution(String[] messages);
	public abstract void usePlummit(boolean val);
	public abstract boolean getUsePlumit();
}
