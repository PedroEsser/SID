package grupo11.diretosid;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Callable;

import org.eclipse.paho.client.mqttv3.MqttException;

import com.mongodb.MongoInterruptedException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Sorts;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import org.bson.Document;

public class Sender extends Thread {

	private MongoCollection<Document> localCollection;
	private MongoDatabase localDB;
	private SQLHandler sqlmanager;

	private LinkedList<String> lastMeasumentsTime;
	private int measurementsPerSecond;
	public final int DEFAULT_TIME = 1;

	public Sender(MongoDatabase localDB, String sensor, SQLHandler sqlmanager) {
			this.localCollection = localDB.getCollection("sensor" + sensor);
			this.sqlmanager = sqlmanager;
			this.localDB = localDB;

			this.lastMeasumentsTime = new LinkedList<>();
			this.measurementsPerSecond = DEFAULT_TIME;
	}

	public void run() {
		while (!interrupted()) {
			try {
				FindIterable<Document> localDocuments = localCollection.find();
				localDocuments.sort(Sorts.descending("Data"));
				if(localDocuments.first() == null) {
					continue;
				}
				ArrayList<Document> docs = new ArrayList<>();
				int aux = 0;
				for (Document d : localDocuments) {
					
					if (aux++ >= 4)
						break;
					docs.add(d);
					lastMeasumentsTime.add(d.get("Data").toString());
				}
				ArrayList<Document> res = checkDocuments(docs);
				produceAndSendNext(res);
				updateTime();
				sleep(measurementsPerSecond * 1000 * 3);
			} catch (InterruptedException | MongoInterruptedException | Error e) {
				interrupt();
			}
		}
	}

	private ArrayList<Document> checkDocuments(ArrayList<Document> dlist) {
		ArrayList<Document> res = new ArrayList<Document>();
		Double d1, d2, d3;
		for (int i = 1; i != dlist.size() - 1; i++) {
			Document d = dlist.get(i);
			d1 = Double.parseDouble(dlist.get(i - 1).get("Medicao").toString());
			d2 = Double.parseDouble(d.get("Medicao").toString());
			d3 = Double.parseDouble(dlist.get(i + 1).get("Medicao").toString());
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

	private synchronized void produceAndSendNext(ArrayList<Document> docs) {
		try {
			for (Document d : docs) {
				Double res = Double.parseDouble(String.format("%.2f", Double.parseDouble(d.get("Medicao").toString())).replace(",", "."));
				ResultSet cond = sqlmanager.queryDB("select count(idmedicao) from medicao where "
						+ "zona = '" + d.get("Zona").toString() + "' and "
						+ "sensor = '" + d.get("Sensor").toString() + "' and "
						+ "hora = '" + d.get("Data").toString() + "' and "
						+ "leitura = '" + res + "';");
				
				if (cond.next() && cond.getInt(1)==0) {
					Main.gui.addData("INSERTED: " + d.toString() + "\n");
					sqlmanager.updateDB("insert into medicao(zona, sensor, hora, leitura) " +
								"values ('" + d.get("Zona") + "','" + d.get("Sensor") + "','" + d.get("Data") + "','" + d.get("Medicao") + "')");
				}else {
					Main.gui.addData("DISCARDED: " + d.toString() + "\n");
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