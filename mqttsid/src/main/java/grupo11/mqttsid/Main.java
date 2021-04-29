package grupo11.mqttsid;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class Main {
	
	public static GUI gui;
	public static Sender[] senders;
	public static Receiver receiver;

	public static void main(String[] args) {
		
		final String ourURI = "mongodb://localhost:25017,localhost:24017,localhost:23017/?replicaSet=projetosid&readPreference=primary&appname=MongoDB%20Compass&ssl=false";
        MongoClient ourMongoClient = MongoClients.create(ourURI);
        MongoDatabase ourMongoDB = ourMongoClient.getDatabase("sensors");
	        
		Runnable r = () -> {
			try {
				IMqttClient publisher = new MqttClient("tcp://broker.mqtt-dashboard.com:1883", "publisher_grupo11");
				IMqttClient subscriber = new MqttClient("tcp://broker.mqtt-dashboard.com:1883", "subscriber_grupo11");
				MqttConnectOptions options = new MqttConnectOptions();
				options.setAutomaticReconnect(true);
				options.setCleanSession(true);
//				options.setCleanSession(false);
				publisher.connect(options);
				subscriber.connect(options);
				
				senders = new Sender[1];
				senders[0] = new Sender(publisher, "t1", ourMongoDB);
//				senders[1] = new Sender(publisher, "h1", ourMongoDB);
//				senders[2] = new Sender(publisher, "l1", ourMongoDB);
//				senders[3] = new Sender(publisher, "h2", ourMongoDB);
//				senders[4] = new Sender(publisher, "t2", ourMongoDB);
//				senders[5] = new Sender(publisher, "l2", ourMongoDB);
				
				for(Sender sender : senders)
		        	sender.start();
				
//				try {
//					Thread.sleep(2000);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
				
				receiver = new Receiver(subscriber);
			} catch (MqttException e) {
				e.printStackTrace();
			}
		};
			
		gui = new GUI(r);
		
	}
	
}
