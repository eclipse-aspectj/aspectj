import org.aspectj.testing.Tester;

public class Driver {
  static Object objectFromClass; 
  static Object objectFromAspect; 

  static String stringFromClass;
  static String stringFromAspect;
  
  public static void main(String[] args) { test(); }
  
  static public void test() {
    Driver       obj = new Driver();
    
    obj.doit();

    Tester.check(objectFromClass == objectFromAspect,
                  "this matches this");
    Tester.check(stringFromClass.equals(stringFromAspect),
                  "this.toString() matches this.toString()");
  }
 
  void doit() {
    objectFromClass = this;
    stringFromClass = this.toString();
  }
}

aspect DriverAspect pertarget(target(Driver)) {
  before (Driver d): target(d) && call(* doit(..)) {
      d.objectFromAspect = d;
      d.stringFromAspect = d.toString();
  }
}
