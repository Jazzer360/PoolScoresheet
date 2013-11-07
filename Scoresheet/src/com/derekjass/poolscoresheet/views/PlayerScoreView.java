package com.derekjass.poolscoresheet.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.ViewStub;
import android.widget.FrameLayout;

import com.derekjass.poolscoresheet.R;

public class PlayerScoreView extends FrameLayout implements IntegerView {

	public interface ScoreChangedListener {
		public void onScoreChanged(PlayerScoreView view);
	}
	
	public PlayerScoreView(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray a = context.getTheme().obtainStyledAttributes(
				attrs,
				R.styleable.PlayerScoreView,
				0, 0);

		try {
			int game = a.getInteger(R.styleable.PlayerScoreView_game, 0);
			initBackground(game);
		} finally {
			a.recycle();
		}
		
		ViewStub stub = new ViewStub(context, 0);
	}

	private void initBackground(int game) {
		switch (game) {
		case 1:
			setBackgroundResource(R.drawable.score1_bg);
			return;
		case 2:
			setBackgroundResource(R.drawable.score2_bg);
			return;
		case 3:
			setBackgroundResource(R.drawable.score3_bg);
			return;
		case 4:
			setBackgroundResource(R.drawable.score4_bg);
			return;
		case 5:
			setBackgroundResource(R.drawable.score5_bg);
			return;
		default:
			setBackgroundResource(R.drawable.box_bg);
		}
	}

	@Override
	public int getValue() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean hasValue() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setValue(int value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clearValue() {
		// TODO Auto-generated method stub
		
	}
}
