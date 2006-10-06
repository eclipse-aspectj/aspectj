aspect MyAspect implements MyInterface<MyClass> {
  before() : MyAspect1<MyClass>.myPointcutInInterface(){ }
}

class MyClass {}

interface MyInterface<T>{
  public abstract static aspect MyAspect1<T> {
    public final pointcut myPointcutInInterface() : call(T *(..));
  }
}

public class Bug2 {
  public static void main(MyClass[]argv) {
    new Bug2().callit();
  }

  public MyClass callit() {
    return null;
  }
}

aspect MyAspect2 implements MyInterface<String> {
	before(): MyAspect1<String>.myPointcutInInterface() {} // shouldn't match...  since the return type of callit is MyClass
}