import java.util.*;

class Parent {

  Object method1() {return null;}
    void method2(String a) {}
    void method3(List l,String b) {}

}

class Child extends Parent {

  @Override String method1() {return null;}
  @Override void method2(String b) {}
  @Override void method3(List l,String b) {}

}

class Child2 extends Parent {
}

aspect Injector { 

  @Override public String Child2.method1() {return null;}  
  @Override public   void Child2.method2(String s) {}  
  @Override public   void Child2.method3(List l,String b) {}  

}
