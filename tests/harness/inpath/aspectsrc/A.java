
import org.aspectj.testing.Tester;

public aspect A {
	static {
		Tester.expectEvents(
		new String[] {
            "execution(void Main.main(String[]))",
            "execution(void pack.Util.log(String[]))"
	        }
		);
	}
	before() : execution(public static * *(..)) {
		Tester.event("" + thisJoinPointStaticPart);
		//System.out.println("\"" + thisJoinPointStaticPart);
	}
	after() returning : execution(public static void main(String[])) {
		Tester.checkAllEvents();
	}
}
