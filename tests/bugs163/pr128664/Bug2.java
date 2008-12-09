import java.lang.annotation.*;

public class Bug2 {
    public static void main(String[] args) {		
        MonitorMyAnnotationExecution.aspectOf().executionCount = 0;
        new ClassImplementingSubInterfaceWithInheritedAnnotation();
        if (MonitorMyAnnotationExecution.aspectOf().executionCount!=1) {
        	throw new RuntimeException();
        }
    }
}

@Retention(RetentionPolicy.RUNTIME)
@Inherited
@interface InheritedAnnotation { }

@InheritedAnnotation
interface InterfaceWithInheritedAnnotation { }

interface SubInterfaceOfInterfaceWithInheritedAnnotation extends InterfaceWithInheritedAnnotation {}

class ClassImplementingSubInterfaceWithInheritedAnnotation implements SubInterfaceOfInterfaceWithInheritedAnnotation { }

aspect MonitorMyAnnotationExecution {
        int executionCount;

        before() : execution((@InheritedAnnotation *)+.new(..)) && !within(Monitor*) && !within(Bug2) {
                executionCount++;
        }
}
