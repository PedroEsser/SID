package grupo11.projetosid;

import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;

import java.util.ArrayList;

import org.bson.Document;
import org.bson.conversions.Bson;

public class SensorDataWriter extends Thread {

	private static final int BATCHSIZE = 20000;
	
	Bson typeFilter;
	String type;
	MongoCollection<Document> cloudCollection;
	MongoCollection<Document> localCollection;
	
	public SensorDataWriter(String sensor, MongoDatabase cloudDB, MongoDatabase localDB) {
		cloudCollection = cloudDB.getCollection("sensor" + sensor);
		localCollection = localDB.getCollection("sensor" + sensor);
		type = sensor.toUpperCase();
		typeFilter = Filters.eq("Sensor", type);
	}
	
	private Object getLastID() {
		if(localCollection.countDocuments() == 0)
			return null;
		return localCollection.find(typeFilter).sort(Sorts.descending("_id")).first().get("_id");
	}
	
	public void run(){
		Object lastID = getLastID();
		System.out.println("Last ID of " + type + ": "  + lastID);
		FindIterable<Document> sorted = cloudCollection.find(typeFilter).batchSize(BATCHSIZE);
		
		if(lastID != null) {
			Bson bsonFilter = Filters.gt("_id", lastID);
			sorted = sorted.filter(bsonFilter);
		}
		
		while(true) {
			int count = 0;
			ArrayList<Document> list = new ArrayList<Document>();
			for(Document d : sorted) {
				list.add(d);
				if(count++ % BATCHSIZE == 0) {
					System.out.println(count + "th Document from sensor " + d.getString("Sensor") + " inserted.");
					localCollection.insertMany(list);
					list.clear();
				}
			}
			if(list.isEmpty()) {
				System.out.println(type + " didn't insert anything");
			} else {
				localCollection.insertMany(list);
				System.out.println(type + ", Count: " + count);
			}
			Bson bsonFilter = Filters.and(Filters.gt("_id", getLastID()), typeFilter);
			sorted = sorted.filter(bsonFilter);
        }
		
	}
	
}
