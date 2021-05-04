package grupo11.projetosid;

import com.mongodb.BasicDBObject;
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
	private boolean delete;
	
	public SensorDataWriter(String sensor, MongoDatabase cloudDB, MongoDatabase localDB, boolean delete) {
		cloudCollection = cloudDB.getCollection("sensor" + sensor);
		localCollection = localDB.getCollection("sensor" + sensor);
		type = sensor.toUpperCase();
		typeFilter = Filters.and(Filters.eq("Sensor", type), Filters.eq("Zona", type.replaceFirst("^.", "Z")));
		this.delete = delete;
	}
	
	private Object getLastID() {
		if(localCollection.countDocuments() == 0)
			return null;
		return localCollection.find(typeFilter).sort(Sorts.descending("_id")).first().get("_id");
	}
	
	public void run(){
		if(delete) {
			GUI.gui.addData("Clearing the collection from the sensor " + type + "...\n");
			localCollection.deleteMany(new BasicDBObject());
		}
		
		FindIterable<Document> sorted = cloudCollection.find(typeFilter).batchSize(BATCHSIZE);
		Object lastID = getLastID();
		if(lastID != null) {
			Bson bsonFilter = Filters.gt("_id", lastID);
			sorted = sorted.filter(bsonFilter);
		}
//		Bson dateFilter = Filters.gt("Data", DateUtils.getCurrentDateMinus(30));
//		sorted = sorted.filter(dateFilter);
		
		Document lastDocument = null;
		while(!interrupted()) {
			try {			
				int count = 0;
				ArrayList<Document> list = new ArrayList<Document>();
				for(Document d : sorted) {
					d.replace("Data", Utils.parseDate(d.getString("Data")));
					if(!Utils.equals(d, lastDocument) && Utils.isValid(d)) {
						list.add(d);
						GUI.gui.addData(d+"\n");
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
				Bson bsonFilter = Filters.and(Filters.gt("_id", getLastID())/*, Filters.gt("Data", Utils.getCurrentDateMinus(30))*/, typeFilter);
				sorted = sorted.filter(bsonFilter);
			} catch (Exception | Error e) {
				interrupt();
			}
        }	
	}
	
}
