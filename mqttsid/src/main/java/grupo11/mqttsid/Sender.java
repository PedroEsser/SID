package grupo11.mqttsid;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.SerializationUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;

import com.mongodb.MongoInterruptedException;
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
	
	private LocalDateTime lastDate;
	
	public Sender(IMqttClient publisher, String sensor, MongoDatabase localDB) {
        this.publisher = publisher;
        this.localCollection = localDB.getCollection("sensor" + sensor);
		//lastDate = LocalDateTime.now().minusMinutes(30);
        lastDate = LocalDateTime.now().minusDays(2).minusHours(11).minusMinutes(20);
    }
	
	public void run() {
		try {
			upToDate(2);
			while(true) {
				try {
					boolean success = produceAndSendMedianUntil(LocalDateTime.now());
					if(success) 
						lastDate = LocalDateTime.now();
				} catch (MqttException e) {
					e.printStackTrace();
				}
				sleep(2000);
			}
			
		} catch (InterruptedException | MongoInterruptedException | Error e) {
		}
	}
	
	private void upToDate(int timeStep) {
		LocalDateTime nextDate = lastDate.plusSeconds(timeStep);
		while(nextDate.isBefore(LocalDateTime.now())) {
			try {
				System.out.println(lastDate);
				System.out.println(produceAndSendMedianUntil(nextDate));
			} catch (MqttException e) {
				e.printStackTrace();
			}
			lastDate = nextDate;
			nextDate = lastDate.plusSeconds(timeStep);
		};
	}
	
	private boolean produceAndSendMedianUntil(LocalDateTime nextDate) throws MqttException {
		List<Document> docs = getDocumentsUntil(nextDate);
		if(!docs.isEmpty()) {
			double median = Utils.medianOf(docs);
			Document d = docs.get(0);
			d.replace("Medicao", median);
			d.replace("Data", Utils.standardFormat(nextDate));
			sendDocument(d);
			return true;
		}
		return false;
	}
	
	private List<Document> getDocumentsUntil(LocalDateTime nextDate){
		Bson filter = nextDateFilter(nextDate);
		
		FindIterable<Document> localDocuments = localCollection.find();
		localDocuments.filter(filter);
		List<Document> medicoes = new ArrayList<Document>();
		
		for(Document d : localDocuments)
			medicoes.add(d);
		
		return medicoes;
	}
	
	private Bson nextDateFilter(LocalDateTime nextDate) {
		Bson gteFilter = Filters.gte("Data", Utils.standardFormat(lastDate));
		Bson ltFilter = Filters.lt("Data", Utils.standardFormat(nextDate));
		return Filters.and(gteFilter, ltFilter);
	}
	
	private void sendDocument(Document d) throws MqttException {
		Main.gui.addData("sender:" + d + "\n");
		payload = SerializationUtils.serialize(d);
		call();
	}
	
	public IMqttClient getPublisher() {
		return publisher;
	}

	@Override
	public Void call() throws MqttException {
		if (!publisher.isConnected()) {
            return null;
        }
        publisher.publish(TOPIC, payload, 2, false);
        return null;
	}

}
