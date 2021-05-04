package grupo11.projetosid;

import com.mongodb.BasicDBObject;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;

public class SensorDataWriter extends Thread {

	private static final int BATCHSIZE = 20000;
	public static final int MARGIN = 5;
	
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
		return localCollection.find(typeFilter).sort(Sorts.descending("Data")).first().get("_id");
	}
	
	public void run(){
		if(delete) {
			GUI.gui.addData("Clearing the collection from the sensor " + type + "...\n");
			localCollection.deleteMany(new BasicDBObject());
		}
		
		FindIterable<Document> aux = cloudCollection.find(typeFilter).sort(Sorts.descending("_id")).limit(BATCHSIZE);
		Object lastID = getLastID();
		if(lastID != null) {
			Bson bsonFilter = Filters.gt("_id", lastID);
			aux = aux.filter(bsonFilter);
		}
		List<Document> sorted = new LinkedList<>();
		aux.forEach(d -> sorted.add(d));
		sorted.sort((d1,d2) -> d1.getObjectId("_id").compareTo(d2.getObjectId("_id")));
		
		Document lastDocument = null;
		while(!interrupted()) {
			try {
				int count = 0;
				ArrayList<Document> list = new ArrayList<Document>();
				LocalDateTime lastDate = LocalDateTime.now().minusMinutes(MARGIN);
				for(Document d : sorted) {
					String formattedDate = Utils.parseDate(d.getString("Data"));
					d.replace("Data", formattedDate);
					if(!Utils.equals(d, lastDocument) && Utils.isValid(d) && Utils.stringToDate(formattedDate).isAfter(lastDate)) {
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
				Bson bsonFilter = Filters.and(Filters.gt("_id", getLastID()), typeFilter);
				sorted.clear();
				aux = cloudCollection.find(bsonFilter);
				aux.forEach(d -> sorted.add(d));
				sleep(1000);
			} catch (Exception | Error e) {
				interrupt();
			}
        }	
	}
	
}
