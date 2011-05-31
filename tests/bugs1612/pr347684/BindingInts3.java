import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@interface Foo {
  String i() default "abc";
}


public class BindingInts3 {
  public static void main(String []argv) {
    BindingInts3 inst = new BindingInts3();
    inst.a();
    inst.b();
  }

  @Foo
  void a() {}

  void b() {}
}

aspect X {
  before(String i): execution(* a(..)) && @annotation(Foo(i)) {
    System.out.println(thisJoinPointStaticPart+" "+i);
  }
}
