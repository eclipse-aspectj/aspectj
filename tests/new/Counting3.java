import java.util.Vector;
import org.aspectj.testing.*;

public class Counting3 {
    public static void main(String[] args) {
        Testing.start();
        new Counting3().f();
        Testing.finish();
    }
    int j;
    void f() {
        j = 13;
        int i = j;
    }
}

class Testing {
    static void start() {
        //                           - e main - - ca f() - - ex f() - - set j --------- - get j ---------
        Tester.expectEventsInString("e,egs,cegs,c,cgs,cegs,e,egs,cegs,s,gs,cgs,egs,cegs,g,gs,cgs,egs,cegs");
        // Tester.expectEventsInString("g,s,gs,c,e,cgs,egs,cegs");  // old, incorrect (matching dups)
    }
    static void finish() {
        Tester.checkAllEvents();
    }
}

aspect JoinPointCounting {

    pointcut g(): get(* *.*) && within(Counting3);
    before(): g() { a("g"); }

    pointcut s(): set(* *.*) && within(Counting3);
    before(): s() { a("s"); }
    
    pointcut gs(): g() || s();
    before(): gs() { a("gs"); }
    
    pointcut c(): call(* *.*(..)) && within(Counting3) && ! call(* Testing.*());
    before(): c() { a("c"); }
    pointcut e(): execution(* *.*(..)) && within(Counting3);
    before(): e() { a("e"); }

    pointcut cgs(): c() || gs(); before(): cgs() { a("cgs"); }
    pointcut egs(): e() || gs(); before(): egs() { a("egs"); }

    pointcut cegs(): c() || e() || gs(); before(): cegs() { a("cegs"); }

    static void a(String s) { Tester.event(s); }
    
}
