/**
 * Inspired by examples in https://openjdk.java.net/jeps/420
 */
public class SwitchPatternPreview3Error2 {
  static void constantLabelsMustAppearBeforePatterns2(Object o) {
    switch (o) {
      case null -> System.out.println("value unavailable: " + o);
      // This seems to be a bug in JEP 420 implementation. Those constants should be compatible with 'Object'.
      // case -1, 1 -> System.out.println("absolute value 1: " + o);
      // case "hello" -> System.out.println("string value: " + o);

      // 'Integer value' dominates the next two, more specific ones -> error
      case Integer value -> System.out.println("other integer: " + o);
      case Integer value when (value == 1 || value == -1) -> System.out.println("absolute value 1: " + o);
      case Integer value when value > 0 -> System.out.println("positive integer: " + o);

      case String value when value.startsWith("hello") -> System.out.println("greeting: " + o);
      default -> System.out.println("other type: " + o);
    }
  }
}
