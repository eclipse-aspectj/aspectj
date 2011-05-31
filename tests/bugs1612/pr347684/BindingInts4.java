import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@interface Foo {
  int i() default 37;
  int j() default 48;
}


public class BindingInts4 {
  public static void main(String []argv) {
    BindingInts4 inst = new BindingInts4();
    inst.a();
    inst.b();
  }
@Foo
void a() {}
void b() {}
}

aspect X {
  before(int i,int j): execution(* a(..)) && @annotation(Foo(i)) && @annotation(Foo(j)) {
    System.out.println(thisJoinPointStaticPart+" "+i+" "+j);
  }
}
