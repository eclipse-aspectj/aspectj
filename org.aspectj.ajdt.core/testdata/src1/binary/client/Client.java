package client;

import lib.ConcreteA;
import org.aspectj.lang.*;

public class Client {
	public static void main(String[] args) {
		C c = new C();
		System.out.println(c.value);
		ConcreteA.Marker m = c;
		System.out.println(m.value);
		System.out.println(ConcreteA.getPrivateValue(c));
		try {
			new Client();
		} catch (SoftException se) {
			System.out.println("se: " + se);
		}
	}
	
	
	public Client() {
		foo();
	}
	
	private void foo() throws ConcreteA.MyException {
		throw new ConcreteA.MyException();
	}
}

class C implements ConcreteA.Marker { }