/**
 * Inspired by examples in https://openjdk.java.net/jeps/420
 */
public class SwitchPatternPreview2Error {
  static void constantLabelMustAppearBeforePattern1(Object o) {
    switch (o) {
      case Integer i && i > 0 -> System.out.println("positive integer: " + o);
      case -1, 1 -> System.out.println("special case:" + o);
      case Integer i -> System.out.println("other integer: " + o);
      default -> System.out.println("non-integer: " + o);
    }
  }

  static void constantLabelMustAppearBeforePattern2(Object o) {
    switch (o) {
      case -1, 1 -> System.out.println("special case:" + o);
      case Integer i -> System.out.println("other integer: " + o);
      case Integer i && i > 0 -> System.out.println("positive integer: " + o);
      default -> System.out.println("non-integer: " + o);
    }
  }

  static void constantLabelMustAppearBeforePattern3(Object o) {
    switch (o) {
      case Integer i && i > 0 -> System.out.println("positive integer: " + o);
      case Integer i -> System.out.println("other integer: " + o);
      case -1, 1 -> System.out.println("special case:" + o);
      default -> System.out.println("non-integer: " + o);
    }
  }
}
