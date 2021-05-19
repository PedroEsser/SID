package grupo11.projetosid;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class GUI {
	
//	public static final String PROF_URI = "mongodb://aluno:aluno@194.210.86.10:27017/?authSource=admin&readPreference=primary&appname=MongoDB%20Compass&ssl=false";
	public static final String PROF_URI = "mongodb://localhost:27017/?readPreference=primary&appname=MongoDB%20Compass&ssl=false";
	public static final String OUR_URI = "mongodb://localhost:25017,localhost:24017,localhost:23017/?replicaSet=projetosid&readPreference=primary&appname=MongoDB%20Compass&ssl=false";
	public static GUI gui;
	private static SensorDataWriter[] dataWriters;
	private static boolean delete;
	
	private Runnable r;
	private JTextArea console;
	private JScrollPane scroll;
	private int count = 0;

	public GUI(Runnable r) {
		this.r = r;
		create();
	}
	
	private void create() {
		JFrame window = new JFrame("Mongo To Mongo");
		JPanel panel = new JPanel(new BorderLayout()); 
		console = new JTextArea();
		console.setEditable(false);
		scroll = new JScrollPane(console);
        JButton start = new JButton("Start"); 
        panel.add(BorderLayout.CENTER, scroll);
        panel.add(BorderLayout.SOUTH, start);
        start.addActionListener(e-> {
        	if(start.getText().equals("Start")) {
        		int answer = JOptionPane.showConfirmDialog(null, "Do you want to clear the local mongodb database?", "Database Clear", JOptionPane.YES_NO_OPTION);
        		delete = answer == 0;
        		r.run();
        		start.setText("Stop");
        	} else {
        		for(SensorDataWriter dw: dataWriters) {
        			dw.interrupt();
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
		if(++count > 100) {
			try {
				int end = console.getLineEndOffset(0);
				console.replaceRange("", 0, end);
			} catch (BadLocationException e) {}
		}
	}
	
    public static void main(String[] args) {
    
    	MongoClient profMongoClient = MongoClients.create(PROF_URI);
        MongoDatabase profMongoDB = profMongoClient.getDatabase("sensors");
        
        MongoClient ourMongoClient = MongoClients.create(OUR_URI);
        MongoDatabase ourMongoDB = ourMongoClient.getDatabase("sensors");
        
        Runnable r = () -> {
        	dataWriters = new SensorDataWriter[6];
            dataWriters[0] = new SensorDataWriter("t1", profMongoDB, ourMongoDB, delete);
            dataWriters[1] = new SensorDataWriter("h1", profMongoDB, ourMongoDB, delete);
            dataWriters[2] = new SensorDataWriter("l1", profMongoDB, ourMongoDB, delete);
            dataWriters[3] = new SensorDataWriter("t2", profMongoDB, ourMongoDB, delete);
          	dataWriters[4] = new SensorDataWriter("h2", profMongoDB, ourMongoDB, delete);
          	dataWriters[5] = new SensorDataWriter("l2", profMongoDB, ourMongoDB, delete);
            
        	for(SensorDataWriter writer : dataWriters) {
            	writer.start();
        	}
		};
		
        gui = new GUI(r);

    }
	
}
