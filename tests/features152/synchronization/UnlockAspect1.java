// to be LTW with BasicProgram1
import org.aspectj.lang.annotation.*;

public aspect UnlockAspect1 {
	@SuppressAjWarnings("adviceDidNotMatch")
	before(): unlock() {
		System.err.println("Unlock advice running at "+thisJoinPoint.getSourceLocation());
	}
}