package client;

import lib.AbstractA;
import org.aspectj.lang.*;

public class Client1 {
	public static void main(String[] args) {
		C1 c = new C1();
		System.out.println(c.value);
		AbstractA.Marker m = c;
		System.out.println(m.value);
		System.out.println(AbstractA.getPrivateValue(c));
		
		FooMarkMe f = new FooMarkMe();
		System.out.println(f.value);
		
		m = f;
	}	

}

class C1 implements AbstractA.Marker {
	public void m() {
		System.out.println("hello");
	}
}

class FooMarkMe {
	public void m() {
		System.out.println("hello");
	}
}
