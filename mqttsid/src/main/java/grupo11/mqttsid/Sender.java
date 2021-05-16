package grupo11.mqttsid;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.SerializationUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;

import com.mongodb.MongoInterruptedException;
import com.mongodb.MongoTimeoutException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;

public class Sender extends Thread implements Callable<Void> {
	
	public static final String TOPIC = "sid_g11";
	public static final int MARGIN = 5;
	public static final int TIME_STEP = 2;
	
	private String sensor;
	private MongoCollection<Document> localCollection;
	
	private IMqttClient publisher;
	private byte[] payload;
	
	private LocalDateTime lastDate;
	
	public Sender(IMqttClient publisher, String sensor, MongoDatabase localDB) {
        this.publisher = publisher;
        this.sensor = sensor;
        this.localCollection = localDB.getCollection("sensor" + sensor);
        this.lastDate = LocalDateTime.now().minusMinutes(MARGIN);
    }
	
	public void run() {
		try {
			sendMeasuresUntilNow();
			while(true) {
				boolean success = sendMeasuresUntilDate(LocalDateTime.now());
				if(success) 
					lastDate = LocalDateTime.now();
				sleep(TIME_STEP * 1000);
			}
		} catch (MongoTimeoutException e) {
			restartMongo();
		} catch (InterruptedException | MongoInterruptedException | Error e) {}
	}
	
	private void sendMeasuresUntilNow() {
		LocalDateTime currentDate = LocalDateTime.now();
		List<Document> allDocs = getMeasuresUntilDate(currentDate);
		upToDate(allDocs);
		
		lastDate = currentDate;
		LocalDateTime nextDate = lastDate.plusSeconds(TIME_STEP);
		while(nextDate.isBefore(LocalDateTime.now())) {
			sendMeasuresUntilDate(nextDate);
			lastDate = nextDate;
			nextDate = lastDate.plusSeconds(TIME_STEP);
		};
	}
	
	private void upToDate(List<Document> docs) {
		LocalDateTime nextDate = lastDate.plusSeconds(TIME_STEP);
		List<Document> aux = new LinkedList<Document>();
		for(int i = 0 ; i < docs.size() ; i++) {
			Document current = docs.get(i);
			if(Utils.stringToDate(current.getString("Data")).isAfter(nextDate)) {
				if(!aux.isEmpty()) {
					makeMedianAndSend(aux, nextDate);
				}
				aux.clear();
				nextDate = nextDate.plusSeconds(TIME_STEP);
				i--;
			} else {
				aux.add(current);
			}
		}
	}
	
	private boolean sendMeasuresUntilDate(LocalDateTime nextDate) {
		List<Document> docs = getMeasuresUntilDate(nextDate);
		return makeMedianAndSend(docs, nextDate);
	}
	
	private List<Document> getMeasuresUntilDate(LocalDateTime nextDate){
		Bson gteFilter = Filters.gte("Data", Utils.standardFormat(lastDate));
		Bson ltFilter = Filters.lt("Data", Utils.standardFormat(nextDate));
		Bson filter = Filters.and(gteFilter, ltFilter);
		
		FindIterable<Document> localDocuments = localCollection.find(filter).sort(Sorts.ascending("Data"));
		List<Document> medicoes = new ArrayList<Document>();
		
		for(Document d : localDocuments)
			medicoes.add(d);
		
		return medicoes;
	}
	
	private boolean makeMedianAndSend(List<Document> docs, LocalDateTime nextDate) {
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
	
	private void sendDocument(Document d) {
		try {
			payload = SerializationUtils.serialize(d);
			call();
			SenderGUI.gui.addData("sender:" + d + "\n");
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Void call() throws MqttException {
		if (!publisher.isConnected()) {
            return null;
        }
        publisher.publish(TOPIC, payload, 2, false);
        return null;
	}
	
	public IMqttClient getPublisher() {
		return publisher;
	}
	
	private void restartMongo() {
		try {
			MongoClient ourMongoClient = MongoClients.create(SenderGUI.OUR_URI);
			MongoDatabase ourMongoDB = ourMongoClient.getDatabase("sensors");
			this.localCollection = ourMongoDB.getCollection("sensor" + sensor);
			sleep(1000);
			run();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
