package com.example.geniuschimp;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;

public class ConfigActivity extends Activity {
	public static final String PrefsKeyNumPanels="activatedPanels";
	public static final String PrefsKeyShowMillis="showMillis";
	public static final String PrefsKeyBlankMillis="blankMillis";
	public static final String PrefsKeySound="sound";
	public static final int defNumPanels=8;
	public static final int defShowMillis=3000;
	public static final int defBlankMillis=800;
	public static final boolean defSound=true;
	
	private static final int nPanelsSliderOffset=2;
	private TextView nPanelsLabel,tShowLabel,tBlankLabel;
	private SeekBar nPanelsSlider,tShowSlider,tBlankSlider;
	private String numFmt;
	private SharedPreferences shrP;
	private CheckBox soundCheckbox;
	
	private abstract class ChimpSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {
		protected ChimpSeekBarChangeListener() {
			onProgressChanged(null,0,false);
		}
		@Override
		public void onStartTrackingTouch(SeekBar seekBar) { }
		@Override
		public void onStopTrackingTouch(SeekBar seekBar) { }
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_config);
		
		/*
		if(android.os.Build.VERSION.SDK_INT>11) {
			setActionbarBackButton();
		}
		*/
		
		numFmt=getString(R.string.settingsNumFormat);
		
		nPanelsLabel=(TextView)findViewById(R.id.nPanelsLabel);
		nPanelsSlider=(SeekBar)findViewById(R.id.nPanelsSlider);
		nPanelsSlider.setMax(ChimpActivity.maxChimpPanels-nPanelsSliderOffset);
		nPanelsSlider.setOnSeekBarChangeListener(new ChimpSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				nPanelsLabel.setText(String.format(numFmt,progress+nPanelsSliderOffset));
			}
		});
		tShowLabel=(TextView)findViewById(R.id.tShowLabel);
		tShowSlider=(SeekBar)findViewById(R.id.tShowSlider);
		tShowSlider.setOnSeekBarChangeListener(new ChimpSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				tShowLabel.setText(String.format(numFmt,progress)+" ms");
			}
		});
		tBlankLabel=(TextView)findViewById(R.id.tBlankLabel);
		tBlankSlider=(SeekBar)findViewById(R.id.tBlankSlider);
		tBlankSlider.setOnSeekBarChangeListener(new ChimpSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				tBlankLabel.setText(String.format(numFmt,progress)+" ms");
			}
		});
		
		soundCheckbox=(CheckBox)findViewById(R.id.configSound);
		
		shrP=getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
		int nPanels=shrP.getInt(PrefsKeyNumPanels, defNumPanels);
		int showMillis=shrP.getInt(PrefsKeyShowMillis, defShowMillis);
		int blankMillis=shrP.getInt(PrefsKeyBlankMillis, defBlankMillis);
		boolean enableSound=shrP.getBoolean(PrefsKeySound, defSound);
		
		nPanelsSlider.setProgress(nPanels-nPanelsSliderOffset);
		tShowSlider.setProgress(showMillis);
		tBlankSlider.setProgress(blankMillis);
		soundCheckbox.setChecked(enableSound);
	}
	@Override
	public void onPause() {
		int nPanels=nPanelsSlider.getProgress()+nPanelsSliderOffset;
		int showMillis=tShowSlider.getProgress();
		int blankMillis=tBlankSlider.getProgress();
		boolean enableSound=soundCheckbox.isChecked();
		
		SharedPreferences.Editor e=shrP.edit();
		e.putInt(PrefsKeyNumPanels, nPanels);
		e.putInt(PrefsKeyShowMillis, showMillis);
		e.putInt(PrefsKeyBlankMillis, blankMillis);
		e.putBoolean(PrefsKeySound, enableSound);
		e.commit();
		super.onPause();
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int menuId=item.getItemId();
		if(menuId==android.R.id.home) {
			finish();
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}
	/*
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setActionbarBackButton() {
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}
	*/
}
