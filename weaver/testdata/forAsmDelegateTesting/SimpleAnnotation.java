import java.lang.annotation.*;

@Retention(RetentionPolicy.SOURCE)
public @interface SimpleAnnotation {
  int id();
  String fruit() default "bananas";
}
