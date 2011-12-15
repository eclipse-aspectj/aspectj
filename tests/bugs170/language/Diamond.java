import java.util.*;

public class Diamond {
  public static void main(String []argv) {
  }
}

aspect Foo {
  before(): execution(* *(..)) {
    // in advice
    List<String> ls = new ArrayList<>();
  }
  
}
