package t;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class Ajava2 {

	@Around("call(public java.net.InetAddress t.C2.getAddress() throws java.net.UnknownHostException)")
	public static InetAddress getAddress() throws UnknownHostException {
		return InetAddress.getLocalHost();
	}
}
