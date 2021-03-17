package grupo11.projetosid;

import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;

import java.util.ArrayList;

import org.bson.Document;
import org.bson.conversions.Bson;

public class SensorDataWriter extends Thread {

	private static final int BATCHSIZE = 20000;
	
	private Bson typeFilter;
	private String type;
	private MongoCollection<Document> cloudCollection;
	private MongoCollection<Document> localCollection;
	
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
	
	private boolean equals(Document d1, Document d2) {
		if(d1==null || d2==null) {
			return false;
		}
		return  d1.get("Zona").equals(d2.get("Zona")) &&
				d1.get("Sensor").equals(d2.get("Sensor")) &&
				d1.get("Data").equals(d2.get("Data")) &&
				d1.get("Medicao").equals(d2.get("Medicao"));
	}
	
	public void run(){
		Object lastID = getLastID();
		
		Document lastDocument = null;
		
		FindIterable<Document> sorted = cloudCollection.find(typeFilter).batchSize(BATCHSIZE);
		
		if(lastID != null) {
			Bson bsonFilter = Filters.gt("_id", lastID);
			sorted = sorted.filter(bsonFilter);
		}
		
		while(Main.running) {
			int count = 0;
			ArrayList<Document> list = new ArrayList<Document>();
			
			for(Document d : sorted) {
				d.replace("Data", DateUtils.parse(d.getString("Data")));
				if(!equals(d, lastDocument)) {
					list.add(d);
					Main.gui.addData(d+"\n");
					if(++count % BATCHSIZE == 0) {
						localCollection.insertMany(list);
						list.clear();
					}
				}
				lastDocument = d;
			}
			if(!list.isEmpty()) {
				localCollection.insertMany(list);
			}
			Bson bsonFilter = Filters.and(Filters.gt("_id", getLastID()), typeFilter);
			sorted = sorted.filter(bsonFilter);
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
        }
		
	}
	
}
