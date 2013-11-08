package com.derekjass.poolscoresheet.views;

import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewStub;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.derekjass.poolscoresheet.R;

public class PlayerScoreView extends FrameLayout implements IntegerView {

	private int game;
	private int round;
	private boolean ero;

	private TextView score;
	private TextView eroText;
	private ViewStub eroStub;

	private Set<ValueChangedListener> listeners;

	public PlayerScoreView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setBackgroundResource(R.drawable.box_bg);

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
		if (breaks) li.inflate(R.layout.textview_break, this);

		score = (TextView) li.inflate(R.layout.textview_score, this, false);
		addView(score);

		ero = false;
		eroStub = new ViewStub(context);
		eroStub.setLayoutResource(R.layout.textview_ero);
		eroText = null;
		addView(eroStub);

		initBackground(game);

		listeners = new HashSet<ValueChangedListener>();
	}

	@Override
	public void setValue(int value) {
		score.setText(String.valueOf(value));
		notifyListeners();
	}

	@Override
	public void clearValue() {
		score.setText("");
		notifyListeners();
	}

	@Override
	public int getValue() {
		return stringToInt(score.getText().toString());
	}

	@Override
	public boolean hasValue() {
		return !TextUtils.isEmpty(score.getText());
	}

	public void addValueChangedListener(ValueChangedListener li) {
		listeners.add(li);
		li.onListenerAttached(this);
	}

	public void removeValueChangedListener(ValueChangedListener li) {
		listeners.remove(li);
		li.onListenerRemoved(this);
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

	public void setRound(int round) {
		this.round = round;
	}

	public int getRound() {
		return round;
	}

	public void setGame(int game) {
		this.game = game;
		initBackground(game);
	}

	public int getGame() {
		return game;
	}

	protected void notifyListeners() {
		for (ValueChangedListener listener : listeners) {
			listener.onValueChanged(this);
		}
	}

	private void initBackground(int game) {
		Resources res = getContext().getResources();
		int color;
		switch (game) {
		case 1:
			color = res.getColor(R.color.pastel_red);
			break;
		case 2:
			color = res.getColor(R.color.pastel_orange);
			break;
		case 3:
			color = res.getColor(R.color.pastel_yellow);
			break;
		case 4:
			color = res.getColor(R.color.pastel_green);
			break;
		case 5:
			color = res.getColor(R.color.pastel_blue);
			break;
		default:
			color = Color.WHITE;
		}
		GradientDrawable bg = (GradientDrawable) getBackground();
		bg.setColor(color);
	}

	private int stringToInt(String string) {
		return !TextUtils.isEmpty(string) ? Integer.valueOf(string) : 0;
	}
}
