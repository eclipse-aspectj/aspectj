import org.aspectj.testing.*;

public class IntroOnIntro {
  public static void main(String[] args) {
    Class1 c1 = new Class1();

    try {
      c1.getName();
    } catch (NoSuchMethodError nsme) {
      Tester.check(false, "getName was not found in Class1");
    }

    try {
      c1.m = "potato";
    } catch (NoSuchFieldError nsfe) {
      Tester.check(false, "m was not introduced into Class1");
    }
  }
}

class Class1 {
  String name = "";  //public String getName() { return name; }
}

aspect Aspect1 /**of eachobject (instanceof(Class1))*/ {
    public String Class1.getName() { return this.name; } 
  
  void f() {}
  before(): call(* getName(..)) && this(Class1) {
    f();
  }  
}

aspect AComposer /**of eachobject(instanceof(Class1 || Aspect1))*/ {
	interface HasManager {}
    private String HasManager.my_manager;
    String HasManager.m;
    public void HasManager.setManager(String manager) { 
      this.my_manager = manager; 
    }
    declare parents: Class1 || Aspect1 implements HasManager;
  
  before(Aspect1 a1): call(void f()) && this(a1) {
    
    try {
      a1.setManager("potato");
    } catch (NoSuchMethodError nsme) {
      Tester.check(false, "getName not found in Aspect1");
    }
    
    try {
      a1.m = "potato";
    } catch (NoSuchFieldError nsfe) {
      Tester.check(false, "m was not introduced into Class1");
    }    
  }
}




