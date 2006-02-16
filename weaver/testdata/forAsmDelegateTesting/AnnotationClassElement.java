import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
public @interface AnnotationClassElement {
  Class clz();
}
