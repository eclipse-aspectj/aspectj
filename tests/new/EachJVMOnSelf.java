import org.aspectj.testing.Tester;

public class EachJVMOnSelf {
    public static void main(String[] args) {
	new C();

	Tester.checkEqual(A.aspectOf().advisedNewClass, "C");
    }
}

aspect A issingleton() {
    String advisedNewClass = null;

    after () returning (): this(*) && execution(new(..)) && !this(A) {
		advisedNewClass = thisJoinPoint.getSourceLocation().getWithinType().getName();
    }
}

class C {
}
