import java.util.*;

public class FieldN {
  public static void main(String []argv) {
  }
}

interface I { }

aspect X {
  public List<Z> I<Z>.i; // error - the onType isn't generic!
}
