package com.sasuke.covid19.strategy;

import android.view.View;

public class TestedPositiveStatusStrategy implements StatusStrategy {
	@Override
	public StatusStrategyResult getStatusVisiblity() {
		return new StatusStrategyResult(View.GONE, View.GONE, View.VISIBLE, "RECOVERING");
	}
}
