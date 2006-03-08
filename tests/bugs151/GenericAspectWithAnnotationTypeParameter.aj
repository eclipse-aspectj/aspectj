import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class GenericAspectWithAnnotationTypeParameter {
	
	@AnnOne
	@AnnTwo
	public static void main(String[] args) {
		System.out.println("hello");
	}
	
	
}

@Retention(RetentionPolicy.RUNTIME)
@interface AnnOne {}
@interface AnnTwo {}

abstract aspect AnnotationMatcher<A extends Annotation> {
	
	before() : execution(* *(..)) && @annotation(A) {
		System.out.println("annotation match - no binding");
	}
	
	before() : execution(@A * *(..)) {
		System.out.println("execution with annotation match");
	}
	
	before(A anAnnotation) : execution(* *(..)) && @annotation(anAnnotation) {
		System.out.println("annotation match - binding");
	}
}

aspect AnnOneMatcher extends AnnotationMatcher<AnnOne> {}