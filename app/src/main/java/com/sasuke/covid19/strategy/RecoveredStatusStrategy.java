package com.sasuke.covid19.strategy;

import android.view.View;

import com.sasuke.covid19.util.StatusUtil;

public class RecoveredStatusStrategy implements StatusStrategy {
	@Override
	public StatusStrategyResult getStatusVisiblity() {
		return new StatusStrategyResult(View.GONE, View.GONE, View.GONE, StatusUtil.ToStatusLiteral(StatusUtil.Status.Recovered));
	}
}
