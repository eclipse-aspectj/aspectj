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
        "exit staticinitialization(C.<clinit>)", 
        "enter call(Integer java.lang.Integer.valueOf(String))", 
        "exit call(Integer java.lang.Integer.valueOf(String))", 
        "enter call(int java.lang.Integer.intValue())", 
        "exit call(int java.lang.Integer.intValue())", 
        "enter execution(SuperC(int))", 
        "enter call(java.lang.StringBuffer(String))", 
        "exit call(java.lang.StringBuffer(String))", 
        "enter call(StringBuffer java.lang.StringBuffer.append(int))", 
        "exit call(StringBuffer java.lang.StringBuffer.append(int))", 
        "enter call(StringBuffer java.lang.StringBuffer.append(String))", 
        "exit call(StringBuffer java.lang.StringBuffer.append(String))", 
        "enter call(String java.lang.StringBuffer.toString())", 
        "exit call(String java.lang.StringBuffer.toString())", 
        "SuperC(2)", 
        "exit execution(SuperC(int))", 
        "enter execution(C(int))", 
        "enter set(int C.y)", 
        "exit set(int C.y)", 
        "enter set(int C.x)", 
        "exit set(int C.x)", 
        "enter call(java.lang.StringBuffer(String))", 
        "exit call(java.lang.StringBuffer(String))", 
        "enter get(int C.x)", 
        "exit get(int C.x)", 
        "enter call(StringBuffer java.lang.StringBuffer.append(int))", 
        "exit call(StringBuffer java.lang.StringBuffer.append(int))", 
        "enter call(String java.lang.StringBuffer.toString())", 
        "exit call(String java.lang.StringBuffer.toString())", 
        "x = 2", 
        "exit execution(C(int))", 
        "enter execution(C(String))", 
        "enter call(java.lang.StringBuffer(String))", 
        "exit call(java.lang.StringBuffer(String))", 
        "enter call(StringBuffer java.lang.StringBuffer.append(String))", 
        "exit call(StringBuffer java.lang.StringBuffer.append(String))", 
        "enter call(StringBuffer java.lang.StringBuffer.append(String))", 
        "exit call(StringBuffer java.lang.StringBuffer.append(String))", 
        "enter call(String java.lang.StringBuffer.toString())", 
        "exit call(String java.lang.StringBuffer.toString())", 
        "C(2)", 
        "enter call(java.lang.StringBuffer(String))", 
        "exit call(java.lang.StringBuffer(String))", 
        "enter get(int C.y)", 
        "exit get(int C.y)", 
        "enter call(StringBuffer java.lang.StringBuffer.append(int))", 
        "exit call(StringBuffer java.lang.StringBuffer.append(int))", 
        "enter call(String java.lang.StringBuffer.toString())", 
        "exit call(String java.lang.StringBuffer.toString())", 
        "y = 42", 
        "exit execution(C(String))", 
        "enter execution(C())", 
        "C()", 
        "exit execution(C())", 
        "exit call(C())", 
        "enter call(C(String))", 
        "enter call(Integer java.lang.Integer.valueOf(String))", 
        "exit call(Integer java.lang.Integer.valueOf(String))", 
        "enter call(int java.lang.Integer.intValue())", 
        "exit call(int java.lang.Integer.intValue())", 
        "enter execution(SuperC(int))", 
        "enter call(java.lang.StringBuffer(String))", 
        "exit call(java.lang.StringBuffer(String))", 
        "enter call(StringBuffer java.lang.StringBuffer.append(int))", 
        "exit call(StringBuffer java.lang.StringBuffer.append(int))", 
        "enter call(StringBuffer java.lang.StringBuffer.append(String))", 
        "exit call(StringBuffer java.lang.StringBuffer.append(String))", 
        "enter call(String java.lang.StringBuffer.toString())", 
        "exit call(String java.lang.StringBuffer.toString())", 
        "SuperC(9)", 
        "exit execution(SuperC(int))", 
        "enter execution(C(int))", 
        "enter set(int C.y)", 
        "exit set(int C.y)", 
        "enter set(int C.x)", 
        "exit set(int C.x)", 
        "enter call(java.lang.StringBuffer(String))", 
        "exit call(java.lang.StringBuffer(String))", 
        "enter get(int C.x)", 
        "exit get(int C.x)", 
        "enter call(StringBuffer java.lang.StringBuffer.append(int))", 
        "exit call(StringBuffer java.lang.StringBuffer.append(int))", 
        "enter call(String java.lang.StringBuffer.toString())", 
        "exit call(String java.lang.StringBuffer.toString())", 
        "x = 9", 
        "exit execution(C(int))", 
        "enter execution(C(String))", 
        "enter call(java.lang.StringBuffer(String))", 
        "exit call(java.lang.StringBuffer(String))", 
        "enter call(StringBuffer java.lang.StringBuffer.append(String))", 
        "exit call(StringBuffer java.lang.StringBuffer.append(String))", 
        "enter call(StringBuffer java.lang.StringBuffer.append(String))", 
        "exit call(StringBuffer java.lang.StringBuffer.append(String))", 
        "enter call(String java.lang.StringBuffer.toString())", 
        "exit call(String java.lang.StringBuffer.toString())", 
        "C(9)", 
        "enter call(java.lang.StringBuffer(String))", 
        "exit call(java.lang.StringBuffer(String))", 
        "enter get(int C.y)", 
        "exit get(int C.y)", 
        "enter call(StringBuffer java.lang.StringBuffer.append(int))", 
        "exit call(StringBuffer java.lang.StringBuffer.append(int))", 
        "enter call(String java.lang.StringBuffer.toString())", 
        "exit call(String java.lang.StringBuffer.toString())", 
        "y = 42", 
        "exit execution(C(String))", 
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
    
    // one minimal version
//    before(): this(Runnable) && call(* intValue()) {
//      
//    }


//    void around(String s): initialization(C.new(String)) && args(s) && if(s.equals("9")) {
//        log("C.new(9)");
//        proceed(s+"1");
//    }

    Object around(): //initialization(C.new(String)) { 
                    if(test()) && !within(A) && !call(* A.*(..)) && !initialization(new(..)) && !preinitialization(new(..)) {
       A.log("enter " + thisJoinPoint);
       Object ret = proceed();
       A.log("exit " + thisJoinPoint);
       //proceed();
       //System.err.println("run twice");
       return ret;
       }
}
