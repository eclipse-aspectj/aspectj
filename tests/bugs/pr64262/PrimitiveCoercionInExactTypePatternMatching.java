

public class PrimitiveCoercionInExactTypePatternMatching  {
   double foo(double x) {
      return x;
   }
}

aspect Aspect {

  pointcut foo(int b): args(b);

  before(int i) : foo(i)  {
  }
}