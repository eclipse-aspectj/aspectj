import java.io.Serializable;

public class Test implements Serializable {

	private int i;

	public void run () {
		System.out.println("Test.run()");
		i++;
	}
	
	public static void main (String[] args) throws Exception {
		Test test = new Test();
		test.run();
		Util.write(Util.DEFAULT_NAME,test);
	}
}
