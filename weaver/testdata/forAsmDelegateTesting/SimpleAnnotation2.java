import java.lang.annotation.*;

@Retention(RetentionPolicy.CLASS)
public @interface SimpleAnnotation2 {
  int id();
  String fruit() default "bananas";
}

