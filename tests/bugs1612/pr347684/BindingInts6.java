import java.lang.annotation.*;

enum Color { R,G,B; }

@Retention(RetentionPolicy.RUNTIME)
@interface Foo {
  String s() default "abc";
  int i() default 37;
  Color c() default Color.G;
  int j() default 21;
  int k() default 101;
  float f() default 1.0f;
}


public class BindingInts6 {
  public static void main(String []argv) {
    BindingInts6 inst = new BindingInts6();
    inst.a();
    inst.b();
  }
@Foo(j=1,k=99)
void a() {}
void b() {}
}

aspect X {
  before(int i,int j, int k): execution(* a(..)) && @annotation(Foo(i)) && @annotation(Foo(j)) && @annotation(Foo(k)) {
    System.out.println(thisJoinPointStaticPart+" "+i+" "+j+" "+k);
  }
}
