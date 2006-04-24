import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@interface Goo {}


public class Boo {
  public static void main(String []argv) {
    new Boo().m(); // advises here
  }

  @Goo
  public void m() {
    System.err.println("");
  }
}

aspect X {
  before(): call(* *(..)) && !@withincode(Goo) {
  }
}