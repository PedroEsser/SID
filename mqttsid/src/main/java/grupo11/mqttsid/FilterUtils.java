package grupo11.mqttsid;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class FilterUtils {
	
	public static Map<String,Range> getRangeFilter() {
		
		SQLHandler handler = new SQLHandler("jdbc:mysql://194.210.86.10:3306/sid2021", "aluno", "aluno");
    	ResultSet result = handler.queryDB("SELECT * FROM sensor");
    	
    	Map<String,Range> aux = new HashMap<>();
    	
		try {
			
			while(result.next()) {
				aux.put(result.getString("tipo") + result.getString("idsensor"), new Range(result.getDouble("limiteinferior"), result.getDouble("limitesuperior")));
			}
			return aux;
			
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
}
