// for Bug#:  33635  
import org.aspectj.testing.Tester;


public class NotIf {
    public static void main(String[] args) {
		Tester.checkEqual(Aspect1.ranNone, 0, "shouldn't run");
		Tester.checkEqual(Aspect1.ranTwo, 2, "should run");
		Tester.checkEqual(Aspect2.ran, 1, "should run with values");
    }
}

aspect Aspect1 {
	static int ranNone = 0;
	static int ranTwo = 0;
	
	static boolean testTrue() { return true; }
	
	static boolean testFalse() { return false; }
	
	before(): execution(void main(..)) && !if(testTrue()) {
		ranNone += 1;
	}
	
	before(): execution(void main(..)) && if(!testTrue()) {
		ranNone += 1;
	}
	before(): execution(void main(..)) && !if(testFalse()) {
		ranTwo += 1;
	}
	
	before(): execution(void main(..)) && if(!testFalse()) {
		ranTwo += 1;
	}
}

aspect Aspect2 {
	static int ran = 0;
	
	static boolean testValues(int i, String s, Object o) {
		return false;
	}
	
	before(String[] a): execution(void main(String[])) && 
				!if(testValues(a.length, a.toString(), a)) && args(a)
	{
		ran += 1;
	}
}
