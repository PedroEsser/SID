package filters;

import org.bson.Document;

public class SensorTypeFilter implements DocumentFilter{

	private String type;
	
	public SensorTypeFilter(String type) {
		this.type = type;
	}

	@Override
	public boolean filter(Document d) {
		return d.get("Sensor").equals(type);
	}
	
}