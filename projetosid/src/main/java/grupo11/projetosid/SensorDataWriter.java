package grupo11.projetosid;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;

import filters.DateFilter;
import filters.DocumentFilter;
import filters.SensorTypeFilter;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;

public class SensorDataWriter extends Thread {

	private static final int BATCHSIZE = 20000;
	
	Bson typeFilter;
	String type;
	MongoCollection<Document> cloudCollection;
	MongoCollection<Document> remoteCollection;
	
	public SensorDataWriter(String sensor, MongoDatabase cloudDB, MongoDatabase remoteDB) {
		cloudCollection = cloudDB.getCollection("sensor" + sensor);
		remoteCollection = remoteDB.getCollection("sensor" + sensor);
		type = sensor.toUpperCase();
		typeFilter = Filters.eq("Sensor", type);
		//filters.add(new DateFilter(getLastDate()));
		//System.out.println(getLastDate());
		
	}
	
	private Object getLastID() {
		if(remoteCollection.countDocuments() == 0)
			return null;
		return remoteCollection.find(typeFilter).sort(Sorts.descending("_id")).first().get("_id");
	}
	
	public void run(){
		Object lastID = getLastID();
		System.out.println("Last ID of " + type + ": "  + lastID);
		FindIterable<Document> sorted = cloudCollection.find(typeFilter).batchSize(BATCHSIZE);
		
		if(lastID != null) {
			Bson bsonFilter = Filters.gt("_id", lastID);
			sorted = sorted.filter(bsonFilter);
		}
		
		//List<Bson> pipeline = singletonList(match(in("operationType", asList("insert", "delete"))));
		//List<Bson> pipeline = new ArrayList<Bson>();
		//pipeline.add(Filters.eq("operationType", "insert"));

		/*cloudCollection.watch().forEach(e -> {
			System.out.println(e);
		});*/
		
		while(true) {
			int count = 0;
			ArrayList<Document> list = new ArrayList<Document>();
			for(Document d : sorted) {
				list.add(d);
				if(count++ % BATCHSIZE == 0) {
					System.out.println(count + "th Document from sensor " + d.getString("Sensor") + " inserted.");
					remoteCollection.insertMany(list);
					list.clear();
				}
			}
			if(list.isEmpty()) {
				System.out.println(type + " didn't insert anything");
			} else {
				remoteCollection.insertMany(list);
				System.out.println(type + ", Count: " + count);
			}
			//System.out.println(remoteCollection.countDocuments());
			Bson bsonFilter = Filters.and(Filters.gt("_id", getLastID()), typeFilter);
			sorted = sorted.filter(bsonFilter);
        }
		
	}
	
}
