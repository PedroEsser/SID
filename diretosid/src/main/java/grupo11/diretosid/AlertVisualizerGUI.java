package grupo11.diretosid;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;


public class AlertVisualizerGUI {

	public static AlertVisualizerGUI gui;
	public static AlertManager[] AlertManagers;

	private Runnable r;
	private JTextArea console;
	private JScrollPane scroll;
	private int count = 0;

	public AlertVisualizerGUI(Runnable r) {
		this.r = r;
		create();
	}

	private void create() {
		JFrame window = new JFrame("Alert Management");
		JPanel panel = new JPanel(new BorderLayout());
		console = new JTextArea();
		console.setEditable(false);
		scroll = new JScrollPane(console);
		JButton start = new JButton("Start");
		panel.add(BorderLayout.CENTER, scroll);
		panel.add(BorderLayout.SOUTH, start);
		start.addActionListener(e -> {
			if (start.getText().equals("Start")) {
				r.run();
				start.setText("Stop");
			} else {
				for (AlertManager s : AlertVisualizerGUI.AlertManagers) {
					s.interrupt();
				}
				start.setText("Start");
			}
		});
		window.add(panel);
		window.setSize(800, 400);
		window.setVisible(true);
		window.setLocationRelativeTo(null);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public synchronized void addData(String data) {
		console.append(data);
		JScrollBar vertical = scroll.getVerticalScrollBar();
		vertical.setValue(vertical.getMaximum());
		if (++count > 100) {
			try {
				int end = console.getLineEndOffset(0);
				console.replaceRange("", 0, end);
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		final SQLHandler sqlmanager = new SQLHandler("jdbc:mysql://localhost:3306/projetosid", "root", "");
		final String ourURI = "mongodb://localhost:25017,localhost:24017,localhost:23017/?replicaSet=projetosid&readPreference=primary&appname=MongoDB%20Compass&ssl=false";
		MongoClient ourMongoClient = MongoClients.create(ourURI);
		MongoDatabase ourMongoDB = ourMongoClient.getDatabase("sensors");

		Runnable r = () -> {

			AlertManagers = new AlertManager[6];
			AlertManagers[0] = new AlertManager(sqlmanager, "t1");
			AlertManagers[1] = new AlertManager(sqlmanager, "h1");
			AlertManagers[2] = new AlertManager(sqlmanager, "l1");
			AlertManagers[3] = new AlertManager(sqlmanager, "t2");
			AlertManagers[4] = new AlertManager(sqlmanager, "h2");
			AlertManagers[5] = new AlertManager(sqlmanager, "l2");

			for (AlertManager sender : AlertManagers)
				sender.start();
		};
		gui = new AlertVisualizerGUI(r);

	}
}
