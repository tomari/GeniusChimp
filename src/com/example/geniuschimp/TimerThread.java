package com.example.geniuschimp;

import java.util.concurrent.LinkedBlockingQueue;

import android.os.Handler;

public class TimerThread extends Thread {
	private LinkedBlockingQueue<Integer> cmdQ;
	public interface TimerListener {
		public void onTimerFire();
	}
	private TimerListener listener=null;
	private Handler handler;
	protected TimerThread(Handler handler, TimerListener listener) {
		cmdQ=new LinkedBlockingQueue<Integer>();
		this.handler=handler;
		this.listener=listener;
	}
	@Override
	public void run() {
		int cmdVal=0;
		while(cmdVal>=0) {
			try {
				Integer cmd;
				cmd=cmdQ.take(); // This blocks when queue is empty
				cmdVal=cmd.intValue();
			} catch (InterruptedException e) {
				continue;
			}
			if(cmdVal<=0) { continue; }
			try {
				Thread.sleep(cmdVal);
			} catch (InterruptedException e) {
				continue;
			}
			handler.post(new Runnable() {
				@Override
				public void run() {
					listener.onTimerFire();
				}
			});
		}
	}
	// 
	public void enqueueTimer(int millis) {
		cmdQ.add(Integer.valueOf(millis));
	}
	public boolean isEmpty() {
		return cmdQ.isEmpty();
	}
	public void cancel() {
		this.interrupt();
	}
}
