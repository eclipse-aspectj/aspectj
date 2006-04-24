import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@interface Goo {}


@Goo
public class Doo {
  public static void main(String []argv) {
    new Doo().m(); // advises here
  }

  public void m() {
    System.err.println("");
  }
}

class Soo {
	public void m() {
	    System.err.println("");
	}
}

aspect X {
  before(): call(* println(..)) && !@within(Goo) {
  }
}