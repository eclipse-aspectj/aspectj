// for Bug#:  31423  
import org.aspectj.testing.Tester;


public class AdviceExec {
    public static void main(String[] args) {
    	Tester.checkEqual(Aspect1.ran, 2, "Aspect1 ran");
    	Tester.checkEqual(Aspect2.ran, 2, "Aspect2 ran");
    }
}

aspect Aspect1 {
	static int ran = 0;
    before() : execution(* AdviceExec.*(..)) {
		//System.out.println("Reached " + thisJoinPoint);
		ran++;
    }
    
    void around(): execution(* AdviceExec.*(..)) {
    	ran++;
    	proceed();
    }
}

aspect Aspect2 {
	static int ran = 0;
    before() : adviceexecution() && !within(Aspect2) {
		//System.out.println("Reached " + thisJoinPoint);
		ran++;
    }
}
