aspect MyAspect implements MyInterface<MyClass> {
  before() : MyAspect1<MyClass>.myPointcutInInterface(){ }
}

class MyClass { }

interface MyInterface<T>{
  public abstract static aspect MyAspect1<T> {
    public final pointcut myPointcutInInterface() : call(* *..*.*(..));
  }
}
