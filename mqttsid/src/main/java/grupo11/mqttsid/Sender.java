package grupo11.mqttsid;

import java.util.concurrent.Callable;

import org.apache.commons.lang3.SerializationUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;

public class Sender extends Thread implements Callable<Void> {
	
	public static final String TOPIC = "sid_g11_xpexial";
	Object lastID;
	
	Bson typeFilter;
	String type;
	MongoCollection<Document> localCollection;
	
	IMqttClient publisher;
	byte[] payload;
	
	public Sender(IMqttClient publisher, String sensor, MongoDatabase localDB) {
        this.publisher = publisher;
        this.localCollection = localDB.getCollection("sensor" + sensor);
        this.type = sensor.toUpperCase();
		this.typeFilter = Filters.eq("Sensor", type);
    }
	
	private Object getLastID() {
		if(localCollection.countDocuments() == 0)
			return null;
		return localCollection.find(typeFilter).sort(Sorts.descending("_id")).first().get("_id");
	}
	
	public void run() {
		
		/*Object lastID = getLastID();
		System.out.println("Last ID of " + type + ": "  + lastID);*/
		FindIterable<Document> sorted = localCollection.find(typeFilter);
		
		/*if(lastID != null) {
			Bson bsonFilter = Filters.gt("_id", lastID);
			sorted = sorted.filter(bsonFilter);
		}*/
		
		while(true) {
			for(Document d : sorted) {
				try {
					payload = SerializationUtils.serialize(d);
					call();
					//
					lastID = d.get("_id");
					//
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			Bson bsonFilter = Filters.and(Filters.gt("_id", lastID /*getLastID()*/), typeFilter);
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
