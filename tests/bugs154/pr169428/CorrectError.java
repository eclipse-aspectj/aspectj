import org.aspectj.lang.annotation.*;

class ClassMissingAspectAnnotation {

	@Before("execution(* *(..))")
	public void m() { }

}