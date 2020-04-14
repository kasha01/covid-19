package com.sasuke.covid19;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

public class UserStatusActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_status);
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		CompoundTextView statusCtv = findViewById(R.id.user_status_ctv_status);
		CompoundTextView notTestedCtv = findViewById(R.id.user_status_ctv_not_tested);
		CompoundTextView testedNegCtv = findViewById(R.id.user_status_ctv_tested_neg);
		CompoundTextView testedPosCtv = findViewById(R.id.user_status_ctv_tested_pos);

		setCompoundTextView(statusCtv, "NOT TESTED", "current status");
		setCompoundTextView(notTestedCtv, "NOT TESTED", "");
		setCompoundTextView(testedNegCtv, "NEGATIVE", "TESTED");
		setCompoundTextView(testedPosCtv, "POSITIVE", "TESTED");

		testedNegCtv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Toast.makeText(getBaseContext(), "click", Toast.LENGTH_LONG).show();
			}
		});

		statusCtv.setColor(ContextCompat.getColor(this, R.color.expressive));

		FloatingActionButton fab = findViewById(R.id.fab);
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
						.setAction("Action", null).show();
			}
		});
	}

	private void setCompoundTextView(CompoundTextView compoundTextView, String primary, String secondary) {
		compoundTextView.setPrimaryText(primary);
		compoundTextView.setSecondaryText(secondary);
	}
}