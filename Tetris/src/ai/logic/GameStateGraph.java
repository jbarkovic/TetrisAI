package ai.logic;

import java.util.ArrayList;

import ai.state.BoardState;
import ai.state.GameState;
import ai.state.ShapeState;
import interfaces.EngineInterface;

public class GameStateGraph extends GameState {

	private GameStateGraph parent;
	private ArrayList<GameStateGraph> children;
	
	{
		parent = null;
		children = new ArrayList<GameStateGraph> (2);
	}
	private static final long serialVersionUID = 6367842790549316094L;
	public GameStateGraph(BoardState bState, ShapeState sState) {
		super(bState, sState);
	}
	public GameStateGraph (EngineInterface engine) {
		super(engine);
	}
	public GameStateGraph (GameState state) {
		super (state);
	}
	public void addChild (GameStateGraph child) {
		if (child != null) children.add(child);
	}
	public boolean hasParent() {
		return this.parent != null;
	}
	public void setParent(GameStateGraph parent) {
		this.parent = parent;
	}
	public ArrayList<GameStateGraph> getChildren () {
		return children;
	}
	public GameStateGraph getParent () {
		return parent;
	}
	public int numberOfChildren() {
		return children.size();
	}

}
