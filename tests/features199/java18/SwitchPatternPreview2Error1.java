/**
 * Inspired by examples in https://openjdk.java.net/jeps/420
 */
public class SwitchPatternPreview2Error1 {
  static void constantLabelsMustAppearBeforePatterns1(Integer i) {
    switch (i) {
      case null -> System.out.println("value unavailable: " + i);
      case Integer value && value > 0 -> System.out.println("positive integer: " + i);
      case -1, 1 -> System.out.println("absolute value 1: " + i);
      default -> System.out.println("other integer: " + i);
    }
  }
}
