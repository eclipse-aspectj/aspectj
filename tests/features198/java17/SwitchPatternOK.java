import java.util.List;

/**
 * Inspired by examples in https://openjdk.java.net/jeps/406
 */
public class SwitchPatternOK {
  public static void main(String[] args) {
    System.out.println(formatterPatternSwitch(null));
    System.out.println(formatterPatternSwitch(123));
    System.out.println(formatterPatternSwitch(999L));
    System.out.println(formatterPatternSwitch(12.34));
    System.out.println(formatterPatternSwitch("foo"));
    System.out.println(formatterPatternSwitch(List.of(123, "foo", 999L, 12.34)));

    System.out.println(testCircle(new Rectangle()));
    System.out.println(testCircle(new Circle(5)));
    System.out.println(testCircle(new Circle(6)));

    System.out.println(testSealedCoverage(new A()));
    System.out.println(testSealedCoverage(new B()));
    System.out.println(testSealedCoverage(new C(5)));
  }

  static String formatterPatternSwitch(Object o) {
    return switch (o) {
      case null      -> "null";
      case Integer i -> String.format("int %d", i);
      case Long l    -> String.format("long %d", l);
      case Double d  -> String.format("double %f", d);
      case String s  -> String.format("String %s", s);
      default        -> o.toString();
    };
  }

  static class Shape {}
  static class Rectangle extends Shape {}
  static class Circle extends Shape {
    private final double radius;
    public Circle(double radius) { this.radius = radius; }
    double calculateArea() { return Math.PI * radius * radius; }
  }

  static String testCircle(Shape s) {
    return switch (s) {
      case Circle c && (c.calculateArea() > 100) -> "Large circle";
      case Circle c -> "Small circle";
      default -> "Non-circle";
    };
  }

  sealed interface S permits A, B, C {}
  final static class A implements S {}
  final static class B implements S {}
  static record C(int i) implements S {}  // Implicitly final

  static String testSealedCoverage(S s) {
    return switch (s) {
      case A a -> "Sealed sub-class A";
      case B b -> "Sealed sub-class B";
      case C c -> "Sealed sub-record C";
    };
  }
}
