import java.lang.annotation.*;
import java.lang.reflect.*;

@Retention(RetentionPolicy.RUNTIME)
@interface SampleAnnotation { }

interface TestInterface { }

class Test implements TestInterface{}

// First case: the ITD on the interface is annotated, it should make it through 
// to the member added to the implementor
public aspect Declaration1 {

	// ITD directly on the implementor
    @SampleAnnotation 
	public String Test.firstProperty;
	
    // ITD on the interface
	@SampleAnnotation
	public String TestInterface.secondProperty;

	public static void main(String[] args) {
		for (Field field: Test.class.getFields()) {
			StringBuffer sb = new StringBuffer();
			sb.append(field.toString());
			boolean b = field.isAnnotationPresent(SampleAnnotation.class);
			sb.append(" has annotation:").append(b);
			System.out.println(sb.toString());
		}
	}
}