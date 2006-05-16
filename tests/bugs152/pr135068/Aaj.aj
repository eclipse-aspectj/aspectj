import java.net.InetAddress;

public aspect Aaj {

	InetAddress around() throws java.net.UnknownHostException : call(public java.net.InetAddress C.getAddress() throws java.net.UnknownHostException) {
		return InetAddress.getLocalHost();
	}
}
