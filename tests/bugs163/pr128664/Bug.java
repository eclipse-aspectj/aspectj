import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class Bug {
	public static void main(String[] args) {		
        MonitorMyAnnotationExecution.aspectOf().executionCount = 0;
        new ClassImplementingInterfaceWithInheritedAnnotation();
        if (MonitorMyAnnotationExecution.aspectOf().executionCount!=1) {
        	throw new RuntimeException();
        }

        MonitorMyAnnotationExecution.aspectOf().executionCount = 0;
        new ClassExtendingClassWithInheritedAnnotation();
	// Expecting 2, one for derived and one for base class ctor execution
	if (MonitorMyAnnotationExecution.aspectOf().executionCount!=2) {
		throw new RuntimeException();
	}
    }
}

@Retention(RetentionPolicy.RUNTIME)
@Inherited
@interface InheritedAnnotation { }

@InheritedAnnotation
interface InterfaceWithInheritedAnnotation { }

@InheritedAnnotation class ClassWithInheritedAnnotation { }

class ClassImplementingInterfaceWithInheritedAnnotation implements InterfaceWithInheritedAnnotation { }

class ClassExtendingClassWithInheritedAnnotation extends ClassWithInheritedAnnotation { }

aspect MonitorMyAnnotationExecution {
        int executionCount;

        before() : execution((@InheritedAnnotation *)+.new(..)) && !within(Monitor*) && !within(Bug) {
                executionCount++;
        }
}
