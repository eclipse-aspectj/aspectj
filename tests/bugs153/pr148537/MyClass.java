import java.lang.annotation.*;

public class MyClass {

        @Retention({RententionPolicy.RUNTIME})
        private @interface MyAnnotation {
        }
}
