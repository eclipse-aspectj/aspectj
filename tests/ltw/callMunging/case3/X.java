import java.io.Serializable;

public aspect X {
  declare parents: T implements Serializable;

  before(): call(* Serializable+.m*(..)) {
	 System.out.println("advice running");
  }
}
