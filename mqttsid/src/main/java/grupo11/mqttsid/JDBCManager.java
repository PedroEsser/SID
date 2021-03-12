package grupo11.mqttsid;

import java.sql.*;

public class JDBCManager {
	
	private Connection connection;
	private Statement statement;

	// "jdbc:mysql://localhost:3306/projetosid", "root", ""
	public JDBCManager(String... connectionInfo) {
		try {
			this.connection = DriverManager.getConnection(connectionInfo[0], connectionInfo[1], connectionInfo[2]);
			this.statement = connection.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void queryDB(String strSelect) {
		try {
			ResultSet rset = statement.executeQuery(strSelect);
			while(rset.next()) {
				System.out.println("(Query) Statement: " + rset.getString("nome") + "\n");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void updateDB(String strUpdate) {
		try {
	        statement.executeUpdate(strUpdate);
	        System.out.println("(Insert) Statement: " + strUpdate + "\n");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		JDBCManager m = new JDBCManager();
		m.queryDB("SELECT * FROM utilizador");
	}
	
}
