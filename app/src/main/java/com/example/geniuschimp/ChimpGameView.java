package com.example.geniuschimp;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class ChimpGameView extends View {
	private Path rPath;
	private Paint rPaint;
	private Paint lPaint;
	//private Paint fPaint;
	private ChimpPanel[] panels;
	private final float panelSize=.16f;
	private final float labelOffset=.7f;
	private final float labelSize=.7f;
	private final float panelSize2=2.f*panelSize*panelSize;
	public PanelTouchListener onPanelTouch;
	private boolean panelVisibility=false;
	public ChimpGameView(Context context) {
		super(context);
		init();
	}
	public ChimpGameView(Context context, AttributeSet attrs) {
		super(context,attrs);
		init();
	}
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if(!panelVisibility || panels==null) { return; }
		float h=canvas.getHeight();
		float w=canvas.getWidth();
		float dim=panelSize*Math.min(h,w);
		lPaint.setTextSize(labelSize*dim);
		h-=dim;
		w-=dim;
		for(int i=0; i<panels.length; i++) {
			ChimpPanel p=panels[i];
			if(p.enabled){
				float top,left;
				top=p.top*h;
				left=p.left*w;
				if(p.flipped) {
					rPath.reset();
					rPath.moveTo(w*p.left, h*p.top);
					rPath.lineTo(w*p.left+dim, h*p.top);
					canvas.drawTextOnPath(p.label, rPath, 0.f, labelOffset*dim, lPaint);
					rPath.reset();
					rPath.addRect(left,top,dim+left,dim+top, Path.Direction.CCW);
					//canvas.drawPath(rPath,fPaint);
					//canvas.drawText(p.label, w*p.left, h*p.top+.5f*dim,lPaint);
				} else {
					rPath.reset();
					rPath.addRect(left,top,dim+left,dim+top, Path.Direction.CCW);
					canvas.drawPath(rPath,rPaint);
				}
			}
		}
	}
	private void init() {
		Resources r=getResources();
		rPath=new Path();
		rPaint=new Paint();
		rPaint.setColor(r.getColor(R.color.panelColor));
		rPaint.setAntiAlias(true);
		rPaint.setStyle(Style.FILL);
		lPaint=new Paint();
		lPaint.setColor(r.getColor(R.color.panelLabelColor));
		lPaint.setAntiAlias(true);
		lPaint.setTypeface(android.graphics.Typeface.SANS_SERIF);
		lPaint.setStyle(Style.FILL);
		lPaint.setTextAlign(Align.CENTER);
		//fPaint=new Paint(rPaint);
		//fPaint.setStyle(Style.STROKE);
	}
	public void setPanels(ChimpPanel[] panels) {
		this.panels=panels;
	}
	public interface PanelTouchListener {
		public boolean onPanelTouched(ChimpPanel panel);
	}
	@Override
	public boolean onTouchEvent(MotionEvent e) {
		boolean res;
		if(onPanelTouch==null) {
			res=false;
		} else if(e.getAction()!=MotionEvent.ACTION_DOWN) {
			res=false;
		} else {
			res=false;
			float x=e.getX();
			float y=e.getY();
			float w=this.getWidth();
			float h=this.getHeight();
			float dim=panelSize*Math.min(w, h);
			w-=dim;
			h-=dim;
			for(ChimpPanel p: panels) {
				float px=p.left*w;
				float py=p.top*h;
				float px2=px+dim;
				float py2=py+dim;
				if(px<x && x<px2 && py<y && y<py2) {
					res=onPanelTouch.onPanelTouched(p);
					if(res) {
						break;
					}
				}
			}
		}
		return res;
	}
	public boolean isSafeToPlace(float top, float left) {
		boolean result=true;
		for(ChimpPanel cp: panels) {
			if(cp.enabled) {
				float dy=(cp.top-top);
				float dx=(cp.left-left);
				float d2=dy*dy+dx*dx;
				if(d2<panelSize2) {
					result=false;
					break;
				}
			}
		}
		return result;
	}
	public void setPanelVisibility(boolean visibility) {
		this.panelVisibility=visibility;
		this.invalidate();
	}
	public boolean getPanelVisibility() {
		return this.panelVisibility;
	}
}
