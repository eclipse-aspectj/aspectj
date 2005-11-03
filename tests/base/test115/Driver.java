import org.aspectj.testing.Tester;

public class Driver {
  public static void main(String[] args) { test(); }
  
  static public void test() {
    SubClass s = new SubClass(0);
    Tester.checkEqual(Aspect.count, 2, "introduced constructors");
  }
}

class Class {}

class SubClass extends Class {}

// this should introduce a unary constructor for 
// Class and SubClass

aspect Aspect {
  static int count = 0;
  //introduction subtypes(Class) {
  Class.new(int i) {this(); count++;}
  SubClass.new(int i) {super(2); count++;}
      //}
}
