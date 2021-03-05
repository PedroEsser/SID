package grupo11.projetosid;

import com.mongodb.client.*;
import com.mongodb.client.model.Sorts;

import filters.DateFilter;
import filters.DocumentFilter;
import filters.SensorTypeFilter;

import java.util.ArrayList;

import org.bson.Document;

public class SensorDataWriter extends Thread{

	ArrayList<DocumentFilter> filters = new ArrayList();
	String lastDate;
	MongoCollection<Document> cloudCollection;
	MongoCollection<Document> remoteCollection;
	
	public SensorDataWriter(String sensor, MongoDatabase cloudDB, MongoDatabase remoteDB) {
		cloudCollection = cloudDB.getCollection("sensor" + sensor);
		remoteCollection = remoteDB.getCollection("zone" + sensor.charAt(1));
		lastDate = getLastDate();
		filters.add(new SensorTypeFilter(sensor.toUpperCase()));
		//filters.add(new DateFilter(getLastDate()));
		//System.out.println(getLastDate());
		
	}
	
	private String getLastDate() {
		return cloudCollection.find().sort(Sorts.descending("Data")).first().getString("Data");
	}
	
	public void run(){
		FindIterable<Document> sorted = cloudCollection.find().sort(Sorts.descending("Data"));
		
		
		while(true) {
			/*System.out.println(sorted.first().getString("Data"));
			System.out.println("#########################");
			//lastDate = sorted.first().getString("Data");
			System.out.println(sorted.first().getString("Data"));*/
			//System.out.println(sorted.get(0));
        	try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        }
		
	}
	
}
