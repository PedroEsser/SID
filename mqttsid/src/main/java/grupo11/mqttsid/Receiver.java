package grupo11.mqttsid;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Map;

import org.apache.commons.lang3.SerializationUtils;
import org.bson.Document;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;

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
			    
			    if(sensorsRange.get(aux.getString("Sensor")).isOutOfBounds(aux.getDouble("Medicao")))
					insertBrokenSensorAlert(aux);
				else
					checkCultureLimits(aux);
			    
			    Main.gui.addData("receiver:" + aux + "\n");
			    sqlmanager.updateDB("insert into medicao(zona, sensor, hora, leitura) values ('" + aux.get("Zona") + "','" + aux.get("Sensor") + "','" + aux.get("Data") + "','" + aux.get("Medicao") + "')");
			});
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}
	
	private void checkCultureLimits(Document aux) {
		ResultSet cultures = sqlmanager.queryDB("select * from parametrocultura where idzona = " + aux.getString("Zona").charAt(1));
		try {
			double measure = aux.getDouble("Medicao");
			while(!cultures.isClosed() && cultures.next()) {
				switch(aux.getString("Sensor").charAt(0)) {
					case 'T':
						insertLimitAlert(aux, cultures.getString(1), getIntervalPercentage(cultures.getInt(3), cultures.getInt(4), measure));
						break;
					case 'H':
						insertLimitAlert(aux, cultures.getString(1), getIntervalPercentage(cultures.getInt(5), cultures.getInt(6), measure));
						break;
					case 'L':
						insertLimitAlert(aux, cultures.getString(1), getIntervalPercentage(cultures.getInt(7), cultures.getInt(8), measure));
						break;
				}
			}
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}

	private void insertBrokenSensorAlert(Document aux) {
		ResultSet cultures = sqlmanager.queryDB("select * from parametrocultura where idzona = " + aux.getString("Zona").charAt(1));
		try {
			while(!cultures.isClosed() && cultures.next()) {
				insertAlert(aux, cultures.getString(1), "1", "Sensor Estragado");
			}
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void insertLimitAlert(Document aux, String culture, double percentage) {
		if(percentage < 0) {
			insertAlert(aux, culture, "2", "[Muito Grave] Limite Inferior da Cultura Excedido");
		} else if(percentage > 1) {
			insertAlert(aux, culture, "3", "[Muito Grave] Limite Superior da Cultura Excedido");
		} else if(percentage < 0.05) {
			insertAlert(aux, culture, "4", "[Grave] Aproximação ao Limite Inferior da Cultura");
		} else if(percentage < 0.15) {
			insertAlert(aux, culture, "5", "[Médio] Aproximação ao Limite Inferior da Cultura");
		} else if(percentage > 0.95) {
			insertAlert(aux, culture, "6", "[Grave] Aproximação ao Limite Superior da Cultura");
		} else if(percentage > 0.85) {
			insertAlert(aux, culture, "7", "[Médio] Aproximação ao Limite Superior da Cultura");
		}
	}
	
	private void insertAlert(Document aux, String culture, String type, String desc) {
		try {
			if(!hasBeenRecentlyAlerted(culture, type))
				sqlmanager.updateDB("insert into alerta(zona, sensor, hora, leitura, tipo, mensagem, idcultura, horaescrita) values ('" + 
					aux.get("Zona") + "','" + aux.get("Sensor") + "','" + aux.get("Data") + "','" + aux.get("Medicao") + "','" + type + 
					"','" + desc + "','" + culture + "','" + Utils.standardFormat(LocalDateTime.now()) + "')");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
		
	private double getIntervalPercentage(int min, int max, double measure) {
		return (double) ((measure-min)/(max-min));
	}
	
	private boolean hasBeenRecentlyAlerted(String culture, String type) throws SQLException {
		ResultSet lastAlert = sqlmanager.queryDB("select horaescrita from alerta where idcultura = " + culture + " and tipo = " + type + " order by idalerta desc limit 1");
		if(!lastAlert.next()) {
			return false;
		}
		return LocalDateTime.parse(lastAlert.getString(1), Utils.STANDARD_DATE_FORMAT).isAfter(LocalDateTime.now().minusMinutes(5)); 
	}

	public IMqttClient getSubscriber() {
		return subscriber;
	}
	
}
