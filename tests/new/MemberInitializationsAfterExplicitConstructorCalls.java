
import org.aspectj.testing.Tester;

/**
 * PR#476:
 * Member initializations are run after explicit 
 * constructor calls ("this()") when they should be run beforehand.
 * The following program would produce a NullPointerException because
 * 'member' is set to null *after* the explicit constructor sets it 
 * to "correctValue".
 */          
public class MemberInitializationsAfterExplicitConstructorCalls {
	public static void main(String[] args) {
		// passes - no constructor call to this
		ThisCall thisCall = new ThisCall("foo");
		thisCall.go();
		// fails - constructor call to this
		thisCall = new ThisCall();
		thisCall.go();
	}

	static class ThisCall {
		String init = "INIT";
		String member = null;
		/** set init and member to input */
		ThisCall(String input) { 
			this.init = input; 
			this.member = input; 
		}
		ThisCall() { 
			this("correctValue"); 
			Tester.check(!"INIT".equals(init), "String constructor: !\"INIT\".equals(init)");
			Tester.check(null != member, "String constructor: null != member");
			// get NPE here if using member
		}
		void go() { 
			Tester.check(!"INIT".equals(init), "instance method: !\"INIT\".equals(init)");
			Tester.check(null != member, "instance method: null != member");
			// get NPE here if using member
		}
	}
}
