package grupo11.mqttsid;

import java.sql.*;

public class JDBCManager {

	public JDBCManager() {
		 try {
			Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/projetosid", "root", "");
			Statement stmt = conn.createStatement();
			String strSelect = "select nome from utilizador";
			ResultSet rset = stmt.executeQuery(strSelect);
			System.out.println("The records selected are:");
			int rowCount = 0;
			while(rset.next()) {
				System.out.println(rset.getString(1));
				++rowCount;
			}
			System.out.println("Total number of records = " + rowCount);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		 
	}
	
}
