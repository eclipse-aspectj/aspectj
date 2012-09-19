import java.lang.annotation.*;

enum Color { RED,GREEN,BLUE; }

@Retention(RetentionPolicy.RUNTIME)
@interface Foo {
  String s() default "xyz";
  Color color();
}


aspect Code {
  declare parents: (@Foo(s="abc",color="AA") *) implements java.io.Serializable;
  // declare parents: (@Foo(color="AA", s="abc") *) implements java.io.Serializable;
}
