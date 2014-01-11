package com.example.geniuschimp;

import java.text.NumberFormat;
import java.util.Arrays;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class HighscoreActivity extends Activity {
	private class HighscoreEntry {
		public long score;
		public int panels;
		public boolean highlight;
	}
	private class HighscoreEntryComparator implements java.util.Comparator<HighscoreEntry> {
		@Override
		public int compare(HighscoreEntry x, HighscoreEntry y) {
			if(x.score>y.score) {
				return -1;
			} else if(x.score<y.score) {
				return 1;
			} else {
				return 0;
			}
		}
	}
	private static final String scoreSaveLabel="scores";
	private static final String highlightSaveLabel="highlight";
	private final int numHighscores=5;
	private long score;
	private int panels;
	private TextView[] highscoreLines=new TextView[numHighscores];
	private TextView scoreLabel;
	private NumberFormat numFormat=NumberFormat.getInstance();
	private HighscoreEntry highscores[]=new HighscoreEntry[numHighscores+1];
	private SharedPreferences shrP;
	private final String score0="score0", score1="score1",score2="score2",score3="score3",score4="score4";
	private final String panels0="panels0",panels1="panels1",panels2="panels2",panels3="panels3",panels4="panels4";
	private SoundPool soundPool=null;
	private float soundVolume=0.f;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_highscore);
		
		shrP=getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
		if(android.os.Build.VERSION.SDK_INT>=8) {
			playWelcomeSound();
		}
		for(int i=0; i<highscores.length; i++) {
			highscores[i]=new HighscoreEntry();
		}
		findTextViews();
		loadHighscores();
		
		if(savedInstanceState==null) {
			Intent i=getIntent();
			panels=i.getIntExtra("panels", 0);
			score=i.getLongExtra("score", -1l);
			
			int sndResId;
			if(score>0) {
				scoreLabel.setText(numFormat.format(score));
				if(score>highscores[numHighscores-1].score) {
					updateHighscores();
					sndResId=R.raw.highscore;
				} else {
					sndResId=R.raw.gameover;
				}
				soundPool.load(this,sndResId,1);
			} else {
				scoreLabel.setVisibility(View.INVISIBLE);
			}
		} else {
			score=savedInstanceState.getLong(scoreSaveLabel);
			if(score>0) {
				scoreLabel.setText(numFormat.format(score));
				int highlightID=savedInstanceState.getInt(highlightSaveLabel);
				highscores[highlightID].highlight=true;
			} else {
				scoreLabel.setVisibility(View.INVISIBLE);
			}
		}
		updateHighscoreView();
	}
	@TargetApi(Build.VERSION_CODES.FROYO)
	private void playWelcomeSound() {
		boolean soundEnabled=shrP.getBoolean(ConfigActivity.PrefsKeySound, ConfigActivity.defSound);
		soundVolume=soundEnabled?1.f:0.f;
		
		soundPool=new SoundPool(1, android.media.AudioManager.STREAM_MUSIC, 0);
		soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
			@Override
			public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
				if(status==0) { soundPool.play(sampleId, soundVolume, soundVolume, 1, 0, 1.f); } }});
	}
	private void findTextViews() {
		highscoreLines[0]=(TextView)findViewById(R.id.highscoreLine1);
		highscoreLines[1]=(TextView)findViewById(R.id.highscoreLine2);
		highscoreLines[2]=(TextView)findViewById(R.id.highscoreLine3);
		highscoreLines[3]=(TextView)findViewById(R.id.highscoreLine4);
		highscoreLines[4]=(TextView)findViewById(R.id.highscoreLine5);
		scoreLabel=(TextView)findViewById(R.id.scoreLabelHS);
	}
	private void loadHighscores() {
		highscores[0].score=shrP.getLong(score0, 68000l);
		highscores[1].score=shrP.getLong(score1, 8086l);
		highscores[2].score=shrP.getLong(score2, 6809l);
		highscores[3].score=shrP.getLong(score3, 6502l);
		highscores[4].score=shrP.getLong(score4, 780l);
		highscores[0].panels=shrP.getInt(panels0, 8);
		highscores[1].panels=shrP.getInt(panels1, 7);
		highscores[2].panels=shrP.getInt(panels2, 6);
		highscores[3].panels=shrP.getInt(panels3, 5);
		highscores[4].panels=shrP.getInt(panels4, 4);
		for(HighscoreEntry e: highscores) {
			e.highlight=false;
		}
	}
	private void updateHighscores() {
		highscores[numHighscores].score=score;
		highscores[numHighscores].panels=panels;
		highscores[numHighscores].highlight=true;
		Arrays.sort(highscores,new HighscoreEntryComparator());
		SharedPreferences.Editor edit=shrP.edit();
		edit.putLong(score0, highscores[0].score);
		edit.putLong(score1, highscores[1].score);
		edit.putLong(score2, highscores[2].score);
		edit.putLong(score3, highscores[3].score);
		edit.putLong(score4, highscores[4].score);
		edit.putInt(panels0, highscores[0].panels);
		edit.putInt(panels1, highscores[1].panels);
		edit.putInt(panels2, highscores[2].panels);
		edit.putInt(panels3, highscores[3].panels);
		edit.putInt(panels4, highscores[4].panels);
		edit.commit();
	}
	private void updateHighscoreView() {
		final String hsformat=getString(R.string.highscoreFormat);
		for(int i=0; i<numHighscores; i++) {
			String line=String.format(hsformat, highscores[i].panels,highscores[i].score);
			highscoreLines[i].setText(line);
			if(highscores[i].highlight) {
				highscoreLines[i].setTextColor(getResources().getColor(R.color.highscoreHighlightColor));
			}
		}
	}
	public boolean doneButtonClicked(View v) {
		finish();
		return true;
	}
	@Override
	public void onDestroy() {
		soundPool.release();
		super.onDestroy();
	}
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		int highlightID=0;
		for(int i=0; i<numHighscores; i++) {
			if(highscores[i].highlight) {
				highlightID=i;
				break;
			}
		}
		savedInstanceState.putLong(scoreSaveLabel,score);
		savedInstanceState.putInt(highlightSaveLabel, highlightID);
	}
}
