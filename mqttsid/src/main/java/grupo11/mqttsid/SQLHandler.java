package grupo11.mqttsid;

import java.sql.*;

public class SQLHandler {
	
	private Connection connection;
	private Statement statement;

	public SQLHandler(String... connectionInfo) {
		try {
			this.connection = DriverManager.getConnection(connectionInfo[0], connectionInfo[1], connectionInfo[2]);
			this.statement = connection.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public ResultSet queryDB(String strQuery) {
		try {
			return statement.executeQuery(strQuery);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void updateDB(String strUpdate) {
		try {
	        statement.executeUpdate(strUpdate);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
}
