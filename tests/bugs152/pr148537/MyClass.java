
import java.lang.annotation.*;

public class MyClass {

        @Retention({RetentionPolicy.RUNTIME})
        private @interface MyAnnotation {
        }
}