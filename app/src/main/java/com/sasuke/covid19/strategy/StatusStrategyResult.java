package com.sasuke.covid19.strategy;

public class StatusStrategyResult {
	private int testedNegativeCtvVisibility;
	private int testedPositiveCtvVisibility;
	private int recoveredCtvVisibility;
	private String statusLiteral;


	public StatusStrategyResult(int testedNegativeCtvVisibility, int testedPositiveCtvVisibility, int recoveredCtvVisibility, String statusLiteral) {
		this.testedNegativeCtvVisibility = testedNegativeCtvVisibility;
		this.testedPositiveCtvVisibility = testedPositiveCtvVisibility;
		this.recoveredCtvVisibility = recoveredCtvVisibility;
		this.statusLiteral = statusLiteral;
	}

	public int getTestedNegativeCtvVisibility() {
		return testedNegativeCtvVisibility;
	}

	public int getTestedPositiveCtvVisibility() {
		return testedPositiveCtvVisibility;
	}

	public int getRecoveredCtvVisibility() {
		return recoveredCtvVisibility;
	}

	public String getStatusLiteral() {
		return statusLiteral;
	}
}
