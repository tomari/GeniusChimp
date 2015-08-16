package com.example.geniuschimp;

import java.io.Serializable;

public class ChimpPanel implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 9164220742034959673L;
	public final String label;
	public final int num;
	public boolean flipped=false;
	public boolean enabled=false;
	public float top=0.f,left=0.f;
	protected ChimpPanel(int num) {
		this.num=num;
		this.label=String.valueOf(num+1);
	}
}
