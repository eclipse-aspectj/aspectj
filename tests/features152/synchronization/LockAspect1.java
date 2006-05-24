// to be LTW with BasicProgram1
import org.aspectj.lang.annotation.*;

public aspect LockAspect1 {
	@SuppressAjWarnings("adviceDidNotMatch")
	before(): lock() {
		System.err.println("Lock advice running at "+thisJoinPoint.getSourceLocation());
	}
}