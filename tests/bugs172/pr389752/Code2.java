import java.lang.annotation.*;

enum Color { RED,GREEN,BLUE; }

@Retention(RetentionPolicy.RUNTIME)
@interface Foo {
  String s() default "xyz";
  Color color();
}


aspect Code2 {
  declare parents: (@Foo(color="AA") *) implements java.io.Serializable;
  // declare parents: (@Foo(color="AA", s="abc") *) implements java.io.Serializable;
}
