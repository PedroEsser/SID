package grupo11.diretosid;

public class Range {
	
	private double lowerBound;
	private double upperBound;
	
	public Range(double lowerBound, double upperBound) {
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
	}

	public boolean isOutOfBounds(double medicao) {
		return medicao > upperBound || medicao < lowerBound;
	}
	
}
