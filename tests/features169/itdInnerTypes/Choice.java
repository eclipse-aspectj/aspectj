import java.util.*;

public class Choice {
  public static void main(String []argv) {
    System.out.println(Keys.CHOICE);
  }
}

aspect X {
  static List<Choice> choices;
  public static List forClass() {
    ArrayList al = new ArrayList();
    return al;
  }
  public static class Choice.Keys {
    public static final Function<Object, Choice> CHOICE = Get.attrOf(choices,"choice");
  }
}

class Get {
  public static <T> Function<Object,T> attrOf(List<T> t,String s) {
    return null;
  }
}

class Function<A,B> {
  
}
