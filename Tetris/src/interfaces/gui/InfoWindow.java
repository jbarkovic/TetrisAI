package interfaces.gui;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;


public class InfoWindow extends JFrame {

	private ShapeDisplay sd;
	
	private HashMap <String, String> data;
	private JTable table;
	private JScrollPane scrollPane;
	private JPanel topPanel;

	public static void start() {
		EventQueue.invokeLater(new Runnable() {	
			public void run() {
				try {
					InfoWindow frame = new InfoWindow();
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
	public void update (Map.Entry<String, String> dataEntry) {
		data.put(dataEntry.getKey(), dataEntry.getValue());
		writeData();
	}
	public void writeData () {
		table.
		
	}
	private String [][] hashToArray () {
		ArrayList<String []> array = new ArrayList<String[]> (data.size());
		for (Map.Entry<String, String> line : data.entrySet()) {
			String [] pair = new String [2];
			pair[0] = line.getKey();
			pair[1] = line.getValue();
			
			array.add(pair);
		}
		return array.toArray(new String[][]{});
	}
	public InfoWindow () {	
		data = new HashMap<String, String> ();
		setSize(300, 200);
		setTitle("Info");
		topPanel = new JPanel();
		topPanel.setLayout(new BorderLayout());
		getContentPane().add(topPanel);
		
		// Create a new table instance
		table = new JTable( hashToArray(), new String[] {"Key", "Value"});

		// Configure some of JTable's paramters
		table.setShowHorizontalLines( false );
		table.setRowSelectionAllowed( true );
		table.setColumnSelectionAllowed( true );

		// Change the selection colour
		table.setSelectionForeground( Color.white );
		table.setSelectionBackground( Color.red );

		// Add the table to a scrolling pane
		scrollPane = table.createScrollPaneForTable( table );
		topPanel.add( scrollPane, BorderLayout.CENTER );
		
		setFocusable(false);
		add(this.sd);
		this.pack();
	}

}