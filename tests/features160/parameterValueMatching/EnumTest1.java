package a;

import p.*;

public aspect EnumTest1 {
	public static void main(String[] argv) {
		
	}
	@TrafficLight(Color.RED) public void m() {}
	@TrafficLight(Color.GREEN) public void n() {}
	@TrafficLight public void o() {}
	
	before(): execution(@TrafficLight(Color.RED) * *(..)) {} // referencing Color via import of p.*
}