import org.aspectj.lang.annotation.Aspect;
import java.lang.annotation.*;

public aspect PerTargetAspect pertarget(fooCall()) {
	
	pointcut fooCall() : call(* Foo.foo(..));
	
	public static void main(String[] args) {
		Annotation[] annotations = PerTargetAspect.class.getAnnotations();
		if (annotations.length != 1) throw new RuntimeException("Should have one annotation");
		Aspect aspectAnnotation = (Aspect) annotations[0];
		if (!aspectAnnotation.value().equals("pertarget(fooCall())")) throw new RuntimeException("value should be equal to perclause");
	}
	
}

class Foo {
	
	void foo() {}
	
}