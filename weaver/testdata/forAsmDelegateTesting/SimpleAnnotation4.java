import java.lang.annotation.*;

public @interface SimpleAnnotation4 {
  int id();
  String fruit() default "bananas";
}

