import java.io.Serializable;

public class SuidTest implements Serializable {

	static final long serialVersionUID = 8904684881596717140L;
	private int i;
	
	public void run () {
		System.out.println("? SuidTest.run()");
	}
	
	public static void main(String[] args) throws Exception {
		SuidTest test = new SuidTest();
		Util.write(Util.DEFAULT_NAME,test);
	}
}