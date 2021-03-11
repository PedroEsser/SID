package grupo11.mqttsid;

import java.sql.*;

public class JDBCManager {
	
	Connection connection;
	Statement statement;

	public JDBCManager() {
		try {
			this.connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/projetosid", "root", "");
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
	
}
