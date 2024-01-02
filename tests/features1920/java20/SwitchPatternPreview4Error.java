/**
 * This used to work up to preview 3 (Java 19), but no longer in preview 4 (Java 20).
 * Now, the default case seems to need to be the last one in the list.
 */
public class SwitchPatternPreview4Error {
  static void defaultCanAppearBeforePattern(Integer i) {
    switch (i) {
      case null -> System.out.println("value unavailable: " + i);
      case -1, 1 -> System.out.println("absolute value 1: " + i);
      default -> System.out.println("other integer: " + i);
      case Integer value when value > 0 -> System.out.println("positive integer: " + i);
    }
  }

  static void defaultCanAppearBeforeNull(Integer i) {
    switch (i) {
      case -1, 1 -> System.out.println("absolute value 1: " + i);
      default -> System.out.println("other integer: " + i);
      case null -> System.out.println("value unavailable: " + i);
      case Integer value when value > 0 -> System.out.println("positive integer: " + i);
    }
  }

  static void defaultCanAppearBeforeConstantLabel(Integer i) {
    switch (i) {
      case null -> System.out.println("value unavailable: " + i);
      default -> System.out.println("other integer: " + i);
      case -1, 1 -> System.out.println("absolute value 1: " + i);
      case Integer value when value > 0 -> System.out.println("positive integer: " + i);
    }
  }
}
