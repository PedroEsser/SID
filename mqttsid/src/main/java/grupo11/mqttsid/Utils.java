package grupo11.mqttsid;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class Utils {
	
	public static final DateTimeFormatter STANDARD_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	
	public static Map<String,Range> getRangeFilter() {
		
		SQLHandler handler = new SQLHandler("jdbc:mysql://localhost:3306/projetosid", "root", "");
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
	
	public static String standardFormat(LocalDateTime date) {
		return STANDARD_DATE_FORMAT.format(date);
	}
	
}
