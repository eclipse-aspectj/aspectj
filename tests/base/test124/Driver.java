import org.aspectj.testing.Tester;

public class Driver {
  public static void main(String[] args) { test(); }
  
  public static void test() {
    Driver.m(4);
    Tester.checkEqual(Trace.enterCount, 2, "befores");
    // The after for this method hasn't been run yet
    Tester.checkEqual(Trace.exitCount, 1, "afters");
  }

  static int m(int x) {
    return 0;
  }
}

aspect Trace {
    public static int enterCount, exitCount;
    // notice the absence of any modifiers in the advise
     before(): within(Driver) && (execution(void test()) || 
                                       execution(* m(..))) {
        enterCount++;
    }
     after(): within(Driver) && (execution(void test()) ||
                                      execution(* m(..))) {
        exitCount++;
    }
}
