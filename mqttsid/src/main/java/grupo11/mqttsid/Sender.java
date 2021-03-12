package grupo11.mqttsid;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.SerializationUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

public class Sender extends Thread implements Callable<Void> {
	
	public static final String TOPIC = "sid_g11_xpexial";
	private ObjectId lastID;
	private File auxID;
	
	private Bson typeFilter;
	private String type;
	private MongoCollection<Document> localCollection;
	
	private IMqttClient publisher;
	private byte[] payload;
	
	public Sender(IMqttClient publisher, String sensor, MongoDatabase localDB) {
        this.publisher = publisher;
        this.localCollection = localDB.getCollection("sensor" + sensor);
        this.type = sensor.toUpperCase();
		this.typeFilter = Filters.eq("Sensor", type);
		new File(".\\lastIDs").mkdir();
		this.auxID = new File(".\\lastIDs\\lastID_" + sensor + ".txt");
		this.lastID = getLastID();
    }
	
	private void setLastID(Object lastID) {
		try {
			PrintWriter writer = new PrintWriter(auxID);
			writer.print(lastID);
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} 
	}
	
	private ObjectId getLastID() {
		try {
			if(!auxID.exists()) {
				this.auxID.createNewFile();
			}
			Scanner scanner = new Scanner(auxID);
			if(scanner.hasNext()) {
				ObjectId aux = new ObjectId(scanner.next());
				scanner.close();
				return aux;
			} else {
				scanner.close();
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void run() {
		
		FindIterable<Document> sorted = localCollection.find(typeFilter);
		
		if(lastID != null) {
			Bson bsonFilter = Filters.gt("_id", lastID);
			sorted = sorted.filter(bsonFilter);
		}
		
		while(true) {
			for(Document d : sorted) {
				try {
					System.out.println("sender:" + d);
					payload = SerializationUtils.serialize(d);
					call();
					lastID = (ObjectId) d.get("_id");
					setLastID(lastID);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			Bson bsonFilter = Filters.and(Filters.gt("_id", lastID), typeFilter);
			sorted = sorted.filter(bsonFilter);
        }
	}
	
	@Override
	public Void call() throws Exception {
		if (!publisher.isConnected()) {
            return null;
        }
        MqttMessage msg = new MqttMessage(payload);
        msg.setQos(2);
        msg.setRetained(true);
        publisher.publish(TOPIC,msg);        
        return null;
	}

}
