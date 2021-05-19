package grupo11.diretosid;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class Main {

	public static GUI gui;
	public static Sender[] senders;

	public static void main(String[] args) {

		final SQLHandler sqlmanager = new SQLHandler("jdbc:mysql://localhost:3306/projetosid", "root", "");
		final String ourURI = "mongodb://localhost:25017,localhost:24017,localhost:23017/?replicaSet=projetosid&readPreference=primary&appname=MongoDB%20Compass&ssl=false";
		MongoClient ourMongoClient = MongoClients.create(ourURI);
		MongoDatabase ourMongoDB = ourMongoClient.getDatabase("sensors");

		Runnable r = () -> {

			senders = new Sender[6];
			senders[0] = new Sender(ourMongoDB, "t1", sqlmanager);
			senders[1] = new Sender(ourMongoDB, "h1", sqlmanager);
			senders[2] = new Sender(ourMongoDB, "l1", sqlmanager);
			senders[3] = new Sender(ourMongoDB, "t2", sqlmanager);
			senders[4] = new Sender(ourMongoDB, "h2", sqlmanager);
			senders[5] = new Sender(ourMongoDB, "l2", sqlmanager);
			
			for (Sender sender : senders)
				sender.start();
		};
		gui = new GUI(r);

	}
}
