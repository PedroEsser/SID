package filters;

import org.bson.Document;

public interface DocumentFilter {

	public boolean filter(Document d);
	
}
