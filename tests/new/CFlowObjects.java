import org.aspectj.testing.Tester;

public class CFlowObjects {
    public static void main(String[] args){
	new Test().go();
	Tester.checkEqual(Test.cflowObjects, 1, "1st cflow");
	Tester.checkEqual(Test.callsPerCFlow, 1, "1 call for each cflow");

	new Test().go();
	Tester.checkEqual(Test.cflowObjects, 2, "2nd cflow");
	Tester.checkEqual(Test.callsPerCFlow, 1, "1 call for each cflow");
    }
}

class Test {
    static int cflowObjects = 0;
    static int callsPerCFlow = 0;

    void go(){
	foo();
    }

    void foo(){}
}

aspect A percflow(target(Test) && call(void go())) {

    { Test.cflowObjects++; }
    { Test.callsPerCFlow = 0; }

    //before(): instanceof(Test) && calls(void Object.*()){ Test.callsPerCFlow++; }
    before(): within(Test) && call(void Object+.*(..)) {
        Test.callsPerCFlow++;
    }
}
