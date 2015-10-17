package interfaces;

import java.awt.EventQueue;
import java.io.IOException;
import java.util.logging.Level;

import tetris.logging.TetrisLogger;

public class UIFactory {
	public static void produceUI (String [] args, UI ui) {
		System.out.println("Loading...");
		final String[] dim = args;
		final int DEFAULTROWS = 16;
		final int DEFAULTCOLUMNS = 10;
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				int AISpeed = 50;
				String historyFile = null; 
				try {
					int rows = DEFAULTROWS;
					int columns = DEFAULTCOLUMNS;
					int logLevel = 0;
					boolean plummit = false;
					for (int i=0;i<dim.length;i++) {
						if (dim [i].equals("-plummit")) {
							plummit = true;
						} else if (dim [i].equals("-l")) {
							try {
								i++;
								logLevel = Integer.parseInt(dim[2]);									
							} catch (ArrayIndexOutOfBoundsException e0) {						
							} catch (NullPointerException e1) {						
							} catch (NumberFormatException e2) {
								try {
								} catch (ArrayIndexOutOfBoundsException e3) {}
									
							}
							Level level = Level.OFF;
							switch (logLevel) {
							case 0 : {level = Level.OFF; break;}
							case 1 : {level = Level.SEVERE; break;}
							case 2 : {level = Level.WARNING; break;}
							case 3 : {level = Level.INFO; break;}
							case 4 : {level = Level.FINE; break;}
							case 5 : {level = Level.FINER; break;}
							case 6 : {level = level.FINEST; break;}
							case 7 : {level = level.CONFIG; break;}
							case 8 : {level = level.ALL; break;}
							default : {
								level = level.ALL;
								break;
								}
							}
							TetrisLogger.setup(level);
						}
						else if (dim [i].equals("-t")) {
							try {
								int speed = Integer.parseInt(dim [i+1]);
								System.out.println("AI Speed: " + speed);
								AISpeed = speed;
							} catch (NumberFormatException e2) {
								System.out.println("option -t (AI SPEED) requires an integer argument, usually 30 <= i <= 100");
							}
						}
						else if (dim [i].equals("-s")) {
							try {
								try {
									rows = Integer.parseInt(dim[i+1]);
									columns = Integer.parseInt(dim[i+2]);
									i += 2;
								} catch (ArrayIndexOutOfBoundsException e0) {
									rows = DEFAULTROWS; columns = DEFAULTCOLUMNS;
								} catch (NullPointerException e1) {
									rows = DEFAULTROWS; columns = DEFAULTCOLUMNS;
								} catch (NumberFormatException e2) {
									rows = DEFAULTROWS; columns = DEFAULTCOLUMNS;
								} finally {
									//frame.updateScreen();
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						else if (dim [i].equals("-h")) {
							System.out.println ("Looking for a history file...");
							try {
								historyFile = dim [++i];
							} catch (ArrayIndexOutOfBoundsException e) {
								System.out.println ("Could not find a history file");
								historyFile = null;
							}
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
					System.err.println("ERROR: Failed to start game.");
					throw new RuntimeException ();
				}
			}
		});
	}
}
