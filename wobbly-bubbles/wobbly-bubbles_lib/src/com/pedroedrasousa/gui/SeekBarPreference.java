package com.pedroedrasousa.gui;

import com.pedroedrasousa.wobblybubbleslib.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class SeekBarPreference extends Preference implements OnSeekBarChangeListener {
	
	private static final String ANDROIDNS			= "http://schemas.android.com/apk/res/android";
	private static final String PEDROEDRASOUSANS	= "http://pedroedrasousa.com";
	
	private static final int DEFAULT_VALUE = 50;
	
	private static Toast mToast;
	
	private SeekBar		mSeekBar;
	private TextView	mValueView;
	
	private int	mMaxValue		= 100;
	private int	mMinValue		= 0;
	private int	mInterval		= 1;
	private int	mCurrentValue	= 0;

	public SeekBarPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		setValuesFromXml(attrs);
	}

	public SeekBarPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setValuesFromXml(attrs);
	}

	private void setValuesFromXml(AttributeSet attrs) {
        mMaxValue = attrs.getAttributeIntValue(ANDROIDNS, "max", 100);
        mMinValue = attrs.getAttributeIntValue(PEDROEDRASOUSANS, "min", 0);        
        mInterval = attrs.getAttributeIntValue(PEDROEDRASOUSANS, "interval", 1);
	}

	@Override
	protected View onCreateView(ViewGroup parent) {
		
		View ret = super.onCreateView(parent);

		View summary = ret.findViewById(android.R.id.summary);
		if (summary != null) {
			ViewParent summaryParent = summary.getParent();
			if (summaryParent instanceof ViewGroup) {
				final LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				ViewGroup summaryParent2 = (ViewGroup) summaryParent;
				layoutInflater.inflate(R.layout.seekbar_preference, summaryParent2);

				mSeekBar = (SeekBar) summaryParent2.findViewById(R.id.seekBar);
				mSeekBar.setMax(mMaxValue - mMinValue);
				mSeekBar.setOnSeekBarChangeListener(this);
				
				mValueView = (TextView) summaryParent2.findViewById(R.id.seekBarValue);
			}
		}

		return ret;
	}

	@Override
	public void onBindView(View view) {
		super.onBindView(view);
		updateView();
	}

	protected void updateView() {
		if (mValueView != null) {
			mValueView.setText(String.valueOf(mCurrentValue));
			mValueView.setMinimumWidth(30);
		}

		if (mSeekBar != null) {
			mSeekBar.setProgress(mCurrentValue - mMinValue);
		}
		//notifyChanged();	// Causes seekbar to loose focus on some devices
	}

	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		int newValue = progress + mMinValue;

		// Validate value
		if (newValue > mMaxValue) {
			newValue = mMaxValue;
		}
		else if (newValue < mMinValue) {
			newValue = mMinValue;
		}
		else if (mInterval != 1 && newValue % mInterval != 0) {
			newValue = Math.round(((float) newValue) / mInterval) * mInterval;
		}

		if (!callChangeListener(newValue)) {
			// Revert to the previous value
			seekBar.setProgress(mCurrentValue - mMinValue);
			return;
		}

		// Change accepted
		mCurrentValue = newValue;

		persistInt(newValue);
			
		if (fromUser && mToast != null) {
			mToast.setText(SeekBarPreference.this.getTitle() + ": " + mCurrentValue + " / " + mMaxValue);
			mToast.show();
		}
	}
	
	public void setProgress(int progress) {
		mSeekBar.setProgress(progress - mMinValue);
	}

	public void onStartTrackingTouch(SeekBar seekBar) {
	    mToast = Toast.makeText(getContext(), SeekBarPreference.this.getTitle() + ": " + mCurrentValue + " / " + mMaxValue, Toast.LENGTH_SHORT);
	    mToast.show();
	}

	public void onStopTrackingTouch(SeekBar seekBar) {
		mToast.cancel();
		if (mValueView != null) {
			mValueView.setText(String.valueOf(mCurrentValue));
		}
		notifyChanged();
	}

	@Override
	protected Object onGetDefaultValue(TypedArray ta, int index) {
		int defaultValue = ta.getInt(index, DEFAULT_VALUE);
		return defaultValue;
	}

	@Override
	protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
		if (restoreValue) {
			mCurrentValue = getPersistedInt(mCurrentValue);
		}
		else {
			int temp = 0;
			if (defaultValue instanceof Integer) {
				temp = (Integer)defaultValue;
			}

			persistInt(temp);
			mCurrentValue = temp;
		}
	}
}
