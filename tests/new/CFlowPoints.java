import org.aspectj.testing.Tester;

public class CFlowPoints {
    public static void main(String[] args){
	new Test().go(2);
	Tester.checkEqual(Test.callsPerCFlow, 2+1, "3 call for each cflow");

	Test.callsPerCFlow = 0;

	new Test().go(3);
	Tester.checkEqual(Test.callsPerCFlow, 3+2+1, "6 call for each cflow");

	try {
	    Each.aspectOf();
	    Tester.checkFailed("should have thrown exception");
	} catch (org.aspectj.lang.NoAspectBoundException exc) {
	    // this is what we want
	}
    }
}

class Test {
    static int cflowObjects = 0;
    static int callsPerCFlow = 0;

    Object lastEachCFlow = null;

    void go(int n) {
	for (int i=0; i<n; i++) foo("i", "i");

	if (n >= 0) go(n-1);

	Tester.check(Each.aspectOf() != lastEachCFlow, "unique eachcflows");

	lastEachCFlow = Each.aspectOf();
    }

    void foo(String s1, String s2){}
}

aspect A {
    pointcut root(int x): target(Test) && call(void go(int)) && args(x);

    pointcut flow(int x): cflow(root(x));

    before(): root(int) && !cflow(root(int)) {
	Tester.checkEqual(Test.callsPerCFlow, 0, "top call");
    }

    before(String s1, int y, String s2):
	flow(y) && target(Test) && target(Object)
        && call(void *(String,String))
        && args(s1,s2)
    {
        Test.callsPerCFlow++;
	Tester.checkEqual(s1, s2, "extra parameters");
    }
}

aspect Each percflow(flowCut()) {
    pointcut flowCut(): target(Test) && call(void go(int));
}
