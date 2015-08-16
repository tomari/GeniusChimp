package com.example.geniuschimp;

import java.text.NumberFormat;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

public class ChimpActivity extends Activity implements ChimpGameView.PanelTouchListener, TimerThread.TimerListener {
	public static final int maxChimpPanels=12;
	private static final String cPanelSaveLabel="cpanels";
	private static final String stateSaveLabel="state";
	private static final String scoreSaveLabel="score";
	private static final String multiplierSaveLabel="multiplier";
	private static final String phaseSaveLabel="phase";
	private static final String seqNumSaveLabel="seq";
	private static final String panelVisibilitySaveLabel="panelVisible";
	private ChimpPanel[] cPanel=new ChimpPanel[maxChimpPanels];
	private ChimpGameView gameView;
	private SoundPool soundPool;
	private int bubuSndId;
	private int clearSndId;
	private int failSndId;
	private int flipSndId;
	private int pingpongSndId;
	private int startSndId;
	private java.util.Random r;
	private enum GameState {
		Init, WaitStart, Show, Blank, AcceptSequence,
		StageCleared, StageFailed, GameOver}
	private GameState state;
	private int seqNumber;
	private int numActivatedPanels=ConfigActivity.defNumPanels;
	private int showMillis=ConfigActivity.defShowMillis;
	private int blankMillis=ConfigActivity.defBlankMillis;
	private int afterStageMillis=1000;
	private Handler handler;
	private TimerThread timer;
	private TextView touchScreenMessage;
	private long score,multiplier;
	private TextView scoreLabel;
	private NumberFormat numFormat=NumberFormat.getInstance();
	private int phase;
	private TextView phaseLabel;
	private final int numPhases=8;
	private final int millisBeforeHighscore=500;
	private boolean sysUIvisible=true;
	private float soundVolume;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chimp);

		// Workaround hardware acceleration issue
		if(android.os.Build.VERSION.SDK_INT>=16) {
			turnonHWaccel();
		}
		
		SharedPreferences shrP=getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
		boolean soundEnabled=shrP.getBoolean(ConfigActivity.PrefsKeySound, ConfigActivity.defSound);
		soundVolume=soundEnabled?1.f:0.f;
		
		soundPool=new SoundPool(4, android.media.AudioManager.STREAM_MUSIC, 0);
		bubuSndId=soundPool.load(this,R.raw.bubu,1);
		clearSndId=soundPool.load(this,R.raw.clear,1);
		failSndId=soundPool.load(this,R.raw.fail,1);
		flipSndId=soundPool.load(this,R.raw.flip,1);
		pingpongSndId=soundPool.load(this,R.raw.pingpong,1);
		startSndId=soundPool.load(this,R.raw.start,1);
		
		numActivatedPanels=shrP.getInt(ConfigActivity.PrefsKeyNumPanels, numActivatedPanels);
		showMillis=shrP.getInt(ConfigActivity.PrefsKeyShowMillis, showMillis);
		blankMillis=shrP.getInt(ConfigActivity.PrefsKeyBlankMillis, blankMillis);
		
		r=new java.util.Random();
		handler=new Handler();
		timer=new TimerThread(handler,this);
		timer.start();
		scoreLabel=(TextView)findViewById(R.id.scoreLabel);
		phaseLabel=(TextView)findViewById(R.id.phaseLabel);
		touchScreenMessage=(TextView) findViewById(R.id.touchScreenView);
		gameView=(ChimpGameView) findViewById(R.id.gameView);
		gameView.onPanelTouch=this;
		if(savedInstanceState==null) {
			state=GameState.Init;
			chimpInit();
			transitionState(GameState.WaitStart);
		} else {
			state=(GameState)savedInstanceState.getSerializable(stateSaveLabel);
			cPanel=(ChimpPanel[])savedInstanceState.getSerializable(cPanelSaveLabel);
			gameView.setPanels(cPanel);
			score=savedInstanceState.getLong(scoreSaveLabel);
			updateScoreLabel();
			multiplier=savedInstanceState.getLong(multiplierSaveLabel);
			phase=savedInstanceState.getInt(phaseSaveLabel);
			updatePhaseLabel();
			gameView.setPanelVisibility(savedInstanceState.getBoolean(panelVisibilitySaveLabel));
			if(state==GameState.WaitStart) {
				touchScreenMessage.setVisibility(View.VISIBLE);
			}
		}
	}
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void turnonHWaccel() {
		getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
				android.view.WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
	}
	private void chimpInit() {
		for(int i=0; i<maxChimpPanels; i++) {
			cPanel[i]=new ChimpPanel(i);
		}
		gameView.setPanels(cPanel);
		score=0l;
		multiplier=calcFirstMultiplier();
		updateScoreLabel();
		phase=0;
	}
	private long calcFirstMultiplier() {
		float showTimeMp=((float)ConfigActivity.defShowMillis)/(1.f+(float)showMillis);
		float blankTimeMp=((float)blankMillis+1.f)/(float)ConfigActivity.defBlankMillis;
		long nPx=1l;
		for(long i=numActivatedPanels; i>1l; i--) { nPx=nPx*i; }
		float nPanelMp=(float)(nPx);
		float fMp=47.f*nPanelMp+showTimeMp*13.f+11.f*blankTimeMp;
		return (long)fMp;
	}
	private void stageInit() {
		int i;
		for(ChimpPanel cp: cPanel) {
			cp.enabled=false;
		}
		for(i=0; i<numActivatedPanels; i++) {
			ChimpPanel cp=cPanel[i];
			float top,left;
			int boobie_trap=64;
			do {
				top=r.nextFloat();
				left=r.nextFloat();
			} while(boobie_trap-->0 && !gameView.isSafeToPlace(top,left));
			cp.top=top;
			cp.left=left;
			cp.enabled=true;
			cp.flipped=true;
		}
		gameView.setPanelVisibility(false);
	}
	private void flipAll(boolean flip) {
		for(int i=0; i<numActivatedPanels; i++) {
			cPanel[i].flipped=flip;
		}
	}
	private void transitionState(GameState nextState) {
		if(state==GameState.WaitStart) {
			touchScreenMessage.setVisibility(View.INVISIBLE);
		}
		if(nextState==GameState.Show){
			gameView.setPanelVisibility(true);
			if(showMillis>0) {
				playSFX(startSndId);
				timer.enqueueTimer(showMillis);
			} else {
				transitionState(GameState.Blank);
				return;
			}
		} else if(nextState==GameState.Blank) {
			if(blankMillis>0) {
				gameView.setPanelVisibility(false);
				if(blankMillis>500) {
					playSFX(startSndId);
				}
				timer.enqueueTimer(blankMillis);
			} else {
				transitionState(GameState.AcceptSequence);
				return;
			}
		} else if(nextState==GameState.AcceptSequence) {
			flipAll(false);
			gameView.setPanelVisibility(true);
			playSFX(flipSndId);
			seqNumber=0;
		} else if(nextState==GameState.StageCleared) {
			timer.enqueueTimer(afterStageMillis);
		} else if(nextState==GameState.StageFailed){
			timer.enqueueTimer(afterStageMillis);
		} else if(nextState==GameState.WaitStart) {
			if(state==GameState.StageCleared) {
				playSFX(clearSndId);
				score+=numActivatedPanels*multiplier*53l;
				multiplier=multiplier*3l;
				updateScoreLabel();
			} else if(state==GameState.StageFailed) {
				playSFX(failSndId);
			}
			if(phase<numPhases) {
				phase++;
				updatePhaseLabel();
				stageInit();
				touchScreenMessage.setVisibility(View.VISIBLE);
			} else {
				gameView.setPanelVisibility(false);
				nextState=GameState.GameOver;
				timer.enqueueTimer(millisBeforeHighscore);
			}
		}
		state=nextState;
	}
	private void playSFX(int SFXid) {
		soundPool.play(SFXid, soundVolume, soundVolume, 1, 0, 1.f);
	}
	public boolean onPanelTouched(ChimpPanel panel) {
		if(sysUIvisible) {
			hideSystemUi();
		}
		if(state==GameState.AcceptSequence) {
			if(panel.num==seqNumber) {
				panel.flipped=!panel.flipped;
				gameView.invalidate();
				seqNumber++;
				playSFX(pingpongSndId);
				score+=(1+seqNumber)*multiplier;
				multiplier+=multiplier/10;
				if(seqNumber==numActivatedPanels) {
					transitionState(GameState.StageCleared);
				}
				updateScoreLabel();
			} else if(panel.num<seqNumber) {
				return false;
			} else {
				flipAll(true);
				gameView.invalidate();
				playSFX(bubuSndId);
				transitionState(GameState.StageFailed);
			}
			return true;
		} else if(state==GameState.Show) {
			if(panel.num==0) {
				timer.cancel();
				transitionState(GameState.AcceptSequence);
				panel.flipped=!panel.flipped;
				seqNumber++;
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(sysUIvisible) {
			hideSystemUi();
		}
		if(event.getAction()==MotionEvent.ACTION_DOWN) {
			if(state==GameState.WaitStart){
				transitionState(GameState.Show);
				return true;
			} else if(state==GameState.Show) {
				timer.cancel();
				transitionState(GameState.Blank);
				return true;
			} else {
				return super.onTouchEvent(event);
			}
		} else {
			return super.onTouchEvent(event);
		}
	}
	@Override
	public void onTimerFire() {
		if(state==GameState.Show) {
			transitionState(GameState.Blank);
		} else if(state==GameState.Blank) {
			transitionState(GameState.AcceptSequence);
		} else if(state==GameState.StageCleared) {
			transitionState(GameState.WaitStart);
		} else if(state==GameState.StageFailed) {
			transitionState(GameState.WaitStart);
		} else if(state==GameState.GameOver) {
			transitionToHighscoreScreen();
		}
	}
	private boolean isTimerActivatedInState(GameState state) {
		if(state==GameState.Show || state==GameState.Blank || state==GameState.StageCleared ||
				state==GameState.StageFailed || state==GameState.GameOver) {
			return true;
		} else {
			return false;
		}
	}
	@TargetApi(19)
	private void hideSystemUi() {
		int SDK_INT = android.os.Build.VERSION.SDK_INT;
		if(SDK_INT>=11) {
			View rootView=getWindow().getDecorView();
			if(SDK_INT >= 11 && SDK_INT < 14) {
				rootView.setSystemUiVisibility(View.STATUS_BAR_HIDDEN);
			} else if(SDK_INT >= 14 && SDK_INT < 19) {
				rootView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LOW_PROFILE);
			} else if(SDK_INT >= 19) {
				rootView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
						| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
						| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
			}
		}
		sysUIvisible=false;
	}
	@TargetApi(Build.VERSION_CODES.FROYO)
	@Override
	public void onPause() {
		if(android.os.Build.VERSION.SDK_INT>=8) {
			soundPool.autoPause();
		}
		if(isTimerActivatedInState(state)) {
			timer.cancel();
		}
		super.onPause();
	}
	@TargetApi(Build.VERSION_CODES.FROYO)
	@Override
	public void onResume() {
		super.onResume();
		if(android.os.Build.VERSION.SDK_INT>=8) {
			soundPool.autoResume();
		}
		if(isTimerActivatedInState(state)) {
			timer.enqueueTimer(1000); // tekitou
		}
		hideSystemUi();
		if(android.os.Build.VERSION.SDK_INT>=11) {
			registerUIChangeListener();
		}
	}
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void registerUIChangeListener() {
		View rootView = getWindow().getDecorView();
		rootView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
			@Override
			public void onSystemUiVisibilityChange(int visibility) {
				sysUIvisible=true;
			}
		});
	}
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putSerializable(cPanelSaveLabel, cPanel);
		savedInstanceState.putSerializable(stateSaveLabel, state);
		savedInstanceState.putLong(scoreSaveLabel, score);
		savedInstanceState.putLong(multiplierSaveLabel, multiplier);
		savedInstanceState.putInt(phaseSaveLabel, phase);
		savedInstanceState.putInt(seqNumSaveLabel, seqNumber);
		savedInstanceState.putBoolean(panelVisibilitySaveLabel, gameView.getPanelVisibility());
		super.onSaveInstanceState(savedInstanceState);
	}
	private void updateScoreLabel() {
		scoreLabel.setText(numFormat.format(score));
	}
	private void updatePhaseLabel() {
		phaseLabel.setText(numFormat.format(phase));
	}
	private void transitionToHighscoreScreen() {
		Intent intent=new Intent(this,HighscoreActivity.class);
		intent.putExtra("score", score);
		intent.putExtra("panels", numActivatedPanels);
		startActivity(intent);
		finish();
	}
	@Override
	public void onDestroy() {
		soundPool.release();
		soundPool=null;
		timer.enqueueTimer(-1);
		super.onDestroy();
	}
}
