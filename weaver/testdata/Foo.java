import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
public @interface Foo {
  String color();
}
