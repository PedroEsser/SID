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
	
	public void select(String strSelect) {
		try {
			ResultSet rset = statement.executeQuery(strSelect);
			while(rset.next()) {
				System.out.println(rset.getString("nome"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void insert(String sqlInsert) {
		try {
	        statement.executeUpdate(sqlInsert);
	        System.out.println("The SQL statement is: " + sqlInsert + "\n");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
}
