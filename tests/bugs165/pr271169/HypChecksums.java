public class HypChecksums {
 
  static class Adler {
    long combine(long a, long b, long c, long d) {
      return 3;
    }
  } 

  public static void main(final String[] pArgs) {
    Adler comb = new Adler();
    comb.combine(4, 2, 3, 3);
  }
}

aspect X { 
	Object around(): call(* combine(..)) && !within(X) {
		return proceed();
	}
}
