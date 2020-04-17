package com.sasuke.covid19;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.SharedPreferences;
import android.graphics.drawable.Animatable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.sasuke.covid19.strategy.StatusStrategy;
import com.sasuke.covid19.strategy.StatusStrategyFactory;
import com.sasuke.covid19.strategy.StatusStrategyResult;
import com.sasuke.covid19.util.StatusUtil;

public class UserStatusActivity extends AppCompatActivity {

	private static final String _STATUS_REF_KEY = "STATUS";
	private SharedPreferences preferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_status);
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		preferences = getPreferences(MODE_PRIVATE);

		//final int status = getStatusPreferenceValue();
		int status = 2;

		final CompoundTextView statusCtv = findViewById(R.id.user_status_ctv_status);
		final CompoundTextView testedNegCtv = findViewById(R.id.user_status_ctv_tested_neg);
		final CompoundTextView testedPosCtv = findViewById(R.id.user_status_ctv_tested_pos);
		final CompoundTextView recoveredCtv = findViewById(R.id.user_status_ctv_recovered);
		final ImageView checkMark = findViewById(R.id.user_status_iv_animated_check);

		// set UI
		StatusStrategyFactory factory = new StatusStrategyFactory();
		StatusStrategy strategy = factory.getStrategy(StatusUtil.Status.values()[status]);
		StatusStrategyResult result = strategy.getStatusVisiblity();
		testedNegCtv.setVisibility(result.getTestedNegativeCtvVisibility());
		testedPosCtv.setVisibility(result.getTestedPositiveCtvVisibility());
		recoveredCtv.setVisibility(result.getRecoveredCtvVisibility());

		// set status ctv
		String statusLiteral = result.getStatusLiteral();
		statusCtv.setColor(ContextCompat.getColor(this, R.color.expressive));
		setCompoundTextView(statusCtv, statusLiteral, "current status");

		// set status test ctv
		setCompoundTextView(testedNegCtv, "NEGATIVE", "TESTED");
		setCompoundTextView(testedPosCtv, "POSITIVE", "TESTED");
		setCompoundTextView(recoveredCtv, "RECOVERED", "");

		testedNegCtv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				getTestedNegativeCtvAnimator(testedNegCtv).start();
				statusCtv.setPrimaryText(testedNegCtv.getPrimaryText());
				setStatusPreferenceValue(StatusUtil.Status.Negative);
			}
		});

		testedPosCtv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (getStatusPreferenceValue() == StatusUtil.Status.NotTested.ordinal()) {
					getTestedNegativeAndPositiveCtvAnimator(testedNegCtv, testedPosCtv, recoveredCtv).start();
				} else {
					getTestedPositiveCtvAnimator(testedPosCtv, recoveredCtv).start();
				}

				statusCtv.setPrimaryText(testedPosCtv.getPrimaryText());
				setStatusPreferenceValue(StatusUtil.Status.Positive);
			}
		});

		recoveredCtv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				getRecoveredCtvAnimator(recoveredCtv, checkMark).start();

				statusCtv.setPrimaryText(recoveredCtv.getPrimaryText());
				setStatusPreferenceValue(StatusUtil.Status.Recovered);
			}
		});

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

	private AnimatorSet getTestedNegativeCtvAnimator(final CompoundTextView testedNegCtv) {

		ObjectAnimator animation = getTransitionXAnimation(testedNegCtv);
		animation.setDuration(500);

		final AnimatorSet animatorSet = new AnimatorSet();
		animatorSet.play(animation);

		animatorSet.addListener(new Animator.AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animator) {

			}

			@Override
			public void onAnimationEnd(Animator animator) {
				testedNegCtv.setVisibility(View.GONE);
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

		final AnimatorSet animatorSet = new AnimatorSet();
		animatorSet.play(animation);
		animatorSet.play(animatorScaleXRecovered).with(animation);
		animatorSet.play(animatorScaleYRecovered).with(animation);

		animatorSet.addListener(new Animator.AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animator) {

			}

			@Override
			public void onAnimationEnd(Animator animator) {
				testedPosCtv.setVisibility(View.GONE);
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

		final AnimatorSet animatorSet = new AnimatorSet();
		animatorSet.play(animatorRightNeg);

		ObjectAnimator animation = getTransitionXAnimation(testedPosCtv);

		recoveredCtv.setScaleX(0);
		recoveredCtv.setScaleY(0);
		recoveredCtv.setVisibility(View.VISIBLE);

		ObjectAnimator animatorScaleXRecovered = getScaleXAnimation(recoveredCtv);
		ObjectAnimator animatorScaleYRecovered = getScaleYAnimation(recoveredCtv);

		animatorSet.play(animation).after(animatorRightNeg);
		animatorSet.play(animatorScaleXRecovered).with(animation);
		animatorSet.play(animatorScaleYRecovered).with(animation);

		animatorSet.addListener(new Animator.AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animator) {

			}

			@Override
			public void onAnimationEnd(Animator animator) {
				testedNegCtv.setVisibility(View.GONE);
				testedNegCtv.setClickable(false);
				testedPosCtv.setVisibility(View.GONE);
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

	private AnimatorSet getRecoveredCtvAnimator(final CompoundTextView recoveredCtv, final ImageView checkMark) {

		ObjectAnimator animation = getTransitionXAnimation(recoveredCtv);
		animation.setDuration(500);

		final AnimatorSet animatorSet = new AnimatorSet();
		animatorSet.play(animation);

		animatorSet.addListener(new Animator.AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animator) {
				((Animatable) checkMark.getDrawable()).start();
			}

			@Override
			public void onAnimationEnd(Animator animator) {
				recoveredCtv.setVisibility(View.GONE);
				recoveredCtv.setClickable(false);
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

	private int getStatusPreferenceValue() {
		return preferences.getInt(_STATUS_REF_KEY, StatusUtil.Status.NotTested.ordinal());
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

	private ObjectAnimator getTransitionXAnimation(CompoundTextView ctv) {
		return ObjectAnimator.ofFloat(ctv, "translationX", 1000f);
	}
}