package grupo11.projetosid;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import org.bson.Document;

public class Utils {

	public static final DateTimeFormatter STANDARD_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	private static final String[] dateFormats = { "yyyy-MM-dd'T'HH:mm:ss'Z'",   "yyyy-MM-dd 'at' HH:mm:ss z" };
	
	public static String parseDate(String d) {
        if (d != null) {
            for (String parse : dateFormats) {
                SimpleDateFormat sdf = new SimpleDateFormat(parse);
                try {
                	Date date = sdf.parse(d);
                    sdf.applyPattern("yyyy-MM-dd HH:mm:ss");
                    return sdf.format(date).toString();
                } catch (ParseException e) {}
            }
        }
		return null;
    }
	
	public static String getCurrentDateMinus(int minutes) {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern(dateFormats[0]);  
		LocalDateTime now = LocalDateTime.now().minusMinutes(minutes);  
		return dtf.format(now);
	}
	
	public static boolean isValid(Document d) {
		return d.get("Data") != null && isNumber(d.get("Medicao").toString());
	}
	
	public static boolean equals(Document d1, Document d2) {
		if(d1==null || d2==null) {
			return false;
		}
		return  d1.get("Zona").equals(d2.get("Zona")) &&
				d1.get("Sensor").equals(d2.get("Sensor")) &&
				d1.get("Data").equals(d2.get("Data")) &&
				d1.get("Medicao").equals(d2.get("Medicao"));
	}
	
	private static boolean isNumber(String number) {
		try {
			Double.parseDouble(number);
			return true;
		} catch(NumberFormatException e) {
			return false;
		}
	}
	
	public static LocalDateTime stringToDate(String date) {
		return LocalDateTime.parse(date, STANDARD_DATE_FORMAT);
	}
	
}

