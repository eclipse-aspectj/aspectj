package weaved;

import lib.B;
import lib.C;

public class SuperC extends B {

	public SuperC(String s) {
		super(s);
	}
	
	public static void main(String[] args) {
		C c = new C("test");
		System.out.println("Is ["+C.class+"] subcass of ["+SuperC.class+"]? "+(SuperC.class.isAssignableFrom(c.getClass())));
	}

}
