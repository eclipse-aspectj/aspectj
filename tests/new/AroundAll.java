import java.util.*;
import org.aspectj.testing.Tester;

public class AroundAll {
   public static void main(String[] args) {
      new C();
      new C("9");
      //A.printLog();
      A.checkLog();
   } 
}

class C extends SuperC {
    static final int i;
    final int x;

    int y = 42;

    static {
        i = 23;
    }

    C(String s) {
        this(Integer.valueOf(s).intValue());
        A.log("C(" + s + ")");
        A.log("y = " + y);
    }

    C(int i) {
        super(i);
        x = i;
        i = i+1;
        //System.out.println(i + 1);
        A.log("x = " + x);
    }

    C() {
        this("2");
        A.log("C()");
    }
}

class SuperC {
    SuperC(int x) {
        A.log("SuperC(" + x + ")");
    }
}

aspect A {
    static String[] expectedSteps = new String[] {
        "enter staticinitialization(AroundAll.<clinit>)",
        "exit staticinitialization(AroundAll.<clinit>)",
        "enter execution(void AroundAll.main(String[]))",
        "enter call(C())",
        "enter staticinitialization(SuperC.<clinit>)",
        "exit staticinitialization(SuperC.<clinit>)",
        "enter staticinitialization(C.<clinit>)",
        "enter set(int C.i)",
        "exit set(int C.i)",
        "exit staticinitialization(C.<clinit>)",
        "enter call(Integer java.lang.Integer.valueOf(String))",
        "exit call(Integer java.lang.Integer.valueOf(String))",
        "enter call(int java.lang.Integer.intValue())",
        "exit call(int java.lang.Integer.intValue())",
        "enter initialization(SuperC(int))",
        "enter execution(SuperC.<init>)",
        "exit execution(SuperC.<init>)",
        "enter execution(SuperC(int))",
        "SuperC(2)",
        "exit execution(SuperC(int))",
        "exit initialization(SuperC(int))",
        "enter initialization(C())",
        "enter execution(C.<init>)",
        "enter set(int C.y)",
        "exit set(int C.y)",
        "exit execution(C.<init>)",
        "enter execution(C(int))",
        "enter set(int C.x)",
        "exit set(int C.x)",
        "enter get(int C.x)",
        "exit get(int C.x)",
        "x = 2",
        "exit execution(C(int))",
        "enter execution(C(String))",
        "C(2)",
        "enter get(int C.y)",
        "exit get(int C.y)",
        "y = 42",
        "exit execution(C(String))",
        "exit initialization(C())",
        "enter execution(C())",
        "C()",
        "exit execution(C())",
        "exit call(C())",
        "enter call(C(String))",
        "enter call(Integer java.lang.Integer.valueOf(String))",
        "exit call(Integer java.lang.Integer.valueOf(String))",
        "enter call(int java.lang.Integer.intValue())",
        "exit call(int java.lang.Integer.intValue())",
        "enter initialization(SuperC(int))",
        "enter execution(SuperC.<init>)",
        "exit execution(SuperC.<init>)",
        "enter execution(SuperC(int))",
        "SuperC(9)",
        "exit execution(SuperC(int))",
        "exit initialization(SuperC(int))",
        "C.new(9)",
        "enter initialization(C(String))",
        "enter execution(C.<init>)",
        "enter set(int C.y)",
        "exit set(int C.y)",
        "exit execution(C.<init>)",
        "enter execution(C(int))",
        "enter set(int C.x)",
        "exit set(int C.x)",
        "enter get(int C.x)",
        "exit get(int C.x)",
        "x = 9",
        "exit execution(C(int))",
        "enter execution(C(String))",
        "C(91)",
        "enter get(int C.y)",
        "exit get(int C.y)",
        "y = 42",
        "exit execution(C(String))",
        "exit initialization(C(String))",
        "exit call(C(String))",
        };

    static List logList = new ArrayList();

    static void printLog() {
        for (Iterator i = logList.iterator(); i.hasNext(); ) {
            System.out.println("        \"" + i.next() + "\", ");
        }
    }

    static void checkLog() {
      Tester.checkEqual(expectedSteps, A.logList.toArray(), "steps");
      Tester.checkEqual(A.logList, expectedSteps, "steps");
    }

    static void log(String s) {
        logList.add(s);
    }

    static boolean test() { return true; }

    //before(): initialization(C.new(String)) { }

    void around(String s): initialization(C.new(String)) && args(s) && if(s.equals("9")) {
        log("C.new(9)");
        proceed(s+"1");
    }

    Object around(): //initialization(C.new(String)) { 
                    if(test()) && !within(A) && !call(* A.*(..)) {
       A.log("enter " + thisJoinPoint);
       Object ret = proceed();
       A.log("exit " + thisJoinPoint);
       //proceed();
       //System.err.println("run twice");
       return ret;
       }
}
