package com.sasuke.covid19;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;

public class CompoundTextView extends FrameLayout {

	private TextView primaryTextView;
	private TextView secondaryTextView;

	public CompoundTextView(Context context, AttributeSet attrs) {
		super(context, attrs);

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.compound_text_view, this, true);

		secondaryTextView = (TextView) getChildAt(0);
		primaryTextView = (TextView) getChildAt(1);
	}

	public CompoundTextView(Context context) {
		this(context, null);
	}

	public void setPrimaryText(String text) {
		primaryTextView.setText(text);
	}

	public void setSecondaryText(String text) {
		secondaryTextView.setText(text);
	}

	public void setColor(int color) {
		primaryTextView.setTextColor(color);
		secondaryTextView.setTextColor(color);
	}
}