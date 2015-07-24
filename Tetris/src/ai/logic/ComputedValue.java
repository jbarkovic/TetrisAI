package ai.logic;

public class ComputedValue {
	public int adjacentCount;
	public int missingBelow;
	public int edgeCount;
	public int distanceFromBottom;
	public int numRowsThatWillClear;
	public int roughness;
	public int percentDown;
	public int buriedFactor;
	public int badGapBesideShape;
	public int coveredFactor;
	public int density;
	public double proximityToSpawn;
	
	public String dump () {
		String out = "\n";
		out += "===========================================\n";
		out += "------------- Solution Value --------------\n";
		out += "===========================================\n";
		out += String.format("%-30s  %d\n", "ADJACENT COUNT",adjacentCount);
		out += String.format("%-30s  %d\n", "MISSING BELOW",missingBelow);
		out += String.format("%-30s  %d\n", "EDGE COUNT",edgeCount);
		out += String.format("%-30s  %d\n", "DISTANCE FROM BOTTOM",distanceFromBottom);
		out += String.format("%-30s  %d\n", "NUM ROWS WILL CLEAR",numRowsThatWillClear);
		out += String.format("%-30s  %d\n", "ROUGHNESS AFTER",roughness);
		out += String.format("%-30s  %d\n", "PERCENT DOWN",percentDown);
		out += String.format("%-30s  %d\n", "BURIED FACTOR",buriedFactor);
		out += String.format("%-30s  %d\n", "BAD GAP BESIDE SHAPE",badGapBesideShape);
		out += String.format("%-30s  %d\n", "COVERED FACTOR",coveredFactor);
		out += String.format("%-30s  %d\n", "PROXIMITY TO SPAWN",proximityToSpawn);
		out += "===========================================\n";
		out += "===========================================\n";
		return out;
	}
}
