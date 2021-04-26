package grupo11.mqttsid;

import java.util.Map;

import org.apache.commons.lang3.SerializationUtils;
import org.bson.Document;
import org.eclipse.paho.client.mqttv3.IMqttClient;

public class Receiver {

	private IMqttClient subscriber;
	private SQLHandler sqlmanager;
	private Map<String,Range> sensorsRange;
	
	public Receiver(IMqttClient subscriber) {
		this.subscriber = subscriber;
		this.sqlmanager = new SQLHandler("jdbc:mysql://localhost:3306/projetosid", "root", "");
		this.sqlmanager.updateDB("delete from medicao");
		this.sensorsRange = Utils.getRangeFilter();
		serve();
	}
	
	public void serve() {
		try {
			subscriber.subscribe(Sender.TOPIC, 2, (topic, msg) -> {
			    byte[] payload = msg.getPayload();
			    Document aux = SerializationUtils.deserialize(payload);
			    if(sensorsRange.get(aux.getString("Sensor")).isOutOfBounds(aux.getDouble("Medicao"))) {
//			    	inserir alerta respetivo
//			    	sqlmanager.updateDB("");
			    }
			    Main.gui.addData("receiver:" + aux + "\n");
			    sqlmanager.updateDB("insert into medicao(zona, sensor, hora, leitura) values ('" + aux.get("Zona") + "','" + aux.get("Sensor") + "','" + aux.get("Data") + "','" + aux.get("Medicao") + "')");
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public IMqttClient getSubscriber() {
		return subscriber;
	}
	
}
