public class TestingAnnotations {

  public static void main(String[] args) {

    A a = new A();
    B b = new B();
    C c = new C();
    D d = new D();
    A reallyB = new B();
    C reallyD = new D();

    a.doSomething();
    b.doSomething();
    c.doSomething();
    d.doSomething();
    reallyB.doSomething();
    reallyD.doSomething();
  }

}

@MyClassRetentionAnnotation
class A {
  public void doSomething() {}
}


@MyAnnotation
class B extends A {
  public void doSomething() {}
}

@MyInheritableAnnotation
@MyAnnotation
class C {
  public void doSomething() {}
}

class D extends C {
  public void doSomething() {}
}


@interface MyClassRetentionAnnotation {}

@java.lang.annotation.Retention(value=java.lang.annotation.RetentionPolicy.RUNTIME)
@interface MyAnnotation {}

@java.lang.annotation.Retention(value=java.lang.annotation.RetentionPolicy.RUNTIME)
@java.lang.annotation.Inherited
@interface MyInheritableAnnotation {}