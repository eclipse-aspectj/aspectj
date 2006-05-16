package t;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class C2 {

	public InetAddress getAddress() throws UnknownHostException {
		return null;
	}
	
	private void test() throws UnknownHostException {
		System.out.println(getAddress().toString());
	}

	public static void main(String[] args) throws Exception {
		C2 c = new C2();
		c.test();
	}
}
