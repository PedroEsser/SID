package grupo11.diretosid;

import java.util.Map;
import java.util.HashMap;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.LinkedList;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;

public class AlertManager extends Thread {

	private String zona;
	private String sensor;
	private String lastDate;
	private SQLHandler sqlmanager;
	private ArrayList<Culture> cultures;
	private Map<String, Alert> alertTypes;
	private Map<String, Range> sensorsRange;

	public AlertManager(SQLHandler sqlmanager, String sensor) {
		this.zona = "Z" + sensor.charAt(1);
		this.sensor = sensor;
		this.lastDate = "";
		this.sqlmanager = sqlmanager;
		this.alertTypes = new LinkedHashMap<>();
		this.sensorsRange = Utils.getSensorRangeFilter();
		this.cultures = Utils.getCulturesRangeFilter("" + sensor.charAt(1), "" + sensor.charAt(0));
	}

	public void run() {
		while (!interrupted()) {
			LinkedList<LinkedHashMap<String, String>> result;

			synchronized (sqlmanager) {
				ResultSet medicoes = sqlmanager.queryDB("SELECT * FROM medicao WHERE sensor = '" + sensor+ lastDate + "' ORDER BY hora DESC LIMIT 60");
				result = Utils.extractResultSet(medicoes);
			}

			if(result.size() >= 60) {
				insertSensorAlert(result);
				checkCultureAlerts(result);
				lastDate = "' AND hora > '" + result.getLast().get("hora");
			}
		}
	}

	public void insertSensorAlert(LinkedList<LinkedHashMap<String, String>> result) {
		int outOfSensor = 0;
		for (LinkedHashMap<String, String> rows : result) {
			if (sensorsRange.get(sensor).isOutOfBounds(Utils.convert(rows.get("leitura")))) {
				if (++outOfSensor >= 20) {
					alertTypes.put("8", new Alert(zona, sensor, rows.get("hora"), rows.get("leitura"), "8"));
				}
			}
		}

		if (outOfSensor >= 20) {
			insertBrokenSensorAlert(alertTypes.get("8"));
		}
	}

	public void checkCultureAlerts(LinkedList<LinkedHashMap<String, String>> result) {
		for (Culture c : cultures) {
			Map<String, Integer> counters = new HashMap<>();
			counters.put("1", 0);
			counters.put("2", 0);
			counters.put("3", 0);
			counters.put("4", 0);
			counters.put("5", 0);
			counters.put("6", 0);
			for (LinkedHashMap<String, String> rows : result) {

				if (c.inPercentil(rows.get("leitura"), new Range(0.85, 0.95))) {
					counters.replace("1", counters.get("1") + 1);
					if (counters.get("1") > 30) {
						alertTypes.put("1", new Alert(zona, sensor, rows.get("hora"), rows.get("leitura"), "1",c.getID(), "[Leve] Aproximação ao Limite Superior da Cultura"));
					}

				} else if (c.inPercentil(rows.get("leitura"), new Range(0.95, 1.00))) {
					counters.replace("2", counters.get("2") + 1);
					if (counters.get("2") > 30) {
						alertTypes.put("2", new Alert(zona, sensor, rows.get("hora"), rows.get("leitura"), "2",c.getID(), "[Medio] Aproximação ao Limite Superior da Cultura"));
					}

				} else if (c.getLimits().isOutOfUpperBounds(Utils.convert(rows.get("leitura")))) {
					counters.replace("3", counters.get("3") + 1);
					if (counters.get("3") > 30) {
						alertTypes.put("3", new Alert(zona, sensor, rows.get("hora"), rows.get("leitura"), "3",c.getID(), "[Grave] Limite Superior da Cultura Excedido"));
					}

				} else if (c.inPercentil(rows.get("leitura"), new Range(0.05, 0.15))) {
					counters.replace("4", counters.get("4") + 1);
					if (counters.get("4") > 30) {
						alertTypes.put("4", new Alert(zona, sensor, rows.get("hora"), rows.get("leitura"), "4",c.getID(), "[Leve] Aproximação ao Limite Inferior da Cultura"));
					}

				} else if (c.inPercentil(rows.get("leitura"), new Range(0.00, 0.05))) {
					counters.replace("5", counters.get("5") + 1);
					if (counters.get("5") > 30) {
						alertTypes.put("5", new Alert(zona, sensor, rows.get("hora"), rows.get("leitura"), "5",c.getID(), "[Medio] Aproximação ao Limite Inferior da Cultura"));
					}

				} else if (c.getLimits().isOutOfLowerBounds(Utils.convert(rows.get("leitura")))) {
					counters.replace("6", counters.get("6") + 1);
					if (counters.get("6") > 30) {
						alertTypes.put("6", new Alert(zona, sensor, rows.get("hora"), rows.get("leitura"), "6",
								c.getID(), "[Grave] Limite Inferior da Cultura Excedido"));
					}
				}
			}
			insertCultureAlerts(counters);
		}
	}

	private void insertCultureAlerts(Map<String, Integer> counters) {
		for (Entry<String, Integer> i : counters.entrySet()) {
			if (i.getValue() >= 30) {
				insertAlert(alertTypes.get(i.getKey()));
			}
		}
	}

	private void insertBrokenSensorAlert(Alert a) {
		ResultSet cultures = sqlmanager.queryDB("select * from parametrocultura where idzona = " + sensor.charAt(1));
		LinkedList<LinkedHashMap<String, String>> results = Utils.extractResultSet(cultures);
		results.forEach(row -> {
			a.setCultura(row.get("idcultura"));
			a.setMensagem("Sensor Estragado");
			insertAlert(a);
		});
	}

	private void insertAlert(Alert al) {
		try {
			ResultSet state = sqlmanager.queryDB("select estado from cultura where idcultura = " + al.getCultura());
			state.next();
			if (state.getInt(1) != 0) {
				sqlmanager.updateDB("insert into alerta(zona, sensor, hora, leitura, tipo, mensagem, idcultura, horaescrita) "
						+ "values ('" + al.getZona() + "','" + al.getSensor() + "','" + al.getHora() + "','" 
						+ al.getLeitura() + "','" + al.getTipo() + "','" + al.getMensagem() + "','"
						+ al.getCultura() + "','" + Utils.standardFormat(LocalDateTime.now()) + "')");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		SQLHandler sqlmanager = new SQLHandler("jdbc:mysql://localhost:3306/gp13_implementacao", "root", "");
		new AlertManager(sqlmanager, "T1");
	}
}