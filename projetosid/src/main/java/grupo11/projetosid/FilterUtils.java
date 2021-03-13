package grupo11.projetosid;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bson.conversions.Bson;

import com.mongodb.client.model.Filters;

public class FilterUtils{
	
	public static Bson getRangeFilter(String sensorType) {
		int idsensor = Integer.parseInt(sensorType.substring(1, 2));
		String tipo = sensorType.substring(0, 1);
		
		SQLHandler handler = new SQLHandler("jdbc:mysql://localhost:3306/sid2021", "root", "");
    	ResultSet result = handler.queryDB(""
    			+ "SELECT * FROM sensor"
    			+ " WHERE idsensor=" + idsensor
    			+ " AND tipo='" + tipo + "'");
    	
		try {
			result.next();
			double lowerBound = result.getDouble("limiteinferior");
			double upperBound = result.getDouble("limitesuperior");
			
			return Filters.and(Filters.gte("Medicao", lowerBound), Filters.lte("Medicao", upperBound));
			
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
}
