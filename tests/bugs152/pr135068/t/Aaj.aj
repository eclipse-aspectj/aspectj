package t;

import java.net.InetAddress;

public aspect Aaj {

	InetAddress around() throws java.net.UnknownHostException : call(public java.net.InetAddress t.C.getAddress() throws java.net.UnknownHostException) {
		return InetAddress.getLocalHost();
	}
}
