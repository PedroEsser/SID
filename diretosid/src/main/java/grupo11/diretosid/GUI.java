package grupo11.diretosid;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;

import org.eclipse.paho.client.mqttv3.MqttException;

public class GUI {

	private Runnable r;
	private JTextArea console;
	private JScrollPane scroll;
	private int count = 0;

	public GUI(Runnable r) {
		this.r = r;
		create();
	}

	private void create() {
		JFrame window = new JFrame("Mongo To MySQL");
		JPanel panel = new JPanel(new BorderLayout());
		console = new JTextArea();
		console.setEditable(false);
		scroll = new JScrollPane(console);
		JButton start = new JButton("Start");
		panel.add(BorderLayout.CENTER, scroll);
		panel.add(BorderLayout.SOUTH, start);
        start.addActionListener(e -> {
        	if(start.getText().equals("Start")) {
        		r.run();
        		start.setText("Stop");
        	} else {
//        		try {
	        		for(Sender s: Main.senders) {
//						s.getPublisher().disconnect();
	        			s.interrupt();
	        		}
//					Main.receiver.getSubscriber().disconnect();
	        		start.setText("Start");
//        		} catch (MqttException e1) {
//					e1.printStackTrace();
//				}
        	}
        });
		window.add(panel);
		window.setSize(800, 400);
		window.setVisible(true);
		window.setLocationRelativeTo(null);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public synchronized void addData(String data) {
		console.append(data);
		JScrollBar vertical = scroll.getVerticalScrollBar();
		vertical.setValue(vertical.getMaximum());
		if (++count > 100) {
			try {
				int end = console.getLineEndOffset(0);
				console.replaceRange("", 0, end);
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
	}
}
