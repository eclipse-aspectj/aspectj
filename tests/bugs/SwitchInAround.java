import org.aspectj.testing.Tester;

public class SwitchInAround {
	public static void main(String[] args) {
		SwitchInAround o = new SwitchInAround();
		Tester.checkEqual(o.doit(1), "1");
		Tester.checkEqual(o.doit(2), "2");
		Tester.checkEqual(o.doit(3), "default");
	}
	
	public String doit(int i) {
		return "doit";
	}
}

privileged aspect A {
	String around(int index): args(index) && call(String doit(int)) {
		switch(index) {
			case 1:
				return "1";
			case 2:
				return "2";
			default:
				return "default";
		}
	}
}