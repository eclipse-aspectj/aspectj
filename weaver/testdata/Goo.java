import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
public @interface Goo {
  String weather();
}
