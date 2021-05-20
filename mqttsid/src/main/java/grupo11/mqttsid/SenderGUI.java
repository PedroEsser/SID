package grupo11.mqttsid;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class SenderGUI {
	
	public static final String OUR_URI = "mongodb://localhost:25017,localhost:24017,localhost:23017/?replicaSet=projetosid&readPreference=primary&appname=MongoDB%20Compass&ssl=false";
	public static SenderGUI gui;
	private static Sender[] senders;
	
	private Runnable r;
	private JTextArea console;
	private JScrollPane scroll;
	private int count = 0;

	public SenderGUI(Runnable r) {
		this.r = r;
		create();
	}
	
	private void create() {
		JFrame window = new JFrame("(Sender) Mongo To MySQL");
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
        		try {
	        		for(Sender s: senders) {
	        			if(s.getPublisher().isConnected()) {
	        				s.getPublisher().disconnect();
	        			}
	        			s.interrupt();
	        		}
	        		start.setText("Start");
        		} catch (MqttException e1) {
					e1.printStackTrace();
				}
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
		if(++count > 100) {
			try {
				int end = console.getLineEndOffset(0);
				console.replaceRange("", 0, end);
			} catch (BadLocationException e) {}
		}
	}
	
	public static void main(String[] args) {
		
        MongoClient ourMongoClient = MongoClients.create(OUR_URI);
        MongoDatabase ourMongoDB = ourMongoClient.getDatabase("sensors");
        
        Runnable r = () -> {
			try {
				IMqttClient publisher = new MqttClient("tcp://broker.mqtt-dashboard.com:1883", "publisher_grupo11");
				MqttConnectOptions options = new MqttConnectOptions();
				options.setConnectionTimeout(0);
				options.setAutomaticReconnect(true);
				options.setCleanSession(true);
				publisher.connect(options);
				
				senders = new Sender[6];
				senders[0] = new Sender(publisher, "t1", ourMongoDB);
				senders[1] = new Sender(publisher, "h1", ourMongoDB);
				senders[2] = new Sender(publisher, "l1", ourMongoDB);
				senders[3] = new Sender(publisher, "h2", ourMongoDB);
				senders[4] = new Sender(publisher, "t2", ourMongoDB);
				senders[5] = new Sender(publisher, "l2", ourMongoDB);
				
				for(Sender sender : senders) {
		        	sender.start();
				}
			} catch (MqttException e) {
				e.printStackTrace();
			}
		};

		gui = new SenderGUI(r);
	}
	
}
