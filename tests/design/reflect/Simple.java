import org.aspectj.lang.reflect.*;

public class Simple {
    public static void main(String[] args) {
	new Simple().foo("hi");
    }

    void foo(String s) {
	System.out.println("foo(" + s + ")");
    }

    char ch = 'a';
    int i = 0;
}

aspect A {
    before(): execution(* *.*(..)) {
	System.out.println(thisJoinPoint+ ", " + thisEnclosingJoinPointStaticPart);
    }
    before(): call(* *.*(..)) && !within(A) {
    	System.out.println("call: " + thisJoinPoint.getThis()+ ", " + thisEnclosingJoinPointStaticPart);
    }

    before(): set(* Simple.*) {
	//Object old = ((FieldAccessJoinPoint)thisJoinPoint).getValue();
	System.out.println(thisJoinPoint +", " + thisJoinPoint.getArgs());
    }
}
