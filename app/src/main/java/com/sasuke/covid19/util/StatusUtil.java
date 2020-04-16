package com.sasuke.covid19.util;

public class StatusUtil {

	public static String ToStatusLiteral(int ordinal) {
		Status status = Status.values()[ordinal];

		switch (status) {
			case Negative:
			case Positive:
			case Recovered:
			case Recovering:
				return status.toString().toUpperCase();

			default:
				return "NOT TESTED";
		}
	}

	public static enum Status {
		NotTested,
		Negative,
		Positive,
		Recovering,
		Recovered
	}
}
