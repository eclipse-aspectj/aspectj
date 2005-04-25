import org.aspectj.lang.annotation.Aspect;
import java.lang.annotation.*;

public class Simple14AspectTest {

	public static void main(String[] args) {
		Annotation[] annotations = Simple14Aspect.class.getAnnotations();
		if (annotations.length != 0) throw new RuntimeException("Should have no annotations");
	}

}