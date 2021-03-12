package grupo11.mqttsid;

import java.time.format.DateTimeFormatter;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.SerializationUtils;
import org.bson.Document;
import org.eclipse.paho.client.mqttv3.IMqttClient;

public class Receiver {

	private IMqttClient subscriber;
	private JDBCManager sqlmanager;
	
	public Receiver(IMqttClient subscriber) {
		this.subscriber = subscriber;
		this.sqlmanager = new JDBCManager("jdbc:mysql://localhost:3306/projetosid", "root", "");
		serve();
	}
	
	public void serve() {
		CountDownLatch receivedSignal = new CountDownLatch(10);
		try {
			subscriber.subscribe(Sender.TOPIC, (topic, msg) -> {
			    byte[] payload = msg.getPayload();
			    Document aux = SerializationUtils.deserialize(payload);
			    System.out.println("receiver:" + aux);
			    sqlmanager.updateDB("insert into medicao(zona, sensor, hora, leitura) values ('" + aux.get("Zona") + "','" + aux.get("Sensor") + "','" + DateUtils.parse(aux.get("Data").toString()) + "','" + aux.get("Medicao") + "')");
			    receivedSignal.countDown();
			});
			receivedSignal.await(1, TimeUnit.MINUTES);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
