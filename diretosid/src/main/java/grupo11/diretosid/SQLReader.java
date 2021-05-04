package grupo11.diretosid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SQLReader extends Thread {

	private String sensor;
	private SQLHandler sqlmanager;
	private Map<String, Range> sensorsRange;
	private ArrayList<Culture> cultures;

	public SQLReader(SQLHandler sqlmanager, String sensor) {
		this.sensor = sensor;
		this.sqlmanager = sqlmanager;
		this.sensorsRange = Utils.getSensorRangeFilter();
		this.cultures = Utils.getCulturesRangeFilter("" + sensor.charAt(1), "" + sensor.charAt(0));
	}

	public void run() {
		while (!interrupted()) {
			int outOfSensor = 0;
			ResultSet medicoes = sqlmanager.queryDB("SELECT * FROM `medicao` WHERE " + "sensor = '" + sensor + "' ORDER BY hora DESC LIMIT 60");
			LinkedList<LinkedHashMap<String, String>> result = Utils.extractResultSet(medicoes);

			for (LinkedHashMap<String, String> rows : result) {
				if (sensorsRange.get(sensor).isOutOfBounds(Double.parseDouble(rows.get("leitura")))) {
					outOfSensor++;
				}
				for(Culture c: cultures) {
					c.checkMeasurement(rows.get("leitura"));
				}
			}
			
			if(outOfSensor >= 20) {
				//TODO Adicionar Alerta 8
			}
			
			for(Culture c: cultures) {
				if(c.checkNumbersofTime(30)) {
					//TODO Adicionar Alerta de 1 a 6 maybe
				}
			}
		}
	}

	public static void main(String[] args) {
		SQLHandler sqlmanager = new SQLHandler("jdbc:mysql://localhost:3306/gp13_implementacao", "root", "");
		new SQLReader(sqlmanager, "T1");
	}
}