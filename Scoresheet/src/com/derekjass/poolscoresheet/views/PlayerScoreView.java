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

	private final int game;
	private final int round;
	private boolean ero;

	private SummableInteger score;
	private TextView eroText;
	private ViewStub eroStub;

	public PlayerScoreView(Context context, AttributeSet attrs) {
		super(context, attrs);

		TypedArray a = context.getTheme().obtainStyledAttributes(
				attrs,
				R.styleable.PlayerScoreView,
				0, 0);

		boolean breaks;
		try {
			game = a.getInteger(R.styleable.PlayerScoreView_game, 0);
			round = a.getInteger(R.styleable.PlayerScoreView_round, 0);
			breaks = a.getBoolean(R.styleable.PlayerScoreView_breaks, false);
		} finally {
			a.recycle();
		}

		LayoutInflater li = (LayoutInflater)
				context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		li.inflate(R.layout.view_player_score, this);
		score = (SummableInteger) findViewById(R.id.scoreText);
		eroStub = (ViewStub) findViewById(R.id.eroStub);

		if (breaks) {
			((ViewStub) findViewById(R.id.breakStub)).inflate();
		}

		ero = false;
		eroText = null;
	}

	@Override
	public void setValue(int value) {
		score.setValue(value);
	}

	@Override
	public void setValue(CharSequence value) {
		score.setValue(value);
	}

	@Override
	public void clearValue() {
		score.clearValue();
	}

	@Override
	public int getValue() {
		return score.getValue();
	}

	@Override
	public String getValueAsString() {
		return score.getValueAsString();
	}

	@Override
	public boolean hasValue() {
		return score.hasValue();
	}

	@Override
	public boolean hasSoftValue() {
		return score.hasSoftValue();
	}

	@Override
	public boolean mustSum() {
		return score.mustSum();
	}

	@Override
	public void addOnValueChangedListener(OnValueChangedListener li) {
		score.addOnValueChangedListener(li);
	}

	@Override
	public void removeOnValueChangedListener(OnValueChangedListener li) {
		score.removeOnValueChangedListener(li);
	}

	public void setEro(boolean isEro) {
		if (ero ^ isEro) {
			ero = isEro;
			if (eroText == null) {
				eroText = (TextView) eroStub.inflate();
				eroStub = null;
			} else {
				eroText.setVisibility(ero ? View.VISIBLE : View.GONE);
			}
		}
	}

	public boolean isEro() {
		return ero;
	}

	public int getRound() {
		return round;
	}

	public int getGame() {
		return game;
	}
}
