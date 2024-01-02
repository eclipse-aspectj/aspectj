import java.util.List;
import java.util.Locale;

aspect SwitchPatternPreview3Aspect {
  Object around(Object o) : execution(* doSomethingWithObject(*)) && args(o) {
    System.out.println(switch (o) {
      case null      -> "null";
      case Integer i -> String.format("int %d", i);
      case Long l    -> String.format("long %d", l);
      case Double d  -> String.format(Locale.ENGLISH, "double %f", d);
      case String s  -> String.format("String %s", s);
      default        -> o.toString();
    });
    return proceed(o);
  }

  before(Shape s) : execution(* doSomethingWithShape(*)) && args(s) {
    System.out.println(switch (s) {
      case Circle c when (c.calculateArea() > 100) -> "Large circle";
      case Circle c -> "Small circle";
      default -> "Non-circle";
    });
  }

  after(S s) : execution(* doSomethingWithSealedClass(*)) && args(s) {
    System.out.println(switch (s) {
      case A a -> "Sealed sub-class A";
      case B b -> "Sealed sub-class B";
      case C c -> "Sealed sub-record C";
    });
  }

  Object around(Integer i): execution(* doSomethingWithInteger(*)) && args(i) {
    System.out.println(
      switch (i) {
        case null -> "value unavailable: " + i;
        case -1, 1 -> "absolute value 1: " + i;
        case Integer value when value > 0 -> "positive integer: " + i;
        default -> "other integer: " + i;
      }
    );
    return proceed(i);
  }
}

class Shape {}
class Rectangle extends Shape {}
class Circle extends Shape {
  private final double radius;
  public Circle(double radius) { this.radius = radius; }
  double calculateArea() { return Math.PI * radius * radius; }
}

sealed interface S permits A, B, C {}
final class A implements S {}
final class B implements S {}
record C(int i) implements S {}  // Implicitly final

class Application {
  public static void main(String[] args) {
    doSomethingWithObject(null);
    doSomethingWithObject(123);
    doSomethingWithObject(999L);
    doSomethingWithObject(12.34);
    doSomethingWithObject("foo");
    doSomethingWithObject(List.of(123, "foo", 999L, 12.34));

    doSomethingWithShape(new Rectangle());
    doSomethingWithShape(new Circle(5));
    doSomethingWithShape(new Circle(6));

    doSomethingWithSealedClass(new A());
    doSomethingWithSealedClass(new B());
    doSomethingWithSealedClass(new C(5));

    doSomethingWithInteger(-1);
    doSomethingWithInteger(0);
    doSomethingWithInteger(42);
    doSomethingWithInteger(-99);
    doSomethingWithInteger(Integer.valueOf(123));
    doSomethingWithInteger(null);
  }

  public static Object doSomethingWithObject(Object o) { return o; }
  public static void doSomethingWithSealedClass(S s) {}
  public static void doSomethingWithShape(Shape s) {}
  public static Object doSomethingWithInteger(Integer o) { return o; }
}
