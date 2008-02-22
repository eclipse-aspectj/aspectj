package a;

import p.*;

public aspect EnumTest2 {
	public static void main(String[] argv) {
		
	}
	@TrafficLight(Color.RED) public void m() {}
	@TrafficLight(Color.GREEN) public void n() {}
	@TrafficLight public void o() {}
	
	before(): execution(@TrafficLight(p.Color.RED) * *(..)) {}; // referencing Color directly in package p
}