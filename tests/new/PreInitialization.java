import org.aspectj.testing.Tester;

public class PreInitialization {
    PreInitialization() {
	this(PreInitialization.interestingCall());
    }
    PreInitialization(int ignored) {
    }

    public static void main(String[] args) {
	new PreInitialization();
        Tester.checkEvents(new String[] {"before advice ran"});
    }
    static int interestingCall() {
	// do something interesting
	return 3;
    }
}

aspect A {
    before(): call(int PreInitialization.interestingCall()) {
	Tester.checkEqual(thisEnclosingJoinPointStaticPart.getKind(),
			  "preinitialization");
        Tester.event("before advice ran");
    }
}
