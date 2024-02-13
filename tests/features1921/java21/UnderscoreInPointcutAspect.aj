public aspect UnderscoreInPointcutAspect {
  public static void main(String[] args) {
    UnderTest u = new UnderTest();
    System.out.println(u._add(12, 4));
    System.out.println(u._subtract(12, 4));
    System.out.println(u.multiply_(12, 4));
    System.out.println(u.divide_(12, 4));
    System.out.println(u.power_of(3, 3));
    System.out.println(u.squareRoot(49));
  }

  before(int a, int b) : execution(* _*(..)) && args(a, b) {
    System.out.println("[starts with underscore] " + thisJoinPoint + " -> " + a + ", " + b);
  }

  before(int a, int b) : execution(* *_(..)) && args(a, b) {
    System.out.println("[ends with underscore] " + thisJoinPoint + " -> " + a + ", " + b);
  }

  before(int a, int b) : execution(* *_*(..)) && args(a, b) {
    System.out.println("[contains underscore] " + thisJoinPoint + " -> " + a + ", " + b);
  }

  before(int a) : execution(* *(..)) && !execution(* *_*(..)) && args(a) {
    System.out.println("[no underscore] " + thisJoinPoint + " -> " + a);
  }
}

class UnderTest {
  int _add(int a, int b) {
    return a + b;
  }

  int _subtract(int a, int b) {
    return a - b;
  }

  int multiply_(int a, int b) {
    return a * b;
  }

  int divide_(int a, int b) {
    return a / b;
  }

  int power_of(int base, int exponent) {
    return (int) Math.pow(base, exponent);
  }

  int squareRoot(int a) {
    return (int) Math.sqrt(a);
  }
}
