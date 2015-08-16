package com.example.geniuschimp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class TitleActivity extends Activity implements SharedPreferences.OnSharedPreferenceChangeListener {
	private SoundPool soundPool;
	private int startSndId;
	private float soundVolume=0.f;
	private SharedPreferences shrP;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_title);
		
		shrP=getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
		shrP.registerOnSharedPreferenceChangeListener(this);
		onSharedPreferenceChanged(shrP,"");
		
		soundPool=new SoundPool(3, android.media.AudioManager.STREAM_MUSIC, 0);
		startSndId=soundPool.load(this,R.raw.start,1);
		
		TextView versionLabel=(TextView) findViewById(R.id.version_label);
		String versionName;
		try {
			versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			versionName = "0";
		}
		versionLabel.setText(versionName);
	}
	@Override
	public void onDestroy() {
		soundPool.release();
		soundPool=null;
		super.onDestroy();
	}
	private void playSFX(int SFXid) {
		soundPool.play(SFXid, soundVolume, soundVolume, 1, 0, 1.f);
	}
	public boolean StartButtonClicked(View v) {
		playSFX(startSndId);
		Intent intent=new Intent(this,ChimpActivity.class);
		startActivity(intent);
		return true;
	}
	public boolean HighscoreButtonClicked(View v) {
		playSFX(startSndId);
		Intent intent=new Intent(this,HighscoreActivity.class);
		startActivity(intent);
		return true;
	}
	public boolean ConfigButtonClicked(View v) {
		playSFX(startSndId);
		Intent intent=new Intent(this,ConfigActivity.class);
		startActivity(intent);
		return true;
	}
	@Override
	public void onSharedPreferenceChanged(SharedPreferences shrP, String arg) {
		boolean soundEnabled=shrP.getBoolean(ConfigActivity.PrefsKeySound, ConfigActivity.defSound);
		soundVolume=soundEnabled?1.f:0.f;
	}
}
