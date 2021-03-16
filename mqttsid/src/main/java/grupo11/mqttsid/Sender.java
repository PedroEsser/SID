package grupo11.mqttsid;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.SerializationUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.eclipse.paho.client.mqttv3.IMqttClient;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;

public class Sender extends Thread implements Callable<Void> {
	
	public static final String TOPIC = "sid_g11";
	
	private MongoCollection<Document> localCollection;
	
	private IMqttClient publisher;
	private byte[] payload;
	
	private String lastDate;
	private SQLHandler sqlmanager;
	
	public Sender(IMqttClient publisher, String sensor, MongoDatabase localDB) {
        this.publisher = publisher;
        this.localCollection = localDB.getCollection("sensor" + sensor);
		this.sqlmanager = new SQLHandler("jdbc:mysql://localhost:3306/projetosid", "root", "");
		setLastDate();
    }
	
	private void setLastDate() {
		ResultSet aux = sqlmanager.queryDB("select hora from medicao order by hora DESC limit 1");
		try {
			if(aux.next()) {
				lastDate = aux.getString("hora");
			}
		} catch (SQLException e) {
			e.printStackTrace();
			lastDate = null;
		}
	}
	
	public void run() {
		
		FindIterable<Document> sorted = localCollection.find().sort(Sorts.ascending("Data"));
		
		if(lastDate != null) {
			Bson bsonFilter = Filters.gt("Data", lastDate);
			sorted = sorted.filter(bsonFilter);
		}
		
		while(true) {
			try {
				sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			for(Document d : sorted) {
				try {
					System.out.println("sender:" + d);
					payload = SerializationUtils.serialize(d);
					call();
					lastDate = d.getString("Data");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			sorted = sorted.filter(Filters.gt("Date", lastDate));
        }
	}
	
	@Override
	public Void call() throws Exception {
		if (!publisher.isConnected()) {
            return null;
        }
        publisher.publish(TOPIC, payload, 2, false);
        return null;
	}

}
