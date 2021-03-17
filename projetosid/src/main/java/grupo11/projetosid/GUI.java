package grupo11.projetosid;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;

public class GUI {
	
	private SensorDataWriter[] dataWriters;
	private JTextArea console;
	private JScrollPane scroll;
	private int count = 0;

	public GUI(SensorDataWriter[] dataWriters) {
		this.dataWriters = dataWriters;
		create();
	}
	
	private void create() {
		JFrame window = new JFrame("Mongo To Mongo");
		JPanel panel = new JPanel(new BorderLayout()); 
		console = new JTextArea();
		console.setEditable(false);
		scroll = new JScrollPane(console);
        JButton start = new JButton("Start"); 
        panel.add(BorderLayout.CENTER, scroll);
        panel.add(BorderLayout.SOUTH, start);
        start.addActionListener(e-> {
        	if(start.getText().equals("Start")) {
        		for(SensorDataWriter writer : this.dataWriters)
                	writer.start();
        		start.setText("Stop");
        	} else {
        		Main.running = false;
        		start.setText("Start");
        	}
        });
        window.add(panel); 
        window.setSize(800, 400); 
        window.setVisible(true);
        window.setLocationRelativeTo(null);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	public void addData(String data) {
		console.append(data);
		JScrollBar vertical = scroll.getVerticalScrollBar();
		vertical.setValue(vertical.getMaximum());
		if(++count > 100) {
			try {
				int end = console.getLineEndOffset(0);
				console.replaceRange("", 0, end);
			} catch (BadLocationException e) {}
		}
	}
	
}
