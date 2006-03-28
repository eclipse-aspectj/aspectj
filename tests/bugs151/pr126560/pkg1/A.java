package pkg1;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.DeclareWarning;
import org.aspectj.lang.annotation.DeclareError;

@Aspect
public class A {

	@DeclareWarning("execution(* C.warningMethod())")
    static final String warning = "warning";

    @DeclareError("execution(* C.badMethod())")
    static final String error = "error";
	
}
