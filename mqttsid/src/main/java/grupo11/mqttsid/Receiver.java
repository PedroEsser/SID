package grupo11.mqttsid;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.SerializationUtils;
import org.bson.Document;
import org.eclipse.paho.client.mqttv3.IMqttClient;

public class Receiver {

	IMqttClient subscriber;
	
	public Receiver(IMqttClient subscriber) {
		this.subscriber = subscriber;
		serve();
	}
	
	public void serve() {
		CountDownLatch receivedSignal = new CountDownLatch(10);
		try {
			subscriber.subscribe(Sender.TOPIC, (topic, msg) -> {
			    byte[] payload = msg.getPayload();
			    Document aux = SerializationUtils.deserialize(payload);
			    System.out.println("receiver:" + aux);
			    receivedSignal.countDown();
			});
			receivedSignal.await(1, TimeUnit.MINUTES);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
