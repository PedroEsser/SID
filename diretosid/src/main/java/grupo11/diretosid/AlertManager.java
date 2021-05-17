package grupo11.diretosid;

import java.util.Map;
import java.util.HashMap;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Map.Entry;

import java.util.LinkedList;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;

public class AlertManager extends Thread {

	private int repeating;
	private int amountOfEmptySet;
	public final int LIMIT = 5;

	private String zona;
	private String sensor;
	private String lastDate;
	private SQLHandler sqlmanager;
	private ArrayList<Culture> cultures;
	private Map<String, Alert> alertTypes;
	private Map<String, Range> sensorsRange;

	public AlertManager(SQLHandler sqlmanager, String sensor) {
		this.repeating = 0;
		this.amountOfEmptySet = 0;
		this.zona = "Z" + sensor.charAt(1);
		this.sensor = sensor.toUpperCase();
		this.lastDate = "";
		this.sqlmanager = sqlmanager;
		this.alertTypes = new LinkedHashMap<>();
		this.sensorsRange = Utils.getSensorRangeFilter();
		this.cultures = Utils.getCulturesRangeFilter("" + sensor.charAt(1), "" + sensor.charAt(0));
	}

	public void run() {
		while (!interrupted()) {
			try {
				LinkedList<LinkedHashMap<String, String>> result;
				String str = "SELECT * FROM medicao WHERE sensor = '" + sensor + lastDate
						+ "' ORDER BY hora DESC LIMIT 60";
				synchronized (sqlmanager) {
					ResultSet medicoes = sqlmanager.queryDB(str);
					result = Utils.extractResultSet(medicoes);
				}
				System.out.println(str + " && Size = " + result.size() + " && Timer = " + amountOfEmptySet + "\n");
				checkAlerts(result);
				repeating = result.size();
				checkInternetConnection();
				sleep(3000);
			} catch (InterruptedException | SQLException e) {
				interrupt();
			}
		}
	}

	private void checkAlerts(LinkedList<LinkedHashMap<String, String>> result) throws SQLException {
		if (result.size() >= 60) {
			amountOfEmptySet = 0;
			lastDate = "' AND hora > '" + result.getFirst().get("hora");
			insertSensorAlert(result);
			checkCultureAlerts(result);

		} else if (amountOfEmptySet >= LIMIT) {
			amountOfEmptySet = 0;
			synchronized (sqlmanager) {
				ResultSet cond = sqlmanager.queryDB("SELECT count(*) FROM alerta WHERE tipo = '7' AND sensor = '"
						+ sensor + "' AND horaescrita = (SELECT Max(horaescrita) FROM alerta WHERE sensor = '" + sensor
						+ "')");
				if (cond.next() && cond.getInt(1) == 0) {
					insertCostumAlert(new Alert(zona, sensor, Utils.standardFormat(LocalDateTime.now()), "0.0", "7"),
							"Comunicação com sensor " + sensor + " Perdida");
				}
			}

		} else if (result.size() == repeating) {
			amountOfEmptySet++;
		}
	}

	private void checkInternetConnection() throws SQLException {
		try {
			URL url = new URL("http://www.google.com");
			URLConnection connection = url.openConnection();
			connection.connect();
		} catch (IOException e) {
			synchronized (sqlmanager) {
				ResultSet cond = sqlmanager.queryDB(
						"SELECT count(*) FROM alerta WHERE tipo = '9' AND horaescrita = (SELECT Max(horaescrita) FROM alerta)");
				if (cond.next() && cond.getInt(1) == 0) {
					insertCostumAlert(new Alert(zona, sensor, Utils.standardFormat(LocalDateTime.now()), "0.0", "9"),
							"Perda da Comunicação geral (Falha de internet ou Falha de energia entre outros)");
				}
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
			insertCostumAlert(alertTypes.get("8"), "Sensor Estragado");
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
						alertTypes.put("1", new Alert(zona, sensor, rows.get("hora"), rows.get("leitura"), "1",
								c.getID(), "[Leve] Aproximação ao Limite Superior da Cultura"));
					}

				} else if (c.inPercentil(rows.get("leitura"), new Range(0.95, 1.00))) {
					counters.replace("2", counters.get("2") + 1);
					if (counters.get("2") > 30) {
						alertTypes.put("2", new Alert(zona, sensor, rows.get("hora"), rows.get("leitura"), "2",
								c.getID(), "[Medio] Aproximação ao Limite Superior da Cultura"));
					}

				} else if (c.getLimits().isOutOfUpperBounds(Utils.convert(rows.get("leitura")))) {
					counters.replace("3", counters.get("3") + 1);
					if (counters.get("3") > 30) {
						alertTypes.put("3", new Alert(zona, sensor, rows.get("hora"), rows.get("leitura"), "3",
								c.getID(), "[Grave] Limite Superior da Cultura Excedido"));
					}

				} else if (c.inPercentil(rows.get("leitura"), new Range(0.05, 0.15))) {
					counters.replace("4", counters.get("4") + 1);
					if (counters.get("4") > 30) {
						alertTypes.put("4", new Alert(zona, sensor, rows.get("hora"), rows.get("leitura"), "4",
								c.getID(), "[Leve] Aproximação ao Limite Inferior da Cultura"));
					}

				} else if (c.inPercentil(rows.get("leitura"), new Range(0.00, 0.05))) {
					counters.replace("5", counters.get("5") + 1);
					if (counters.get("5") > 30) {
						alertTypes.put("5", new Alert(zona, sensor, rows.get("hora"), rows.get("leitura"), "5",
								c.getID(), "[Medio] Aproximação ao Limite Inferior da Cultura"));
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

	private void insertCostumAlert(Alert a, String msg) {
		LinkedList<LinkedHashMap<String, String>> results;
		synchronized (sqlmanager) {
			ResultSet cultures = sqlmanager
					.queryDB("select * from parametrocultura where idzona = " + sensor.charAt(1));
			results = Utils.extractResultSet(cultures);
		}
		results.forEach(row -> {
			a.setCultura(row.get("idcultura"));
			a.setMensagem(msg);
			insertAlert(a);
		});

	}

	private void insertAlert(Alert al) {
		synchronized (sqlmanager) {
			try {
				ResultSet state = sqlmanager.queryDB("select estado from cultura where idcultura = " + al.getCultura());
				if (state.next() && state.getInt(1) != 0) {
					AlertVisualizerGUI.gui
							.addData("ALERT: Zona - " + al.getZona() + ", Sensor - " + al.getSensor() + ", Hora: "
									+ al.getHora() + ", Leitura: " + al.getLeitura() + ", Tipo:" + al.getTipo() + "\n");
					sqlmanager.updateDB(
							"insert into alerta(zona, sensor, hora, leitura, tipo, mensagem, idcultura, horaescrita) "
									+ "values ('" + al.getZona() + "','" + al.getSensor() + "','" + al.getHora() + "','"
									+ al.getLeitura() + "','" + al.getTipo() + "','" + al.getMensagem() + "','"
									+ al.getCultura() + "','" + Utils.standardFormat(LocalDateTime.now()) + "')");
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public boolean checkValue(double medicao, double min, double max) {
		return medicao < max && medicao > min;
	}

	public boolean checkInvalid(String sensor, double medicao) {
		return sensorsRange.get("sensor").isOutOfBounds(medicao);
	}
}