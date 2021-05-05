package grupo11.diretosid;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class Main {

	public static GUI gui;
	public static Sender[] senders;

	public static void main(String[] args) {

		final SQLHandler sqlmanager = new SQLHandler("jdbc:mysql://localhost:3306/gp13_implementacao", "root", "");
//		final SQLHandler sqlmanager2 = new SQLHandler("jdbc:mysql://localhost:3306/gp13_implementacao", "root", "");
		final String ourURI = "mongodb://localhost:25017,localhost:24017,localhost:23017/?replicaSet=projetosid&readPreference=primary&appname=MongoDB%20Compass&ssl=false";
		MongoClient ourMongoClient = MongoClients.create(ourURI);
		MongoDatabase ourMongoDB = ourMongoClient.getDatabase("sensors");

		Runnable r = () -> {

			senders = new Sender[1];
			senders[0] = new Sender(ourMongoDB, "t1", sqlmanager);
//				senders[1] = new Sender(sqlmanager, "h1", ourMongoDB);
//				senders[2] = new Sender(sqlmanager, "l1", ourMongoDB);
//				senders[3] = new Sender(sqlmanager, "h2", ourMongoDB);
//				senders[4] = new Sender(sqlmanager, "t2", ourMongoDB);
//				senders[5] = new Sender(sqlmanager, "l2", ourMongoDB);

			for (Sender sender : senders)
				sender.start();
			
			new AlertManager(sqlmanager,"t1").start();
		};
		gui = new GUI(r);

	}
}
