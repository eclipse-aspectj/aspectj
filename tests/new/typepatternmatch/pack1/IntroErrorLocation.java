
package typepatternmatch.pack1;
import org.aspectj.testing.Tester;
import org.aspectj.testing.Tester; 

/**
 * FYI the compiler will not warn when a TypePattern matches no type/class.
 * From an email to user from Stefan
 */
public class IntroErrorLocation {
	/** change to true if the compiler ever should weave in by finding type? */
	public static volatile boolean EXPECT_INIT = false;
	/** signifies that the initialization advice was run */
	public static final String INIT_SIGNAL = "init";
	static {
		if (EXPECT_INIT) Tester.event(INIT_SIGNAL);
	}
    public static void main(String[] args) {
		typepatternmatch.pack2.TargetClass target 
			= new typepatternmatch.pack2.TargetClass();
		Tester.checkAllEvents();
    }
}

aspect MyIntroductionAspect {
	
	/** @testTarget typepattern.nonmatching.introduction.method */
	public String TargetClass.introMethod(String s) { // fails to match typepattern in other package
		return s;
	}
	/** @testTarget signature.nonmatching.advice.initialization */
	after (typepatternmatch.pack2.TargetClass c) 
		: initialization(TargetClass.new()) && this(c) { // fails to match signature in other package
		final String test = IntroErrorLocation.INIT_SIGNAL;
		if (IntroErrorLocation.EXPECT_INIT) {
			//Tester.event(c.introMethod(test)); // todo add positive: passed
		}
		// compiler error here is correct: no such method; no introMethod b/c TargetClass not matched
		Tester.checkEqual(test, c.introMethod(test), "Round trip failed"); // correct compiler error
	}
}
