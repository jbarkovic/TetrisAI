package ai.state;

import java.util.ArrayList;

public class SortedHashMap<K,V> {
	private ArrayList<Integer> keys;
	private ArrayList<V> values;
	int maxSize;
	{
		keys = new ArrayList<Integer> ();
		values = new ArrayList<V> ();
	}
	public SortedHashMap () {		
	}
	public SortedHashMap (int makeAvailable) {
		this.makeAvailable(makeAvailable);
	}
	private void makeAvailable (int size) {
		keys.ensureCapacity(size);
		values.ensureCapacity(size);
		maxSize = size;
	}
	public V put (K key, V value) {
		V returnVal = null;
		if (keys.size() + 2 > maxSize || keys.size() != values.size()) {
			int maxSizeBef = Math.max(keys.size(), values.size());
			makeAvailable(2 + maxSizeBef*2);
		} else if (this.size() == 0) {
			this.keys.add(key.hashCode());
			this.values.add(value);
		} else {
			BoxedResult result = tryGet (key.hashCode(),0,keys.size());
			if (result.valid) {
				returnVal = result.value;
				values.set(key.hashCode(), value);
			} else {
				values.add(result.freeHash, value);
				keys.add(result.freeHash, key.hashCode());
			}
		}
		return returnVal;
	}
	public int size () {
		return Math.min(keys.size(), values.size());
	}
	public void clear () {
		this.keys.clear();
		this.values.clear();
	}
	private BoxedResult tryGet (int hashKey, int start, int end) { // Not including end
		if (this.size() == 0) {
			return new BoxedResult (start);
		} else if (end-start == 1) {
			if (keys.get(start) == hashKey) return new BoxedResult (true, values.get(start),null);
			else return new BoxedResult (start);
		} else {
			int middle = (end-start)/2;
			int hashAtMiddle = keys.get(middle);
			if (hashKey >= hashAtMiddle) return tryGet (hashKey,middle,end);
			else return tryGet (hashKey,start,middle);
		}
	}
	public V get (K key) {
		if (this.size() == 0) {
			return null;
		}
		System.out.println ("Size = " + this.size());
		return tryGet(key.hashCode(),0,keys.size()).value;
	}
	public boolean hasKey (K key) {
		return tryGet(key.hashCode(),0,keys.size()).valid;
	}
	public boolean hasValue (V value) {
		return values.contains(value);
	}
	private class BoxedResult {		
		boolean valid;
		int freeHash;
		V value;
		K key;
		public BoxedResult (int freeHash) {
			this();
			this.freeHash = freeHash;
		}
		public BoxedResult () {
			valid = false;
			value = null;
			key = null;
		}
		public BoxedResult (boolean valid, V value, K key) {
			this.valid = valid;
			this.key = key;
			this.value = value;
		}		
	}
}
