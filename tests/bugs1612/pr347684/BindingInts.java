import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@interface Foo {
  int i() default 37;
}


public class BindingInts {
  public static void main(String []argv) {
    BindingInts inst = new BindingInts();
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
