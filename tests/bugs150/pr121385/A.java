import org.aspectj.lang.annotation.*;

 abstract aspect X {
  void around(): execution(* foo(..)) {}
}


@Aspect class B extends X { }

public class A {
  public void foo() { } 

public static void main(String []argv) {
   new A().foo();
}
}
