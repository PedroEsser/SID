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
		lastDate = LocalDateTime.now().minusMinutes(30);
    }
	
	public void run() {
		while(!interrupted()) {
			try {
				
				while(produceAndSendMedianOfNext(2));
				
				sleep(2000);
			} catch (InterruptedException | MongoInterruptedException | Error e) {
				interrupt();
			} catch (MqttException e) {
				e.printStackTrace();
			}
        }
	}
	
	private Bson nextDateFilter(LocalDateTime nextDate) {
		Bson gteFilter = Filters.gte("Data", Utils.standardFormat(lastDate));
		Bson ltFilter = Filters.lt("Data", Utils.standardFormat(nextDate));
		return Filters.and(gteFilter, ltFilter);
	}
	
	private boolean produceAndSendMedianOfNext(int seconds) throws MqttException {
		LocalDateTime nextDate = lastDate.plusSeconds(seconds);
		
		if(nextDate.isAfter(LocalDateTime.now()))
			return false;
		
		Bson filter = nextDateFilter(nextDate);
		lastDate = nextDate;
		
		FindIterable<Document> localDocuments = localCollection.find();
		localDocuments.filter(filter).sort(Sorts.ascending("Data"));
		List<Double> medicoes = new ArrayList<Double>();
		Document lastDocument = null;
		for(Document d : localDocuments){
			medicoes.add(Double.parseDouble(d.getString("Medicao")));
			lastDocument = d;
		}
		if(lastDocument == null)
			return true;
		
		double median = medianOf(medicoes);
		lastDocument.replace("Medicao", median);
		
		Main.gui.addData("sender:" + lastDocument + "\n");
		payload = SerializationUtils.serialize(lastDocument);
		call();
		
		return true;
	}
	
	private double medianOf(List<Double> vals) {
		Collections.sort(vals);
		if(vals.size() % 2 == 1)
			return vals.get(vals.size() / 2);
		return (vals.get(vals.size() / 2) + vals.get(vals.size() / 2 - 1)) / 2;
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
