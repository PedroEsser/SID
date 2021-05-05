package grupo11.diretosid;

import static grupo11.diretosid.Utils.convert;

public class Culture {

	private String idculture;
	private Range limits;
	private int outOfRange;

	public Culture(String idculture, Range limits) {
		this.idculture = idculture;
		this.limits = limits;
		this.outOfRange = 0;
	}

	public boolean equals(Culture c) {
		return this.idculture.equals(c.idculture);
	}

	public void checkMeasurement(Double medicao) {
		if (limits.isOutOfBounds(medicao)) {
			outOfRange++;
		}
	}

	public void checkMeasurement(String medicao) {
		if (limits.isOutOfBounds(convert(medicao))) {
			outOfRange++;
		}
	}

	public boolean checkNumbersofTime(int num) {
		return outOfRange >= num;
	}

	public boolean inPercentil(String measure, Range percentage) {
		double interval = limits.getUpperBound() - limits.getLowerBound();
		double minLim = (percentage.getLowerBound() * interval) + limits.getLowerBound();
		double maxLim = (percentage.getUpperBound() * interval) + limits.getLowerBound();

		if ((convert(measure) > minLim) && (convert(measure) < maxLim)) {
			return true;
		}
		return false;
	}

	public boolean inUpperPercentage(String measure, double upperlimit) {
		double interval = limits.getUpperBound() - limits.getLowerBound();
		double maxLim = (upperlimit * interval) + limits.getLowerBound();

		if (convert(measure) < maxLim) {
			return true;
		}
		return false;
	}

	public String getID() {
		return idculture;
	}

	public Range getLimits() {
		return limits;
	}

	public int getOutOfRange() {
		return outOfRange;
	}
}