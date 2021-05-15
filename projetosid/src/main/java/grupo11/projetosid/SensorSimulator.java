package grupo11.projetosid;

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
	
	public SensorSimulator(IMqttClient subscriber, String sensor, MongoDatabase localDB) {
		this.subscriber = subscriber;
		localCollection = localDB.getCollection("sensor" + sensor);
		serve();
	}
	
	public void serve() {
		try {
			subscriber.subscribe("simulator_sid_g11", 2, (topic, msg) -> {
			    byte[] payload = msg.getPayload();
			    String aux = new String(payload);
			    Document doc = Document.parse(aux);
			    System.out.println(doc);
			    localCollection.insertOne(doc);
			});
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		try {
			IMqttClient subscriber = new MqttClient("tcp://broker.mqtt-dashboard.com:1883", "sensor_simulator_grupo11");
			MqttConnectOptions options = new MqttConnectOptions();
			options.setAutomaticReconnect(true);
			options.setCleanSession(true);
			subscriber.connect(options);
			
			final String ourURI = "mongodb://localhost:25017,localhost:24017,localhost:23017/?replicaSet=projetosid&readPreference=primary&appname=MongoDB%20Compass&ssl=false";
	        MongoClient ourMongoClient = MongoClients.create(ourURI);
	        MongoDatabase ourMongoDB = ourMongoClient.getDatabase("sensors");
			
			new SensorSimulator(subscriber, "t1", ourMongoDB);
		} catch (MqttException e) {
			e.printStackTrace();
		}
		
	}
	
}
