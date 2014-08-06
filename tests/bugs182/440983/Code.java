import java.util.*;
import java.lang.annotation.*;

@Target(ElementType.TYPE_USE)
@Retention(RetentionPolicy.CLASS)
@interface Anno {}

public class Code {
  public static void xxx(String []argv) {
    List<@Anno String> ls = new ArrayList<String>();
    System.out.println(ls);
  }

  public static void yyy(String []argv) {
  }

  public static void main(String []argv) {
    Code c = new Code();
    c.xxx(argv);
    System.out.println("works");
  }
}

//aspect X {
//  before(): execution(* main(..)) {}
//}
