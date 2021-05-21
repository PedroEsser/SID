package grupo11.projetosid;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;

import org.bson.Document;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class SensorSimulator {
	
	private IMqttClient subscriber;
	private MongoCollection<Document> localCollection;
	private String sensor;
	public static JTextArea documentLabel = new JTextArea();
	public static JScrollPane jScrollPane;
	public static int count = 0;
	
	public SensorSimulator(IMqttClient subscriber, String sensor, MongoDatabase localDB) {
		this.subscriber = subscriber;
		this.sensor = sensor;
		this.localCollection = localDB.getCollection("sensor" + sensor);
		serve();
	}
	
	public void serve() {
		try {
			subscriber.subscribe("simulator_sid_g11_" + sensor, 2, (topic, msg) -> {
			    byte[] payload = msg.getPayload();
			    String aux = new String(payload);
			    Document doc = Document.parse(aux);
			    localCollection.insertOne(doc);
			    synchronized(documentLabel) {
			    	addData(doc.toString() + "\n");
			    }
			});
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		try {
			IMqttClient subscriber = new MqttClient("tcp://broker.mqtt-dashboard.com:1883", "sensor_simulator_grupo11");
			MqttConnectOptions options = new MqttConnectOptions();
			options.setConnectionTimeout(0);
			options.setAutomaticReconnect(true);
			options.setCleanSession(true);
			subscriber.connect(options);
			
			final String ourURI = "mongodb://localhost:27017/?readPreference=primary&appname=MongoDB%20Compass&ssl=false";
	        MongoClient ourMongoClient = MongoClients.create(ourURI);
	        MongoDatabase ourMongoDB = ourMongoClient.getDatabase("sensors");
	        
	        createWindow();
			
			new SensorSimulator(subscriber, "t1", ourMongoDB);
			new SensorSimulator(subscriber, "t2", ourMongoDB);
			new SensorSimulator(subscriber, "h1", ourMongoDB);
			new SensorSimulator(subscriber, "h2", ourMongoDB);
			new SensorSimulator(subscriber, "l1", ourMongoDB);
			new SensorSimulator(subscriber, "l2", ourMongoDB);
		} catch (MqttException e) {
			e.printStackTrace();
		}
		
	}
	
	public static void createWindow() {
		JFrame jFrame = new JFrame("SimulateSensor");
		jFrame.setDefaultCloseOperation(3);
		JLabel jLabel = new JLabel("Data from Cloud: ", 0);
		jLabel.setPreferredSize(new Dimension(600, 30));
		jScrollPane = new JScrollPane(documentLabel, 22, 32);
		jScrollPane.setPreferredSize(new Dimension(600, 200));
		JButton jButton = new JButton("Stop the program");
		jFrame.getContentPane().add(jLabel, "First");
		jFrame.getContentPane().add(jScrollPane, "Center");
		jFrame.getContentPane().add(jButton, "Last");
		jFrame.setLocationRelativeTo(null);
		jFrame.pack();
		jFrame.setVisible(true);
		jButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent param1ActionEvent) {
				System.exit(0);
			}
		});
	}
	
	public static void addData(String data) {
		documentLabel.append(data);
		JScrollBar vertical = jScrollPane.getVerticalScrollBar();
		vertical.setValue(vertical.getMaximum());
		if(++count > 100) {
			try {
				int end = documentLabel.getLineEndOffset(0);
				documentLabel.replaceRange("", 0, end);
			} catch (BadLocationException e) {}
		}
	}
	
}
