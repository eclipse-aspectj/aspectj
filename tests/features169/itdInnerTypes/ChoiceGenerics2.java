import java.util.*;

public class ChoiceGenerics2 {
  public static void main(String []argv) {
    new Keys<Integer>().choice = "abc";
  }
}


aspect X {
  public static class ChoiceGenerics2.Keys<N> {
    public N choice = null;
  }
}
