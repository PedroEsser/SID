package grupo11.projetosid;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateUtils {

	private static final String[] formats = { "yyyy-MM-dd'T'HH:mm:ss'Z'",   "yyyy-MM-dd 'at' HH:mm:ss z" };
	
	public static String parse(String d) {
        if (d != null) {
            for (String parse : formats) {
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
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern(formats[0]);  
		LocalDateTime now = LocalDateTime.now().minusMinutes(minutes);  
		return dtf.format(now);
	}
	
}
