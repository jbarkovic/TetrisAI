package tetris.engine.shapes2;

import java.util.Arrays;

abstract class Shape {
	private int [] initialCoords;
	private int [] currentCoords;
	private int [] outputCoords;
	private int xDelta, yDelta;
	private Object horzShiftLock, vertShiftLock, rotateLock;
	private int shapeSize;
	
	protected Shape(int x0, int y0, int x1, int y1, int x2, int y2, int x3, int y3) {
		initialCoords = new int [8];
		initialCoords[0] = x0;
		initialCoords[1] = y0;
		initialCoords[2] = x1;
		initialCoords[3] = y1;
		initialCoords[4] = x2;
		initialCoords[5] = y2;
		initialCoords[6] = x3;
		initialCoords[7] = y3;
		shapeSize = getShapeSize();
		
		currentCoords = Arrays.copyOf(initialCoords,  initialCoords.length);
		outputCoords = Arrays.copyOf(initialCoords,  initialCoords.length);
		xDelta = 0; yDelta = 0;
		
		horzShiftLock = new Object();
		vertShiftLock = new Object();
		rotateLock    = new Object();
	}
	
	public void rotateRight () {
		synchronized (rotateLock) {
			verticalFlip();
			transpose();
		}
	}
	
	public void rotateLeft () {
		synchronized (rotateLock) {
			transpose();
			verticalFlip();
		}
	}
	
	public void shiftDown() {
		synchronized (vertShiftLock) {
			yDelta++;
		}
	}
	
	public void shiftUp() {
		synchronized (vertShiftLock) {
			yDelta--;
		}
	}
	
	public void shiftRight () {
		synchronized (horzShiftLock) {
			xDelta++;
		}
	}
	
	public void shiftLeft() {
		synchronized (horzShiftLock) {
			xDelta--;
		}
	}
	
	public void reset () {
		currentCoords = Arrays.copyOf(initialCoords,  initialCoords.length);
		xDelta = 0;
		yDelta = 0;
	}
	
	public Coords getShapeProjection (int spawnX, int spawnY) {
		synchronized (vertShiftLock) {
			synchronized (horzShiftLock) {
				synchronized (rotateLock) {
					for (int i=0;i<outputCoords.length;i+=2) {
						outputCoords[i]   = currentCoords[i] + xDelta + spawnX;
						outputCoords[i+1] = currentCoords[i+1] + yDelta + spawnY;
					}
					return new Coords(outputCoords);
				}
			}
		}
	}
	
	private int getShapeSize () {
		int max = initialCoords[0];
		for (int i=1;i<initialCoords.length;i++) {
			max = Math.max(initialCoords[i], max);
		}
		return max;
	}
	
	private void verticalFlip () {
		int mid = shapeSize; // ShapeSize*2/2
		int [] A = currentCoords;
		for (int i=1;i<A.length;i+=2) {
			int delta = mid - A[i] * 2;
			A[i] += delta;
		}
	}
	
	private void transpose () {
		int [] A = currentCoords;
		int temp0, temp1;
		
		temp0 = A[0];
		A[0] = A[1];
		A[1] = temp0;
		
		temp1 = A[2];
		A[2] = A[3];
		A[3] = temp1;
		
		temp0 = A[4];
		A[4] = A[5];
		A[5] = temp0;
		
		temp1 = A[6];
		A[6] = A[7];
		A[7] = temp1;
	}
	
	public class Coords {
		private int [] A;
		
		private Coords (int [] coords) {
			A = coords;
		}
		
		public int [] getCoordsArray () {
			return Arrays.copyOf(A,  A.length);
		}
		
		public int [][] getCoordsMatrix () {
			int [][] coords = new int [4][2];
			coords[0][0] = A[0];
			coords[0][1] = A[1];
			coords[1][0] = A[2];
			coords[1][1] = A[3];
			coords[2][0] = A[4];
			coords[2][1] = A[5];
			coords[3][0] = A[6];
			coords[3][1] = A[7];
			
			return coords;
		}
		
		public int get (int x, int y) {
			int ptr = 2*x+y;
			if (ptr < 0 || ptr >= A.length) {
				return -2;
			} else {
				return A[ptr];
			}
		}
	}
}
