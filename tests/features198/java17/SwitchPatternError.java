/**
 * Inspired by examples in https://openjdk.java.net/jeps/406
 */
public class SwitchPatternError {
  static void error(Object o) {
    switch(o) {
      case CharSequence cs ->
        System.out.println("A sequence of length " + cs.length());
      case String s ->    // Error - pattern is dominated by previous pattern
        System.out.println("A string: " + s);
      default -> {
          break;
      }
    }
  }
}
