package interfaces.gui;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JTextField;

import tetris.engine.mechanics.Engine;
import tetris.engine.shapes.SHAPETYPE;

public class AIToolsWindow extends JFrame{
		/**
		 * Launch the application.
		 */
		private Engine engine;
		private GameWindow gw;
		private JTextField filename = new JTextField(), dir = new JTextField();
		public int getWindowHeight() {
			return this.getHeight();
		}
		public int getWindowWidth() {
			return this.getWidth();
		}
		public static void start(final GameWindow gw, final Engine engine,final double squareSize,final int x,final int y) {
			EventQueue.invokeLater(new Runnable() {	
				public void run() {
					try {
						AIToolsWindow frame = new AIToolsWindow (gw, engine,squareSize,x,y);
						frame.setVisible(true);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		}
		/**
		 * Create the frame.
		 */
		private ArrayList<String> readSaveFile () {
	        ArrayList<String> data = new ArrayList<String> ();
		    BufferedReader br = null;		
		    try {
				br = new BufferedReader(new FileReader(new String (dir.getText() + filename.getText())));		   
		        String line = br.readLine();

		        while (line != null) {
		            data.add(line);
		            line = br.readLine();
		        }
		    } catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
		        try {
					if (br != null) br.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    }
		    return data;
		}
		private boolean loadState (ArrayList<String> data) {
			if (data.size() < 3) return false;
			try {
				int ptr = 0;
				int [] size = new int [] {Integer.parseInt(data.get(ptr++)),Integer.parseInt(data.get(ptr++))};
				if (size[0] <= 0 || size[1] <= 0) return false;
				int [][] gB = new int [size[0]][size[1]];
				if (data.size() < (2 + size[0]*size[1] + 9)) return false;
				int i = 0;
				for (;i<= size[0]*size[1];i++) {
					gB [i/size[0]][i%size[1]] = Integer.parseInt(data.get(ptr++));
				}
				int [][] shapeCoords = new int [4][2];
				for (int j=0;j<8;j++) {
					shapeCoords [j/2][j%2] = Integer.parseInt(data.get(ptr++));
				}
				SHAPETYPE type = SHAPETYPE.valueOf(data.get(ptr++));
				if (this.engine.getGameBoard().length != size[0] || this.engine.getGameBoard()[0].length != size[1]) {
				///	GameWindow newGw = new GameWindow ();
				}
				
				for (int j=0;j<gB.length;j++) {
					for (int k=0;k<gB[0].length;k++) {
						
					}
				}
				this.engine.colorSpace(ptr, ptr, i);;
				
			} catch (NumberFormatException e) {
				return false;
			}
			return true;
		}
		public AIToolsWindow(GameWindow gw, Engine engine, double squareSize, int x, int y) {
			this.gw = gw;
			int dimension = (int)(4*squareSize);
			setBounds(x, y, dimension, dimension - 5);
			this.setSize(dimension -8, dimension);
			this.setResizable(false);

			setTitle("AI Tools");
			this.engine = engine;
			
			filename.setEditable(false);
			dir.setEditable(false);
			
			JButton saveBtn = new JButton ("Save State");
			JButton loadBtn = new JButton ("Load State");

			saveBtn.addActionListener(new SaveL());
			loadBtn.addActionListener(new OpenL());
			
			add(filename);
			add(saveBtn);
			add(loadBtn);
			this.pack();
		}
		class OpenL implements ActionListener {
			public void actionPerformed(ActionEvent e) {
				JFileChooser c = new JFileChooser();
				// Demonstrate "Open" dialog:
				int rVal = c.showOpenDialog(AIToolsWindow.this);
				if (rVal == JFileChooser.APPROVE_OPTION) {
					filename.setText(c.getSelectedFile().getName());
					dir.setText(c.getCurrentDirectory().toString());
				}
				if (rVal == JFileChooser.CANCEL_OPTION) {
				}
			}
		}
		class SaveL implements ActionListener {
			public void actionPerformed(ActionEvent e) {
				JFileChooser c = new JFileChooser();
				// Demonstrate "Save" dialog:
				int rVal = c.showSaveDialog(AIToolsWindow.this);
				if (rVal == JFileChooser.APPROVE_OPTION) {
					filename.setText(c.getSelectedFile().getName());
					dir.setText(c.getCurrentDirectory().toString());
				}
				if (rVal == JFileChooser.CANCEL_OPTION) {
				}
			}
		}

	}
