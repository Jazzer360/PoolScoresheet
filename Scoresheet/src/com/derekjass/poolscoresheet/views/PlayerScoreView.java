package com.derekjass.poolscoresheet.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewStub;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.derekjass.poolscoresheet.R;

public class PlayerScoreView extends FrameLayout
implements SummableInteger {

	private final int mGame;
	private final int mRound;
	private boolean mEro;

	private SummableInteger mScore;
	private TextView mEroText;
	private ViewStub mEroStub;

	public PlayerScoreView(Context context, AttributeSet attrs) {
		super(context, attrs);

		TypedArray a = context.getTheme().obtainStyledAttributes(
				attrs,
				R.styleable.PlayerScoreView,
				0, 0);

		boolean breaks;
		try {
			mGame = a.getInteger(R.styleable.PlayerScoreView_game, 0);
			mRound = a.getInteger(R.styleable.PlayerScoreView_round, 0);
			breaks = a.getBoolean(R.styleable.PlayerScoreView_breaks, false);
		} finally {
			a.recycle();
		}

		LayoutInflater li = (LayoutInflater)
				context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		li.inflate(R.layout.view_player_score, this);
		mScore = (SummableInteger) findViewById(R.id.scoreText);
		mEroStub = (ViewStub) findViewById(R.id.eroStub);

		if (breaks) ((ViewStub) findViewById(R.id.breakStub)).inflate();

		mEro = false;
		mEroText = null;
	}

	@Override
	public void setValue(int value) {
		mScore.setValue(value);
	}

	@Override
	public void setValue(CharSequence value) {
		mScore.setValue(value);
	}

	@Override
	public void clearValue() {
		mScore.clearValue();
	}

	@Override
	public int getValue() {
		return mScore.getValue();
	}

	@Override
	public String getValueAsString() {
		return mScore.getValueAsString();
	}

	@Override
	public boolean hasValue() {
		return mScore.hasValue();
	}

	@Override
	public boolean hasSoftValue() {
		return mScore.hasSoftValue();
	}

	@Override
	public boolean mustSum() {
		return mScore.mustSum();
	}

	@Override
	public void addOnValueChangedListener(OnValueChangedListener li) {
		mScore.addOnValueChangedListener(li);
	}

	@Override
	public void removeOnValueChangedListener(OnValueChangedListener li) {
		mScore.removeOnValueChangedListener(li);
	}

	public void setEro(boolean isEro) {
		if (mEro ^ isEro) {
			mEro = isEro;
			if (mEroText == null) {
				mEroText = (TextView) mEroStub.inflate();
				mEroStub = null;
			} else {
				mEroText.setVisibility(mEro ? View.VISIBLE : View.GONE);
			}
		}
	}

	public boolean isEro() {
		return mEro;
	}

	public int getRound() {
		return mRound;
	}

	public int getGame() {
		return mGame;
	}
}
