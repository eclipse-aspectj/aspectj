import java.io.*;
import org.aspectj.lang.*;

public class Privileged {
	public static void main(String[] args) {
		Privileged p = new Privileged();
		
		System.out.println("got: " + A.getX(p));
		System.out.println("s: " + s);
	}
	
	private int m() {
		return 2;
	}
	
	private int x = 1;
	
	private static String s = "hi";
}


privileged aspect A {
	static int getX(Privileged p) {
		Runnable r = new Runnable() {
			public void run() {
				Privileged.s += 10;
			}
		};
		r.run();
		return p.x + p.m();
	}
}
