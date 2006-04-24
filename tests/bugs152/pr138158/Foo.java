import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@interface Goo {}


public class Foo {
  public static void main(String []argv) {
    new Foo().m(); 
  }

  @Goo
  public void m() {
    System.err.println("");
  }
  
  public void m2() {
	System.err.println("");
  }
}

aspect X {
  before(): call(* println(..)) && !@withincode(Goo) {  }
  

  before(): call(* println(..)) && @withincode(Goo) {  }
}