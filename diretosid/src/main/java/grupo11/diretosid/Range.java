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
	
	public boolean isOutOfUpperBounds(double medicao) {
		return medicao > upperBound;
	}
	
	public boolean isOutOfLowerBounds(double medicao) {
		return medicao < lowerBound;
	}

	public double getLowerBound() {
		return lowerBound;
	}

	public double getUpperBound() {
		return upperBound;
	}
}
