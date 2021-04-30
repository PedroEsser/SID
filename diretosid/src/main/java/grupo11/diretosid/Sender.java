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

	private File insertedDocs;

	private LinkedList<String> lastMeasumentsTime;
	private int measurementsPerSecond;
	public final int DEFAULT_TIME = 1;

	public Sender(MongoDatabase localDB, String sensor, SQLHandler sqlmanager) {
		try {
			this.localCollection = localDB.getCollection("sensor" + sensor);
			this.sqlmanager = sqlmanager;
			this.localDB = localDB;


			this.insertedDocs = new File("insertedDocs.csv");
			this.insertedDocs.createNewFile();
			
			this.lastMeasumentsTime = new LinkedList<>();
			this.measurementsPerSecond = DEFAULT_TIME;
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		while (!interrupted()) {
			try {
				FindIterable<Document> localDocuments = localCollection.find();
				localDocuments.sort(Sorts.descending("Data"));
				ArrayList<Document> docs = new ArrayList<>();
				int aux = 0;
				for (Document d : localDocuments) {
					if (aux++ >= 4)
						break;
					docs.add(d);
				}
				ArrayList<Document> res = checkDocuments(docs);
				produceAndSendNext(res);
				Main.gui.addData("Vou Dormir por " + measurementsPerSecond * 1000 * 3 + " segundos\n");
				sleep(measurementsPerSecond * 1000 * 3);
			} catch (InterruptedException | MongoInterruptedException | Error e) {
				interrupt();
			}
		}
	}
//	MongoInterruptedException

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
		ArrayList<String> content = getFromFile(insertedDocs);
		for (Document d : docs) {
			if (!containsInList(content, d.get("_id").toString())) {
				writeToFile(d.get("_id").toString() + "\n", insertedDocs);
				sqlmanager.updateDB("insert into medicao(zona, sensor, hora, leitura) " + "values ('" + d.get("Zona")
						+ "','" + d.get("Sensor") + "','" + d.get("Data") + "','" + d.get("Medicao") + "')");
				Main.gui.addData(d.toString() + "\n");
				lastMeasumentsTime.add(d.get("Data").toString());
			}
		}
	}

	private void writeToFile(String data, File file) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
			writer.append(data);
			writer.close();
		} catch (IOException e) {
			System.err.println("File not Found");
		}
	}

	private ArrayList<String> getFromFile(File file) {
		ArrayList<String> content = new ArrayList<>();
		try {
			Scanner scan = new Scanner(file);
			while (scan.hasNext()) {
				content.add(scan.nextLine().trim());
			}
			scan.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return content;
	}

	private Boolean containsInList(ArrayList<String> content, String str) {
		for (String c : content) {
			if (c.equals(str)) {
				return true;
			}
		}
		return false;
	}

	private void updateTime() {
		int numberOfMeasurements = 0;
		String firstMeasure = lastMeasumentsTime.pop();
		for (int i = 0; i != lastMeasumentsTime.size(); i++) {
			if (lastMeasumentsTime.get(i).equals(firstMeasure)) {
				numberOfMeasurements++;
			}
		}
		this.measurementsPerSecond = numberOfMeasurements;
	}

}

/*
 * 
 * Double d1 = Double.parseDouble(d.get("Medicao").toString()); String str =
 * String.format("%.2f", d1); Double d2 = Double.parseDouble("14.08");
 * 
 * 
 */
