import org.aspectj.testing.Tester;

class BadDesignator {
  pointcut cc(): BadDesignator && * void f();

  BadDesignator() {}

  void f() {}
}

 
