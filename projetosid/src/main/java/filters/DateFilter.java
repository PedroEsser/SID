package filters;

import java.util.Date;

import org.bson.Document;

public class DateFilter implements DocumentFilter{

	private Date date;
	
	public DateFilter(String date) {
		this.date = convertToDate(date);
	}
	
	public static boolean isAfter(String date1, String date2) {
		return convertToDate(date1).after(convertToDate(date2));
	}
	
	public static Date convertToDate(String date) {
		int year = Integer.parseInt(date.substring(0, 4));
		int month = Integer.parseInt(date.substring(5, 7));
		int day = Integer.parseInt(date.substring(8, 10));
		int hour = Integer.parseInt(date.substring(14, 16));
		int min = Integer.parseInt(date.substring(17, 19));
		int sec = Integer.parseInt(date.substring(20, 22));
		return new Date(year, month, day, hour, min, sec);

	}

	@Override
	public boolean filter(Document d) {
		return date.after(convertToDate(d.getString("Data")));
	}
}

