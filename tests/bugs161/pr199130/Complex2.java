interface A {}
interface B {}
abstract aspect Parent< V extends A > {}
abstract aspect Child< V extends A & B > extends Parent< V > {}

aspect Foo extends Child<AExtension> {}

class AImpl implements A {}

class AExtension extends AImpl implements B {}

public class Complex2 {
  public static void main(String [] argv) {
  }
}
