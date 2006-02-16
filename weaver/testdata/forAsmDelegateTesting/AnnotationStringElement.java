import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
public @interface AnnotationStringElement {
  String stringval();
}
