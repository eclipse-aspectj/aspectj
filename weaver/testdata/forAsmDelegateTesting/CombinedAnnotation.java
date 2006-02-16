import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
public @interface CombinedAnnotation {
 public SimpleAnnotation[] value();
}
