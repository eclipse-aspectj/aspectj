package com.kronos.code;

public class Processor3 extends Processor1 {

	@OkToIgnore
	public int u = 0;	// should pass it is marked as ok to ignore
	private int v = 0;	// should fail it has public accessor but is not processed
	private int w = 0;	// should pass it has public accessor and is processed
	public int x = 0;	// should pass it is public and is processed
	public int y = 0;	// should fail it is public and is not processed
	private int z = 0;	// should pass it is private and does not have a public accessor
	
	public void process() {
		int a = x;
		int b = w;
		super.process();
	}
	
	public int getW(){
		return w;
	}
	
	public int getV(){
		return v;
	}
}
