package grupo11.diretosid;

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
		try {
			Double aux = Double.parseDouble(medicao);
			if (limits.isOutOfBounds(aux)) {
				outOfRange++;
			}
		} catch (NumberFormatException e) {
			System.err.println("Number Formar Error!");
		}

	}

	public boolean checkNumbersofTime(int num) {
		return outOfRange >= num;
	}

	public int getOutOfRange() {
		return outOfRange;
	}
}