import java.util.*;

public class MethodN {
  public static void main(String []argv) {
  }
}

interface I { }

aspect X {
  public List<Z> I<Z>.m() {}; // error - the onType isn't generic!
}
