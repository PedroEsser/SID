package grupo11.projetosid;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoInterruptedException;
import com.mongodb.MongoTimeoutException;
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
	
	private String sensor;
	private MongoCollection<Document> cloudCollection;
	private MongoCollection<Document> localCollection;
	private String type;
	private Bson typeFilter;
	
	public SensorDataWriter(String sensor, MongoDatabase cloudDB, MongoDatabase localDB, boolean delete) {
		this.sensor = sensor;
		this.cloudCollection = cloudDB.getCollection("sensor" + sensor);
		this.localCollection = localDB.getCollection("sensor" + sensor);
		this.type = sensor.toUpperCase();
		this.typeFilter = Filters.and(Filters.eq("Sensor", type), Filters.eq("Zona", type.replaceFirst("^.", "Z")));
		if(delete) {
			SensorDataWriterGUI.gui.addData("Clearing the collection from the sensor " + type + "...\n");
			localCollection.deleteMany(new BasicDBObject());
		}
	}
	
	public void run(){
		try {			
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
							SensorDataWriterGUI.gui.addData(d+"\n");
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
				} catch (InterruptedException | MongoInterruptedException | Error e) {
					interrupt();
				}
	        }
		} catch(MongoTimeoutException e) {
			restartMongo();
		}
	}
	
	private Object getLastID() {
		if(localCollection.countDocuments() == 0)
			return null;
		return localCollection.find(typeFilter).sort(Sorts.descending("Data")).first().get("_id");
	}

	private void restartMongo() {
		try {
			MongoClient profMongoClient = MongoClients.create(SensorDataWriterGUI.PROF_URI);
			MongoDatabase profMongoDB = profMongoClient.getDatabase("sensors");
			this.cloudCollection = profMongoDB.getCollection("sensor" + sensor);
			MongoClient ourMongoClient = MongoClients.create(SensorDataWriterGUI.OUR_URI);
			MongoDatabase ourMongoDB = ourMongoClient.getDatabase("sensors");
			this.localCollection = ourMongoDB.getCollection("sensor" + sensor);
			sleep(1000);
			run();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
}
