import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@interface Foo {}

@Retention(RetentionPolicy.RUNTIME)
@interface Bar {
  String value() default "abc";
}

@Retention(RetentionPolicy.RUNTIME)
@interface Goo {
  int value() default 44;
}

aspect A {
//  declare @method: void MultiAnno.m(): @Foo @Bar;
  declare @field: int MultiAnno2.i: @Bar("XYCZ") @Foo @Goo(23);
//  declare @type: MultiAnno: @Bar("ABC") @Foo;
//  declare @constructor: MultiAnno.new(): @Foo @Bar("def");
}

public class MultiAnno2 {
  public MultiAnno2() {}
  public int i;
  public static void main(String[]argv) throws Exception {
    System.out.println(MultiAnno2.class.getDeclaredField("i").getAnnotations()[0]);
    System.out.println(MultiAnno2.class.getDeclaredField("i").getAnnotations()[1]);
  }
  public void m() {
  }
}
