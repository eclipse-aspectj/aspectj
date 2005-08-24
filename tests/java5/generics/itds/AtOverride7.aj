import java.util.*;

abstract class Parent {

}

class Child extends Parent {

  @Override public String method1() {return null;}

}

aspect Injector { 

  public abstract Object Parent.method1();

}
