import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
public @interface AnnotationEnumElement {
  SimpleEnum enumval();
}
