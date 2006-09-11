import java.util.List;

public class Test {

	public boolean method(final MyInterface iface) {
		for(final String s:iface.listFile()) if(s.equals("blah")) {
			System.out.println("Test.method()");
			continue;
		}
		return false;	
	}

	public void notCalledMethod() {
	}

}

interface MyInterface {

	public abstract List<String> listFile();
}

aspect MyAspect {

	pointcut p() : call(public * Test.notCalledMethod());

	before() : p() {
		System.out.println("calling method");
	}

}
