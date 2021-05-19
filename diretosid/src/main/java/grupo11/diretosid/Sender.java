package grupo11.diretosid;

import java.util.ArrayList;
import java.util.LinkedList;

import com.mongodb.MongoInterruptedException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Sorts;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.bson.Document;

public class Sender extends Thread {
	
	private MongoCollection<Document> localCollection;
	private SQLHandler sqlmanager;

	private LinkedList<String> lastMeasumentsTime;
	private int measurementsPerSecond;

	public Sender(MongoDatabase localDB, String sensor, SQLHandler sqlmanager) {
		this.localCollection = localDB.getCollection("sensor" + sensor);
		this.sqlmanager = sqlmanager;

		this.lastMeasumentsTime = new LinkedList<>();
		this.measurementsPerSecond = 1;
	}

	public void run() {
		while (!interrupted()) {
			try {
				FindIterable<Document> localDocuments;

				synchronized (localCollection) {
					localDocuments = localCollection.find();
					localDocuments.sort(Sorts.descending("Data"));
				}

				if (localDocuments.first() == null) {
					continue;
				}
				ArrayList<Document> res = checkDocuments(getLastDocuments(localDocuments));
				produceAndSendNext(res);
				updateTime();
				sleep((1/measurementsPerSecond) * 3 * 1000);
			} catch (InterruptedException | MongoInterruptedException | Error e) {
				interrupt();
			}
		}
	}

	private ArrayList<Document> getLastDocuments(FindIterable<Document> localDocuments) {
		ArrayList<Document> docs = new ArrayList<>();
		int timer = 0;
		for (Document d : localDocuments) {
			if (timer++ >= 4)
				break;
			docs.add(d);
			lastMeasumentsTime.add(d.get("Data").toString());
		}
		return docs;
	}

	private ArrayList<Document> checkDocuments(ArrayList<Document> dlist) {
		ArrayList<Document> res = new ArrayList<Document>();
		for (int i = 1; i != dlist.size() - 1; i++) {
			Document d = dlist.get(i);
			Double d1 = Utils.convert(dlist.get(i - 1).get("Medicao").toString());
			Double d2 = Utils.convert(d.get("Medicao").toString());
			Double d3 = Utils.convert(dlist.get(i + 1).get("Medicao").toString());
			
			if (d2 - d1 > 5 && d3 - d2 < -5)
				res.add(fixDoc(d, d1, d3));
			else
				res.add(d);
		}
		return res;
	}

	private Document fixDoc(Document d, Double d1, Double d2) {
		Double aux = d1 + (d2 - d1) / 2;
		Document doc = new Document();
		for (String s : d.keySet()) {
			if (s.equals("Medicao"))
				doc.append(s, (Object) (aux));
			else
				doc.append(s, d.get(s));
		}
		return doc;
	}

	private void produceAndSendNext(ArrayList<Document> docs) {
		try {
			for (Document d : docs) {
				synchronized (sqlmanager) {

					Double res = Utils.convert(
							String.format("%.2f", Utils.convert(d.get("Medicao").toString())).replace(",", "."));
					ResultSet cond = sqlmanager.queryDB("select count(idmedicao) from medicao where " + "zona = '"
							+ d.get("Zona").toString() + "' and " + "sensor = '" + d.get("Sensor").toString() + "' and "
							+ "hora = '" + d.get("Data").toString() + "' and " + "leitura = '" + res + "';");

					if (!cond.isClosed() && cond.next()) {
						if (cond.getLong(1) == 0) {
							Main.gui.addData("INSERTED: " + d.toString() + "\n");
							sqlmanager.updateDB("insert into medicao(zona, sensor, hora, leitura) " + "values ('"
									+ d.get("Zona") + "','" + d.get("Sensor") + "','" + d.get("Data") + "','"
									+ d.get("Medicao") + "')");
						}
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void updateTime() {
		int numberOfMeasurements = 1;
		String firstMeasure = lastMeasumentsTime.pop();
		for (int i = 0; i != lastMeasumentsTime.size(); i++) {
			if (lastMeasumentsTime.get(i).equals(firstMeasure)) {
				numberOfMeasurements++;
			}
		}
		lastMeasumentsTime.clear();
		this.measurementsPerSecond = numberOfMeasurements;
	}
}