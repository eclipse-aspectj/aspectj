import org.aspectj.lang.annotation.Aspect;
import java.lang.annotation.*;

public aspect SA {
	
	public static void main(String[] args) {
		Annotation[] annotations = SA.class.getAnnotations();
		if (annotations.length != 1) throw new RuntimeException("Should have one annotation but has "+annotations.length);
		Aspect aspectAnnotation = (Aspect) annotations[0];
System.out.println(aspectAnnotation);
		if (!aspectAnnotation.value().equals("")) throw new RuntimeException("value should be empty");
	}
	
}
