package tetris.engine.mechanics;
import java.awt.EventQueue;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JButton;

import java.awt.event.KeyAdapter;

import org.eclipse.wb.swing.FocusTraversalOnArray;

import tetris.logging.TetrisLogger;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JLabel;


public class SampleInterface extends JFrame {
	private JButton rButton;
	private JPanel conPane;
	public static final Color EMPTYCOLOR = Color.white;
	private JButton[][] gameSpaceArray = new JButton[16][10];

	private JLabel scoreLabel;
	private Engine engine;
	private JButton[][] nextShapeGrid;
	private int oldLinesCleared = 0;
	
	private final static Logger LOGGER = Logger.getLogger(SampleInterface.class.getName());
	
	public void updateScreen() {
		LOGGER.finest("UPDATING SCREEN");
		if (this.engine == null) {
			LOGGER.severe("null engine in GUI");
		}
		if (this.engine.isGameLost()) {
			for (JButton[] row : this.gameSpaceArray) {
				for (JButton button : row) {
					
					
				this.setSpaceVisible(button,-1);
				}
			}
			return;
		}
		int[][] gameState = this.engine.getGameBoard();
		for (int row=0;row<gameState.length;row++) {
			for (int column=0;column<gameState[row].length;column++) {
				this.setSpaceVisible(this.gameSpaceArray[row][column],gameState[row][column]);
			}
		}
		int[][] swapspace = this.engine.getSwapBoard();
		for (int row=0;row<swapspace.length;row++) {
			for (int column=0;column<swapspace[0].length;column++) {
				this.setSpaceVisible(this.nextShapeGrid[row][column],swapspace[row][column]);
			}
		}
		int newLinesCleared = this.engine.getLinesCleared();
		if (newLinesCleared > oldLinesCleared) {
			if (newLinesCleared > 20) {
				this.engine.setGravity(820);
			} else if (newLinesCleared > 50) {
				this.engine.setGravity(750);
			} else if (newLinesCleared > 110) {
				this.engine.setGravity(600);
			} else if (newLinesCleared > 250) {
				this.engine.setGravity(500);
			} else if (newLinesCleared > 400) {
				this.engine.setGravity(300);
			} else if (newLinesCleared > 700) {
				this.engine.setGravity(100);
			}
			this.oldLinesCleared = newLinesCleared;
		}
		this.conPane.repaint();
		this.scoreLabel.setText("Lines Cleared: " + Integer.toString(newLinesCleared)); 
	}
	private void setSpaceVisible(JButton button, int color) {
		switch (color) {
		case 0 : {
			button.setBackground(EMPTYCOLOR);
			button.setBorderPainted(false);
			break;
		}
		case 1 : {
			button.setBackground(Color.red);
			button.setBorderPainted(true);
			break;
		}
		case 2 : {
			button.setBackground(Color.blue);
			button.setBorderPainted(true);
			break;
		}
		case 3 : {
			button.setBackground(Color.orange);
			button.setBorderPainted(true);
			break;
		}
		case 4 : {
			button.setBackground(Color.green);
			button.setBorderPainted(true);
			break;
		}
		case 5 : {
			button.setBackground(Color.yellow);
			button.setBorderPainted(true);
			break;
		}
		case 6 : {
			button.setBackground(Color.cyan);
			button.setBorderPainted(true);
			break;
		}
		case 7 : {
			button.setBackground(Color.magenta);
			button.setBorderPainted(true);
			break;
		}
		default : { // error color
			button.setBackground(Color.red);
			button.setBorderPainted(true);
			break;
		}
		}
	}
	public void rotate() {
		this.engine.rotateClockwise();
	}
	private void shiftLeft() {
		this.engine.shiftLeft();
	}
	private void shiftRight() {
		this.engine.shiftRight();
	}
	public boolean drop() {
		return this.engine.dropShape();
	}
	public void newGame() {
		this.main(new String[0]);		
		this.dispose();
	}
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				SampleInterface frame = new SampleInterface();
				try {
					TetrisLogger.setup(Level.ALL);
					LOGGER.setLevel(Logger.getGlobal().getLevel());
				} catch (IOException e) {
					e.printStackTrace();
					System.err.println("ERROR: Failed to initialize logging system. Will exit.");
					throw new RuntimeException ();
				}
				try {
					frame.setVisible(true);
				
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	public void plummit() {
		this.engine.plummit();
	}
	public void done() {
		this.conPane.requestFocus();
	}
	private void impossible() {
		this.engine.impossible();
	}
	private void swapShapes() {
		this.engine.swapShapes();
	}
	private void pause() {
		this.engine.pause();
	}
	public SampleInterface() {	
		setTitle("Not Not Tetris");
		SampleCallBackMessenger scbm = new SampleCallBackMessenger(this);
		this.engine = new Engine(16,10,900, scbm);
		this.nextShapeGrid = new JButton[4][4];
		setResizable(false);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.gameSpaceArray = new JButton[16][10];
		setBounds(100, 100, 759, 695);
		JPanel contentPane = new JPanel();

		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JPanel gameArea = new JPanel();
		gameArea.setBounds(new Rectangle(0, 0, 400, 620));
		gameArea.setMinimumSize(new Dimension(400, 620));
		gameArea.setMaximumSize(new Dimension(400, 620));
		gameArea.setBounds(25, 12, 400, 640);
		contentPane.add(gameArea);
		gameArea.setLayout(new GridLayout(0, 10, 0, 0));
		this.conPane = contentPane;
		
		JLabel lblNewLabel = new JLabel("0" + " Lines Cleared");
		this.scoreLabel = lblNewLabel;
		lblNewLabel.setBounds(512, 252, 200, 15);
		contentPane.add(lblNewLabel);
		
		JPanel nextShapePanel = new JPanel();
		nextShapePanel.setBounds(new Rectangle(0, 0, 160, 160));
		nextShapePanel.setBorder(BorderFactory.createLineBorder(Color.black));
		nextShapePanel.setBounds(529, 39, 170, 156);
		contentPane.add(nextShapePanel);
		nextShapePanel.setLayout(new GridLayout(0, 4, 0, 0));
		
		JButton btnPause = new JButton("Pause");
		btnPause.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent arg0) {
				pause();
			}
		});
		JButton btnNewGame = new JButton("New Game");
		btnNewGame.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent arg0) {
				newGame();
			}
		});
		btnPause.setBounds(440, 601, 303, 50);
		btnNewGame.setBounds(440, 477, 303, 50);
		contentPane.add(btnPause);
		contentPane.add(btnNewGame);
		
		JButton btnImpossible = new JButton("Impossible");
		btnImpossible.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent arg0) {
				impossible();
			}
		});
		btnImpossible.setBounds(440, 539, 303, 50);
		contentPane.add(btnImpossible);
		setFocusTraversalPolicy(new FocusTraversalOnArray(new Component[]{contentPane}));
		for (int row=0;row<16;row++){
			for (int column=0;column<10;column++) {
				JButton tempButton = new JButton();
				tempButton.setBounds(new Rectangle(0,0,40,40));
				tempButton.setBackground(EMPTYCOLOR);
				tempButton.setBorderPainted(false);
				tempButton.setName(row+""+column);
				tempButton.setBorder(BorderFactory.createLineBorder(Color.black));
				this.gameSpaceArray[row][column] = tempButton;
				gameArea.add(tempButton);
			}
		}
		for (int row=0;row<4;row++) {
			for (int column=0;column<4;column++) {
				JButton tempButton = new JButton();
				tempButton.setBounds(new Rectangle(0,0,40,40));
				tempButton.setBackground(EMPTYCOLOR);
				tempButton.setBorderPainted(false);
				tempButton.setName(row+""+column);
				tempButton.setBorder(BorderFactory.createLineBorder(Color.black));
				this.nextShapeGrid[row][column] = tempButton;
				nextShapePanel.add(tempButton);
			}
		}
		contentPane.addKeyListener(new KeyAdapter() {
			private boolean holdInput = false;
			@Override
			public void keyTyped(KeyEvent arg0) {
				if (this.holdInput) return; 
				if (arg0.getKeyChar() == 'r' || arg0.getKeyChar() == 'R') {
					rotate();
				}
				else if (arg0.getKeyChar() == 'p' || arg0.getKeyChar() == 'P') {
					if(!this.holdInput)pause();
				}
				else if (arg0.getKeyChar() == 'i' || arg0.getKeyChar() == 'I') {
					impossible();
				}
				else if (arg0.getKeyChar() == 'a' || arg0.getKeyChar() == 'A') {
					shiftLeft();
				}
				else if (arg0.getKeyChar() == 'd' || arg0.getKeyChar() == 'D') {
					shiftRight();
				}
				else if (arg0.getKeyChar() == 's' || arg0.getKeyChar() == 'S') {
					drop();
				}
				else if (arg0.getKeyChar() == 'n' || arg0.getKeyChar() == 'N') {
					newGame();
				}
			}
			@Override
			public void keyPressed(KeyEvent arg0) {
					if (this.holdInput) return;
					if (arg0.getKeyCode() == KeyEvent.VK_LEFT) {
						shiftLeft();
						return;
					}
					if (arg0.getKeyCode() == KeyEvent.VK_RIGHT) {
						shiftRight();
						return;
					}
					if (arg0.getKeyCode() == KeyEvent.VK_DOWN) {												
						if (drop()) {
						}
						return;
					}
					if (arg0.getKeyCode() == KeyEvent.VK_SHIFT) {
						rotate();
						return;
					}
					if (arg0.getKeyCode() == KeyEvent.VK_SPACE) {
						swapShapes();
						return;
					}
					if (arg0.getKeyCode() == KeyEvent.VK_CAPS_LOCK) {
						this.holdInput = true;
						plummit();
						this.holdInput = false;
						return;
					}
			}			
		});			
		
	}
}
