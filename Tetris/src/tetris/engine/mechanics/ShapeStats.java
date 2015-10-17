package tetris.engine.mechanics;

import java.util.HashMap;
import java.util.Map;

public class ShapeStats {
	private Engine.ShapeType [] shapeHistory;
	
	public ShapeStats () {
		shapeHistory = new Engine.ShapeType [100];
		clearHistory();
	}
	private void clearHistory () { 
		for (int i=0;i<shapeHistory.length;i++) {
			shapeHistory[i] = null;
		}
	}
	public void addShape (Engine.ShapeType shape) {
		for (int i=shapeHistory.length-1;i>0;i--) {
			shapeHistory[i] = shapeHistory[i-1];
		}
		shapeHistory[0] = shape;
	}
	public int getHistoryLength () {
		int count = 0;
		if (shapeHistory==null) return 0;
		for (Engine.ShapeType sType : shapeHistory) {
			if (sType != null) count ++; 
		}
		return count;
	}
	public HashMap<Engine.ShapeType, Double> getStats () {
		HashMap<Engine.ShapeType,MutableDouble> stats = new HashMap<Engine.ShapeType,MutableDouble>();
		for (int i=0;i<shapeHistory.length;i++) {
			Engine.ShapeType curShape = shapeHistory[i];
			if (curShape != null) {
				MutableDouble count = stats.get(curShape);
			
				if (count==null) { // Doesn't exist yet
					count = new MutableDouble(1d);
					stats.put(curShape, count);
				} else { // Count exists already, add to it
					count.value += 1d;
				}
			}
		}
		HashMap<Engine.ShapeType, Double> doubleStats = new HashMap<Engine.ShapeType, Double>();
		for (Map.Entry<Engine.ShapeType, MutableDouble> entry : stats.entrySet()) {
			entry.getValue().value /= shapeHistory.length;
			doubleStats.put(entry.getKey(), entry.getValue().toDouble());
		}
		return doubleStats;
	}
	private class MutableDouble {
		public double value;
		public MutableDouble (double value) {
			this.value = value;
		}
		public Double toDouble () {
			return new Double (this.value);
		}
	}
}
