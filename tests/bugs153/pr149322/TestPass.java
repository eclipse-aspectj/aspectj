
public class TestPass {

	public void invoke () {
		Interface i = new CantFind();
		i.method();
	}
	
	public static void main(String[] args) {
		new TestFail();
	}

}
