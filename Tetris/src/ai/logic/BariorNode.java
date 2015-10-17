package ai.logic;

public class BariorNode extends PathNode {

	public BariorNode(PathNode parent) {
		super(parent.getLocation());
	}
	@Override
	public boolean complete() {
		return true;
	}
}
