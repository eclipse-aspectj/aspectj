import java.util.*;

public class ChoiceGenerics1 {
  public static void main(String []argv) {
    new Keys<Integer>().choice = 42;
  }
}


aspect X {
  public static class ChoiceGenerics1.Keys<N> {
    public N choice = null;
  }
}
