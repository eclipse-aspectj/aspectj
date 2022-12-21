/**
 * This was not working as expected in ECJ after the initial Java 19 merge the JDT Core main line,
 * see https://github.com/eclipse-jdt/eclipse.jdt.core/issues/450.
 */
public class RecordPatternsPreview1Error {
  public static void main(String[] args) {
    erroneousTest1(new Box<>("A"));
    erroneousTest2(new Box<>("B"));
  }

  static void erroneousTest1(Box<Object> bo) {
    if (bo instanceof Box(var s)) {     // Javac error: raw deconstruction patterns are not allowed
      System.out.println("I'm a box");
    }
  }

  static void erroneousTest2(Box b) {
    if (b instanceof Box(var t)) {      // Javac error: raw deconstruction patterns are not allowed
      System.out.println("I'm a box");
    }
  }
}

record Box<T>(T t) {}
