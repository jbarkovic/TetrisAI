package interfaces.cli;

public class TetrisCurses {
	private String lastString = "";
	public TetrisCurses () {
		
	}
	public void printMessage (String message) {
		StringBuilder erase = new StringBuilder ();
		for (int i=0;i<lastString.length();i++) {
			erase.append("\b");
		}
		System.out.print(erase);
		lastString = message;
		System.out.print(message);
	}
}
