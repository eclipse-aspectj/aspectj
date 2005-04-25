import org.aspectj.lang.annotation.Aspect;
import java.lang.annotation.*;

public aspect PerTypeWithinAspect pertypewithin(javax.ejb..*) {
	
	public static void main(String[] args) {
		Annotation[] annotations = PerTypeWithinAspect.class.getAnnotations();
		if (annotations.length != 1) throw new RuntimeException("Should have one annotation");
		Aspect aspectAnnotation = (Aspect) annotations[0];
		if (!aspectAnnotation.value().equals("pertypewithin(javax.ejb..*)")) throw new RuntimeException("value should be equal to perclause");
	}
	
}
