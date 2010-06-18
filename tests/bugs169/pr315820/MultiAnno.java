import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@interface Foo {}

@Retention(RetentionPolicy.RUNTIME)
@interface Bar {
  String value() default "abc";
}

aspect A {
  declare @method: void MultiAnno.m(): @Foo @Bar;
  declare @field: int MultiAnno.i: @Bar @Foo;
  declare @type: MultiAnno: @Bar("ABC") @Foo;
  declare @constructor: MultiAnno.new(): @Foo @Bar("def");
}

public class MultiAnno {
  public MultiAnno() {}
  public int i;
  public static void main(String[]argv) throws Exception {
    System.out.println(MultiAnno.class.getDeclaredMethod("m").getAnnotations()[0]);
  }
  public void m() {
  }
}
