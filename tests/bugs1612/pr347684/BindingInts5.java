import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@interface Foo {
  String s() default "abc";
  int i() default 37;
}


public class BindingInts5 {
  public static void main(String []argv) {
    BindingInts5 inst = new BindingInts5();
    inst.a();
    inst.b();
  }
@Foo
void a() {}
void b() {}
}

aspect X {
  before(int i): execution(* a(..)) && @annotation(Foo(i)) {
    System.out.println(thisJoinPointStaticPart+" "+i);
  }
}
