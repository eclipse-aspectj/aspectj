import java.lang.annotation.*;
import java.lang.reflect.*;

@Retention(RetentionPolicy.RUNTIME)
@interface SampleAnnotation { }

interface TestInterface { }

class Test implements TestInterface{}

// Second case: the ITD is annotated via a declare @field.
public aspect Declaration2 {

	declare @field: * TestInterface.secondProperty: @SampleAnnotation;
	
	// ITD directly on the implementor
    @SampleAnnotation 
	public String Test.firstProperty;
	
    // ITD on the interface
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