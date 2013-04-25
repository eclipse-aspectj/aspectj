import java.lang.annotation.*;
@Retention(RetentionPolicy.RUNTIME)
public @interface Gimme {
  Class<?> value();
}

