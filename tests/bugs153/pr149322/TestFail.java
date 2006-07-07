public class TestFail {

	public void invoke () {
		Interface i = new Missing();
		i.method();
		Missing cf = new Missing();
		cf.method();
	}
	
	public static void main(String[] args) {
		new TestFail().invoke();
	}

}
