import org.aspectj.testing.Tester;

// PR#259 "throws Exception" clause is unnecessarily added to Driver.main method

public class Driver {

    /*private*/ static String s = "";
    
    public static void main(String[] args) { test(); }

    public static void test() {
        Driver ts = new Driver();
        Tester.checkEqual(s, "-bound-around", "bound");
    }

    void bind() throws Exception { s += "-bound"; }

    public Driver() { bind(); }
}

aspect Aspect {
    pointcut bind(): within(Driver) && call(void Driver.bind());

    declare soft: Exception: bind();

    void around(): bind() {
        try {
            proceed();
        } catch (Exception e) { }
        Driver.s += "-around";
    }
}


/* HERE IS THE FIXED VERSION OF MARK'S TEST (the one in the bug
   database was broken):
public class Driver
{

  Driver() throws  Exception { }


  public static void main(String[] args) {
    Driver ts = new Driver();
    Driver.bind("foo",ts);
  }

  static void bind(String s, Object o)
  {}

 static around() returns Driver: within(Driver) &&
                        calls(Driver, new() ){
   Driver result = null;
   try {
      result = proceed();
   } catch (Exception e){ }
   return result;
 }


 static around(String name) returns void:
      within(Driver) &&
      calls(Driver, * bind(name,..)) {
   try {
     proceed(name + "0");
   } catch (Exception e) { }
 }

  static before(String[] args):
      within(Driver) && executions(void main(args)){
    System.out.println("...");
  }

}
*/

