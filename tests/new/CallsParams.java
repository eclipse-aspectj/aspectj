import org.aspectj.testing.Tester;
import java.util.*;

public class CallsParams {
    public static void main(String[] args) {
	Test t = new Test();
	t.go();

	//Tester.checkEqual(Test.calls, ", Test.go->Test.foo, Test.foo->java.io.PrintStream.println");
        Tester.checkEqual(Test.calls, ", Test.go->foo, Test.foo->println");
    }
}


class Test {
    static String calls = "";

    void go(){
	foo();
    }
    
    void foo(){
	System.out.println("");
    }
}

aspect SeqCut percflow(call(* Test.go(..))) {
    //before(Object s, Object r) : !instanceof(SeqCut) && instanceof(s) && calls(* r.*(..)) {
    before(Object s, Object r) : !this(SeqCut) && this(s) &&
        //callsto(receptions(* *(..)) && instanceof(r)) {
        call(* *(..)) && target(r) {
	Test.calls += ", " + s.getClass().getName() + "." + 
	    //thisJoinPoint.getEnclosingExecutionJoinPoint().getSignature().getName() + 
            thisEnclosingJoinPointStaticPart.getSignature().getName() + 
	    "->" /*+ r.getClass().getName() + "."*/ + thisJoinPoint.getSignature().getName();
        // IBM's VM doesn't have a java.io.PrintStream :)
    }

    before(Object s) : !this(SeqCut) && this(s) && call(* *..*.*(..)) {
        // no output
	//System.out.println(", " + s.getClass().getName() + "." + thisStaticJoinPoint.getSignature().getName());
    }
}

