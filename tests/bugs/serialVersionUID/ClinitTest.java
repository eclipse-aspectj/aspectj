import java.io.Serializable;

public class ClinitTest implements Serializable {

	private int i;
	private static boolean b = true;
	
	public void run () {
		System.out.println("? Clinit.run()");
	}
	
	public static void main(String[] args) throws Exception {
		ClinitTest test = new ClinitTest();
		Util.write(Util.DEFAULT_NAME,test);
	}
}