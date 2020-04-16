package com.sasuke.covid19;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.sasuke.covid19.util.StatusUtil;

public class UserStatusActivity extends AppCompatActivity {

	private static final String _STATUS_REF_KEY = "STATUS";
	private int status;
	private SharedPreferences preferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_status);
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		preferences = getPreferences(MODE_PRIVATE);

		//status = preferences.getInt(_STATUS_REF_KEY, StatusUtil.Status.NotTested.ordinal());
		status = 0;
		String statusLiteral = StatusUtil.ToStatusLiteral(status);

		final CompoundTextView statusCtv = findViewById(R.id.user_status_ctv_status);
		final CompoundTextView testedNegCtv = findViewById(R.id.user_status_ctv_tested_neg);
		final CompoundTextView testedPosCtv = findViewById(R.id.user_status_ctv_tested_pos);
		final CompoundTextView recoveredCtv = findViewById(R.id.user_status_ctv_recovered);

		setCompoundTextView(statusCtv, statusLiteral, "current status");
		setCompoundTextView(testedNegCtv, "NEGATIVE", "TESTED");
		setCompoundTextView(testedPosCtv, "POSITIVE", "TESTED");
		setCompoundTextView(recoveredCtv, "RECOVERED", "");

		testedNegCtv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				getTestedNegativeCtvAnimator(testedNegCtv, testedPosCtv).start();
				statusCtv.setPrimaryText(testedNegCtv.getPrimaryText());
				setStatusPreferenceValue(StatusUtil.Status.Negative);
			}
		});

		testedPosCtv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (status == StatusUtil.Status.NotTested.ordinal()) {
					getTestedNegativeAndPositiveCtvAnimator(testedNegCtv, testedPosCtv, recoveredCtv).start();
				} else {
					getTestedPositiveCtvAnimator(testedPosCtv, recoveredCtv).start();
				}

				statusCtv.setPrimaryText(testedPosCtv.getPrimaryText());
				setStatusPreferenceValue(StatusUtil.Status.Positive);
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

	private AnimatorSet getTestedNegativeCtvAnimator(final CompoundTextView testedNegCtv, CompoundTextView testedPosCtv) {

		ObjectAnimator animation = getTransitionXAnimation(testedNegCtv);
		animation.setDuration(500);

		ObjectAnimator animatorUpPos = getTransitionYAnimation(testedPosCtv);

		final AnimatorSet animatorSet = new AnimatorSet();
		animatorSet.play(animation);
		animatorSet.play(animatorUpPos).with(animation);

		animatorSet.addListener(new Animator.AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animator) {

			}

			@Override
			public void onAnimationEnd(Animator animator) {
				testedNegCtv.setVisibility(View.INVISIBLE);
				testedNegCtv.setClickable(false);
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

	private AnimatorSet getTestedPositiveCtvAnimator(final CompoundTextView testedPosCtv, CompoundTextView recoveredCtv) {

		ObjectAnimator animation = getTransitionXAnimation(testedPosCtv);
		animation.setDuration(500);

		recoveredCtv.setScaleX(0);
		recoveredCtv.setScaleY(0);
		recoveredCtv.setVisibility(View.VISIBLE);

		ObjectAnimator animatorScaleXRecovered = getScaleXAnimation(recoveredCtv);
		ObjectAnimator animatorScaleYRecovered = getScaleYAnimation(recoveredCtv);
		ObjectAnimator animatorUpRecovered = getTransitionYAnimationDoubleStep(recoveredCtv);

		// property animation
		final AnimatorSet animatorSet = new AnimatorSet();
		animatorSet.play(animation);
		animatorSet.play(animatorScaleXRecovered).with(animation);
		animatorSet.play(animatorScaleYRecovered).with(animation);
		animatorSet.play(animatorUpRecovered).with(animation);

		animatorSet.addListener(new Animator.AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animator) {

			}

			@Override
			public void onAnimationEnd(Animator animator) {
				testedPosCtv.setVisibility(View.INVISIBLE);
				testedPosCtv.setClickable(false);
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

	private AnimatorSet getTestedNegativeAndPositiveCtvAnimator(final CompoundTextView testedNegCtv,
	                                                            final CompoundTextView testedPosCtv,
	                                                            CompoundTextView recoveredCtv) {

		ObjectAnimator animatorRightNeg = getTransitionXAnimation(testedNegCtv);
		animatorRightNeg.setDuration(500);

		ObjectAnimator animatorUpPos = getTransitionYAnimation(testedPosCtv);

		final AnimatorSet animatorSet = new AnimatorSet();
		animatorSet.play(animatorRightNeg);
		animatorSet.play(animatorUpPos).with(animatorRightNeg);


		ObjectAnimator animation = getTransitionXAnimation(testedPosCtv);

		recoveredCtv.setScaleX(0);
		recoveredCtv.setScaleY(0);
		recoveredCtv.setVisibility(View.VISIBLE);

		ObjectAnimator animatorScaleXRecovered = getScaleXAnimation(recoveredCtv);
		ObjectAnimator animatorScaleYRecovered = getScaleYAnimation(recoveredCtv);
		ObjectAnimator animatorUpRecovered = getTransitionYAnimationDoubleStep(recoveredCtv);

		animatorSet.play(animation).after(animatorRightNeg);
		animatorSet.play(animatorScaleXRecovered).with(animation);
		animatorSet.play(animatorScaleYRecovered).with(animation);
		animatorSet.play(animatorUpRecovered).with(animation);

		animatorSet.addListener(new Animator.AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animator) {

			}

			@Override
			public void onAnimationEnd(Animator animator) {
				testedNegCtv.setVisibility(View.INVISIBLE);
				testedNegCtv.setClickable(false);
				testedPosCtv.setVisibility(View.INVISIBLE);
				testedPosCtv.setClickable(false);
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

	private void setStatusPreferenceValue(StatusUtil.Status status) {
		SharedPreferences.Editor editor = preferences.edit();
		editor.putInt(_STATUS_REF_KEY, status.ordinal()).apply();
	}

	private ObjectAnimator getScaleXAnimation(CompoundTextView ctv) {
		return ObjectAnimator.ofFloat(ctv, "scaleX", 0, 1);
	}

	private ObjectAnimator getScaleYAnimation(CompoundTextView ctv) {
		return ObjectAnimator.ofFloat(ctv, "scaleY", 0, 1);
	}

	private ObjectAnimator getTransitionYAnimation(CompoundTextView ctv) {
		return ObjectAnimator.ofFloat(ctv, "translationY", getResources().getDimensionPixelSize(R.dimen.animationTransitionY));
	}

	private ObjectAnimator getTransitionYAnimationDoubleStep(CompoundTextView ctv) {
		return ObjectAnimator.ofFloat(ctv, "translationY", getResources().getDimensionPixelSize(R.dimen.animationTransitionY) * 2);
	}

	private ObjectAnimator getTransitionXAnimation(CompoundTextView ctv) {
		return ObjectAnimator.ofFloat(ctv, "translationX", 1000f);
	}
}