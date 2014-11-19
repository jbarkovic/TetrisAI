package source.tetris.engine.shapes;

public enum SHAPETYPE {
T,O,L,I,S,Z,J,NONE;
	public int toInt () {
		switch (this) {
		case T : return 1;
		case O : return 2;
		case L : return 3;
		case I : return 4;
		case S : return 5;
		case Z : return 6;
		case J : return 7;
		case NONE: return 0;
		}
		return -1;
	}
	public static SHAPETYPE intToShapeType (int in) {
		switch (in) {
		case 1 : return T;
		case 2 : return O;
		case 3 : return L;
		case 4 : return I;
		case 5 : return S;
		case 6 : return Z;
		case 7 : return J;
		case 0: return NONE;
		}
		return NONE;
	}
}
