public class TestWithMissing {

	public void invoke () {
		Interface i = new Missing();
		i.interfaceMethod();
		Missing m = new Missing();
		m.interfaceMethod();
		m.missingMethod();
	}
	
	public static void main(String[] args) {
		new TestWithMissing().invoke();
	}

}
