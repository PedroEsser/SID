package grupo11.projetosid;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

import com.mongodb.client.*;

public class App {

	public static boolean running = true;
	
    public static void main( String[] args ){
    
    	// "jdbc:mysql://localhost:3306/limites", "root", ""
    	SQLHandler handler = new SQLHandler("jdbc:mysql://localhost:3306/limites", "root", "");
    	ResultSet result = handler.queryDB("SELECT * FROM sensor");
    	
    	try {
    		result.next();
			System.out.println(result.getString(1));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	System.out.println(FilterUtils.getRangeFilter("T1"));
    	
    	
    	/*
    	final String profURI = "mongodb://aluno:aluno@194.210.86.10:27017/?authSource=admin&readPreference=primary&appname=MongoDB%20Compass&ssl=false";
        MongoClient profMongoClient = MongoClients.create(profURI);
        MongoDatabase profMongoDB = profMongoClient.getDatabase("sid2021");
        
        final String ourURI = "mongodb://localhost:25017,localhost:24017,localhost:23017/?replicaSet=projetosid&readPreference=primary&appname=MongoDB%20Compass&ssl=false";
        MongoClient ourMongoClient = MongoClients.create(ourURI);
        MongoDatabase ourMongoDB = ourMongoClient.getDatabase("sensors");
        
        SensorDataWriter[] dataWriters = new SensorDataWriter[6];
        dataWriters[0] = new SensorDataWriter("h1", profMongoDB, ourMongoDB);
        dataWriters[1] = new SensorDataWriter("t1", profMongoDB, ourMongoDB);
        dataWriters[2] = new SensorDataWriter("l1", profMongoDB, ourMongoDB);
        dataWriters[3] = new SensorDataWriter("h2", profMongoDB, ourMongoDB);
        dataWriters[4] = new SensorDataWriter("t2", profMongoDB, ourMongoDB);
        dataWriters[5] = new SensorDataWriter("l2", profMongoDB, ourMongoDB);
        
        for(SensorDataWriter writer : dataWriters)
        	writer.start();
        
        while(running) {
        	Scanner scan = new Scanner(System.in);
        	if(scan.next() == "exit")
        		running = false;
        }
        
        for(SensorDataWriter writer : dataWriters)
			try {
				writer.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        
        */

    }
}
