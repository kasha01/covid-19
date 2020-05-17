package com.sasuke.covid19;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.drawable.Animatable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.widget.Toolbar;

import com.google.firebase.firestore.FieldValue;
import com.sasuke.covid19.strategy.StatusStrategy;
import com.sasuke.covid19.strategy.StatusStrategyFactory;
import com.sasuke.covid19.strategy.StatusStrategyResult;
import com.sasuke.covid19.util.Constant;
import com.sasuke.covid19.util.StatusUtil;

import static com.sasuke.covid19.util.Constant.STATUS_REF_KEY;

public class UserStatusActivity extends BaseActivity {
	private static final String TAG = "userStatus_activity";

	private Toolbar toolbar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_status);

		toolbar = findViewById(R.id.toolbar);
		toolbar.setTitle("Not Tested");
		toolbar.setSubtitle(getString(R.string.title_activity_user_status));
		toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
		setSupportActionBar(toolbar);

		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				onBackPressed();
			}
		});

		final String status = getStatusPreferenceValue();
		//String status = StatusUtil.Status.NotTested.toString();   // todo: remove only for testing

		final CompoundTextView testedNegCtv = findViewById(R.id.user_status_ctv_tested_neg);
		final CompoundTextView testedPosCtv = findViewById(R.id.user_status_ctv_tested_pos);
		final CompoundTextView recoveredCtv = findViewById(R.id.user_status_ctv_recovered);
		final ImageView checkMark = findViewById(R.id.user_status_iv_animated_check);

		// set UI
		StatusStrategyFactory factory = new StatusStrategyFactory();
		StatusStrategy strategy = factory.getStrategy(StatusUtil.Status.valueOf(status));
		StatusStrategyResult result = strategy.getStatusVisiblity();
		testedNegCtv.setVisibility(result.getTestedNegativeCtvVisibility());
		testedPosCtv.setVisibility(result.getTestedPositiveCtvVisibility());
		recoveredCtv.setVisibility(result.getRecoveredCtvVisibility());

		// set status ctv
		String statusLiteral = result.getStatusLiteral();
		toolbar.setTitle(statusLiteral);

		// set status test ctv
		setCompoundTextView(testedNegCtv, StatusUtil.Status.Negative.toString().toUpperCase(), "TESTED");
		setCompoundTextView(testedPosCtv, StatusUtil.Status.Positive.toString().toUpperCase(), "TESTED");
		setCompoundTextView(recoveredCtv, StatusUtil.Status.Recovered.toString().toUpperCase(), "");

		testedNegCtv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				StatusUtil.Status status = StatusUtil.Status.Negative;

				getTestedNegativeCtvAnimator(testedNegCtv).start();
				toolbar.setTitle(testedNegCtv.getPrimaryText());
				setStringPreference(STATUS_REF_KEY, status.toString());
				setStatusOnDb(status);
			}
		});

		testedPosCtv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (getStatusPreferenceValue().equals(StatusUtil.Status.NotTested.toString())) {
					getTestedNegativeAndPositiveCtvAnimator(testedNegCtv, testedPosCtv, recoveredCtv).start();
				} else {
					getTestedPositiveCtvAnimator(testedPosCtv, recoveredCtv).start();
				}

				StatusUtil.Status status = StatusUtil.Status.Positive;
				toolbar.setTitle(testedPosCtv.getPrimaryText());
				setStringPreference(STATUS_REF_KEY, status.toString());
				setStatusOnDb(status);
			}
		});

		recoveredCtv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				getRecoveredCtvAnimator(recoveredCtv, checkMark).start();

				StatusUtil.Status status = StatusUtil.Status.Recovered;
				toolbar.setTitle(recoveredCtv.getPrimaryText());
				setStringPreference(STATUS_REF_KEY, status.toString());
				setStatusOnDb(status);
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

	private String getStatusPreferenceValue() {
		return getStringPreference(STATUS_REF_KEY, StatusUtil.Status.NotTested.toString());
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

	private void setStatusOnDb(StatusUtil.Status status) {
		String userDocumentId = getStringPreference(Constant.USER_DOC_ID_PREF_KEY, "");

		if (userDocumentId.equals("")) {
			Log.w(TAG, "userDocId from share_pref, to be empty is not expected.");
			return;
		}

		Log.d(TAG, "msg:user status changed, new user status:" + status.toString());

		String statusField = Constant.UserTable.STATUS_MAP + "." + status.toString();
		db.collection(Constant.UserTable.TABLE_NAME).document(userDocumentId).update(
				statusField, FieldValue.serverTimestamp(),
				Constant.UserTable.INFECTED, StatusUtil.isInfected(status),
				Constant.UserTable.LAST_STATUS_UPDATE, FieldValue.serverTimestamp(),
				Constant.UserTable.STATUS, status.toString()
		);
	}
}