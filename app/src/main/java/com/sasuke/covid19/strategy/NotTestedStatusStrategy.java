package com.sasuke.covid19.strategy;

import android.view.View;

import com.sasuke.covid19.util.StatusUtil;

public class NotTestedStatusStrategy implements StatusStrategy {
	@Override
	public StatusStrategyResult getStatusVisiblity() {
		return new StatusStrategyResult(View.VISIBLE, View.VISIBLE, View.GONE,
				StatusUtil.ToStatusLiteral(StatusUtil.Status.NotTested));
	}
}
