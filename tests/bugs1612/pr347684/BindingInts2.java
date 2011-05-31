import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@interface Foo {
  int i() default 37;
}


public class BindingInts2 {
  public static void main(String []argv) {
    BindingInts2 inst = new BindingInts2();
    inst.a();
    inst.b();
  }

  @Foo(i=99)
  void a() {}

  void b() {}
}

aspect X {
  before(int i): execution(* a(..)) && @annotation(Foo(i)) {
    System.out.println(thisJoinPointStaticPart+" "+i);
  }
}
