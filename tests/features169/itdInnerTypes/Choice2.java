import java.util.*;

public class Choice2 {
  public static void main(String []argv) {
    System.out.println(Bar.Keys.CHOICE);
  }
}

class Bar {

}

aspect X {
  static List<Bar> choices;
  public static List forClass() {
    ArrayList al = new ArrayList();
    return al;
  }
  public static class Bar.Keys {
    public static final Function<Object, Bar> CHOICE = Get.attrOf(choices,"choice");
  }
}

class Get {
  public static <T> Function<Object,T> attrOf(List<T> t,String s) {
    return null;
  }
}

class Function<A,B> {
  
}
