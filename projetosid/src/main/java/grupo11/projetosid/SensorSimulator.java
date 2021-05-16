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
	private String sensor;
	
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
			    System.out.println(doc);
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
			
			final String ourURI = "mongodb://localhost:27017/?readPreference=primary&appname=MongoDB%20Compass&ssl=false";
	        MongoClient ourMongoClient = MongoClients.create(ourURI);
	        MongoDatabase ourMongoDB = ourMongoClient.getDatabase("sensors");
			
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
	
}
