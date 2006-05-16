package t;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class C {

	public InetAddress getAddress() throws UnknownHostException {
		return null;
	}
	
	private void test() throws UnknownHostException {
		System.out.println(getAddress().toString());
	}

	public static void main(String[] args) throws Exception {
		C c = new C();
		c.test();
	}
}
