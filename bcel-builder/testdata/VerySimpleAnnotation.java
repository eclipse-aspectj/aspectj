import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
public @interface VerySimpleAnnotation {
  int id();
}
