import org.aspectj.lang.annotation.Aspect;
import java.lang.annotation.*;

public aspect PerCflowAspect percflow(execution(* Foo.foo(..))) {
	
	public static void main(String[] args) {
		Annotation[] annotations = PerCflowAspect.class.getAnnotations();
		if (annotations.length != 1) throw new RuntimeException("Should have one annotation");
		Aspect aspectAnnotation = (Aspect) annotations[0];
		if (!aspectAnnotation.value().equals("percflow(execution(* Foo.foo(..)))")) 
			throw new RuntimeException("value should be equal to perclause but was: " + aspectAnnotation.value());
	}
	
}

class Foo {
	
	void foo() {}
	
}