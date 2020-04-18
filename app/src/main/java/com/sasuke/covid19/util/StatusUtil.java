package com.sasuke.covid19.util;

public class StatusUtil {

	public static String ToStatusLiteral(Status status) {
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

	public static boolean isInfected(Status status){
		return status == Status.Positive;
	}

	public static String ToStatusLiteral(int ordinal) {
		Status status = Status.values()[ordinal];
		return ToStatusLiteral(status);
	}

	public enum Status {
		NotTested,
		Negative,
		Positive,
		Recovering,
		Recovered
	}
}
