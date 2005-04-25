import org.aspectj.lang.annotation.Aspect;
import java.lang.annotation.*;
@Aspect
public class SimpleAnnotatedAspect {
	
	public static void main(String[] args) {
		Annotation[] annotations = SimpleAnnotatedAspect.class.getAnnotations();
		if (annotations.length != 1) throw new RuntimeException("Should have one annotation");
		Aspect aspectAnnotation = (Aspect) annotations[0];
		if (!aspectAnnotation.value().equals("")) throw new RuntimeException("value should be empty");
	}
	
}