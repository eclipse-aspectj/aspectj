public class Code {
	public static void main(String[] argv) {
	}

  static aspect X {
    before(): execution(* Code.main(..)) {
	System.out.println(
"""
This
is
on
multiple
lines
"""
);
    }
  }

}
