import org.aspectj.lang.annotation.Aspect;
import org.aspectj.internal.lang.annotation.ajcPrivileged;
import java.lang.annotation.*;

public privileged aspect PrivilegedAspect {
	
	public static void main(String[] args) {
		Annotation[] annotations = PrivilegedAspect.class.getAnnotations();
		if (annotations.length != 2) throw new RuntimeException("Should have two annotations");
		Aspect aspectAnnotation = PrivilegedAspect.class.getAnnotation(Aspect.class);
		if (!aspectAnnotation.value().equals("")) throw new RuntimeException("value should be empty");
		ajcPrivileged pAnnotation = PrivilegedAspect.class.getAnnotation(ajcPrivileged.class);
		if (pAnnotation == null) throw new RuntimeException("Should be @ajcPrivileged");
	}
	
}