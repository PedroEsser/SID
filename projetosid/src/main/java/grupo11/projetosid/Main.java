package grupo11.projetosid;

import com.mongodb.client.*;

public class Main {

	public static GUI gui;
	public static SensorDataWriter[] dataWriters;
	
    public static void main(String[] args) {
    
    	final String profURI = "mongodb://aluno:aluno@194.210.86.10:27017/?authSource=admin&readPreference=primary&appname=MongoDB%20Compass&ssl=false";
        MongoClient profMongoClient = MongoClients.create(profURI);
        MongoDatabase profMongoDB = profMongoClient.getDatabase("sid2021");
        
        final String ourURI = "mongodb://localhost:25017,localhost:24017,localhost:23017/?replicaSet=projetosid&readPreference=primary&appname=MongoDB%20Compass&ssl=false";
        MongoClient ourMongoClient = MongoClients.create(ourURI);
        MongoDatabase ourMongoDB = ourMongoClient.getDatabase("sensors");
        
        Runnable r = () -> {
        	dataWriters = new SensorDataWriter[1];
            dataWriters[0] = new SensorDataWriter("t1", profMongoDB, ourMongoDB);
//          dataWriters[1] = new SensorDataWriter("h1", profMongoDB, ourMongoDB);
//          dataWriters[2] = new SensorDataWriter("l1", profMongoDB, ourMongoDB);
//          dataWriters[3] = new SensorDataWriter("t2", profMongoDB, ourMongoDB);
//          dataWriters[4] = new SensorDataWriter("h2", profMongoDB, ourMongoDB);
//          dataWriters[5] = new SensorDataWriter("l2", profMongoDB, ourMongoDB);
            
        	for(SensorDataWriter writer : dataWriters)
            	writer.start();
		};
		
        gui = new GUI(r);

    }
}
