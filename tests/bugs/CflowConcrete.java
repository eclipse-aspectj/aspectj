// for Bug#:  31423  
import org.aspectj.testing.Tester;


public class CflowConcrete {
    public static void main(String[] args) {
    }
}

aspect TestAjc {
   pointcut notMySelf(): !within(TestAjc) && !cflow(within(TestAjc));

	pointcut      eachCall(): notMySelf() &&      call(* *.*(..));
	pointcut eachExecution(): notMySelf() && execution(* *.*(..)) ;

	before(): eachCall() { System.out.println(thisJoinPoint); }

	before(): eachExecution() { System.out.println(thisJoinPoint); }
} 
