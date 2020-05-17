package com.sasuke.covid19.strategy;

import com.sasuke.covid19.util.StatusUtil;

public class StatusStrategyFactory {

	public StatusStrategy getStrategy(StatusUtil.Status status) {
		switch (status) {
			case NotTested:
				return new NotTestedStatusStrategy();
			case Negative:
				return new TestedNegativeStatusStrategy();
			case Positive:
				return new TestedPositiveStatusStrategy();
			case Recovered:
				return new RecoveredStatusStrategy();
			default:
				throw new ArrayIndexOutOfBoundsException();
		}
	}
}
