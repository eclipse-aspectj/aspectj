import java.lang.annotation.*;

@Blah
public class Code {
  public static void main(String[] argv) {
    System.out.println("abcde");
  }
}

@Retention(RetentionPolicy.RUNTIME)
@interface Blah {}
