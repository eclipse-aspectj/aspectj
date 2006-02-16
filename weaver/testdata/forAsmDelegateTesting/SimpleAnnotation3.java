import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
public @interface SimpleAnnotation3 {
  int id();
  String fruit() default "bananas";
}

