package com.sasuke.covid19;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
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

		final CompoundTextView statusCtv = findViewById(R.id.user_status_ctv_status);
		final CompoundTextView notTestedCtv = findViewById(R.id.user_status_ctv_not_tested);
		final CompoundTextView testedNegCtv = findViewById(R.id.user_status_ctv_tested_neg);
		final CompoundTextView testedPosCtv = findViewById(R.id.user_status_ctv_tested_pos);

		setCompoundTextView(statusCtv, "NOT TESTED", "current status");
		setCompoundTextView(notTestedCtv, "NOT TESTED", "");
		setCompoundTextView(testedNegCtv, "NEGATIVE", "TESTED");
		setCompoundTextView(testedPosCtv, "POSITIVE", "TESTED");

		notTestedCtv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				getNotTestedCtvAnimator(notTestedCtv, testedNegCtv, testedPosCtv).start();
				notTestedCtv.setClickable(false);
				statusCtv.setPrimaryText(notTestedCtv.getPrimaryText());
			}
		});

		testedNegCtv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Toast.makeText(getBaseContext(), "click -ve", Toast.LENGTH_LONG).show();
			}
		});

		testedPosCtv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Toast.makeText(getBaseContext(), "click +ve", Toast.LENGTH_LONG).show();
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

	private AnimatorSet getNotTestedCtvAnimator(final CompoundTextView notTestedCtv,
	                                            CompoundTextView testedNegCtv, CompoundTextView testedPosCtv) {

		ObjectAnimator animation = ObjectAnimator.ofFloat(notTestedCtv, "translationX", 1000f);
		animation.setDuration(500);

		ObjectAnimator animatorUpNeg = ObjectAnimator.ofFloat(testedNegCtv, "translationY",
				getResources().getDimensionPixelSize(R.dimen.animationTransitionY));

		ObjectAnimator animatorUpPos = ObjectAnimator.ofFloat(testedPosCtv, "translationY",
				getResources().getDimensionPixelSize(R.dimen.animationTransitionY));

		// property animation
		final AnimatorSet animatorSet = new AnimatorSet();
		animatorSet.play(animation);
		animatorSet.play(animatorUpNeg).with(animation);
		animatorSet.play(animatorUpPos).with(animation);
		//animation.start();

		animatorSet.addListener(new Animator.AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animator) {

			}

			@Override
			public void onAnimationEnd(Animator animator) {
				notTestedCtv.setVisibility(View.INVISIBLE);
			}

			@Override
			public void onAnimationCancel(Animator animator) {

			}

			@Override
			public void onAnimationRepeat(Animator animator) {

			}
		});

		return animatorSet;
	}
}