public class PR320 {
    public static void main(String[] args) {
        //org.aspectj.testing.Tester.check(false, "Shouldn't have compiled!");
        org.aspectj.testing.Tester.check(true, "OK to compile by 08b1!");
    }
}
class Product1 {}
aspect Product1Aspect pertarget(target(Product1)){

  pointcut instance(Product1 p): target(p);
  before(Product1 p): instance(p) {
    System.out.println("Im am instance of product1");
  }
}
