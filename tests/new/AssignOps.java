import org.aspectj.testing.Tester; 
import org.aspectj.testing.Tester;

aspect A {    
    static boolean start = false;
    static int bset;
    static int bget;
    static int siset;
    static int siget;
    static int iset;
    static int iget;
    static void start() {
        siset = siget = iset = iget = 0;
        start = true;
    }
    static String p() {
        return 
            " siset=" + siset
            + " siget=" + siget
            + " iset=" + iset
            + " iget=" + iget ;
    }
    before() : get(int AssignOps.staticInt) {
        signal(AssignOps.sigetStr+ siget++);
        check(siget == (1+siset), "siget == (1+siset)" + p());
    }
    before() : set(int AssignOps.staticInt) {
        signal(AssignOps.sisetStr + siset++);
        check(siget == siset, "siget == siset" + p());
    }
    before() : get(int AssignOps.instanceInt) {
        signal(AssignOps.igetStr + iget++);
        check(iget == (1+iset), "iget == (1+iset)" + p());
    }
    before() : set(int AssignOps.instanceInt) {
        signal(AssignOps.isetStr + iset++);
        check(iget == iset, "iget == iset" + p());
    }
    before() : get(boolean AssignOps.bool) {
        signal(AssignOps.bgetStr + bget++);
        check(bget == (1+bset), "bget == (1+bset)" + p());
    }
    before() : set(boolean AssignOps.bool) {
        signal(AssignOps.bsetStr + bset++);
        check(bget == bset, "bget == bset" + p());
    }
    static void check(boolean b, String s) { 
        if (start) Tester.check(b, s);
    }
    static void signal(String s) { 
        if (start) Tester.event(s); 
    }
}

/** @testcase operators ++ += etc. result in a get and set join point */
public class AssignOps {
    static int staticInt;
    int instanceInt;
    boolean bool;
    static final String sisetStr = "before() : set(int AssignOps.staticInt)";
    static final String sigetStr = "before() : get(int AssignOps.staticInt)";
    static final String isetStr = "before() : set(int AssignOps.instanceInt)";
    static final String igetStr = "before() : get(int AssignOps.instanceInt)";
    static final String bsetStr = "before() : set(boolean AssignOps.bool)";
    static final String bgetStr = "before() : get(boolean AssignOps.bool)";
    public static void main(String[] args) {
        new AssignOps(3).run();
        Tester.checkAllEvents();
    }
    static void t(String s) { Tester.expectEvent(s); }
    AssignOps(int i) { instanceInt = i; }
    void run() {
        A.start();
        t(igetStr + "0");
        t(isetStr + "0");
        instanceInt++;
        t(sigetStr + "0");
        t(sisetStr + "0");
        staticInt++;
        t(igetStr + "1");
        t(isetStr + "1");
        instanceInt += 2;
        t(sigetStr + "1");
        t(sisetStr + "1");
        staticInt += 2;

        t(igetStr + "2");
        t(isetStr + "2");
        instanceInt--;
        t(sigetStr + "2");
        t(sisetStr + "2");
        staticInt--;
        t(igetStr + "3");
        t(isetStr + "3");
        instanceInt -= 2;
        t(sigetStr + "3");
        t(sisetStr + "3");
        staticInt -= 2;

        t(igetStr + "4");
        t(isetStr + "4");
        instanceInt *= 2;
        t(sigetStr + "4");
        t(sisetStr + "4");
        staticInt *= 2;

        t(igetStr + "5");
        t(isetStr + "5");
        instanceInt /= 2;
        t(sigetStr + "5");
        t(sisetStr + "5");
        staticInt /= 2;

        t(igetStr + "6");
        t(isetStr + "6");
        instanceInt %= 2;
        t(sigetStr + "6");
        t(sisetStr + "6");
        staticInt %= 2;

        t(igetStr + "7");
        t(isetStr + "7");
        instanceInt >>= 2;
        t(sigetStr + "7");
        t(sisetStr + "7");
        staticInt >>= 2;

        t(igetStr + "8");
        t(isetStr + "8");
        instanceInt <<= 2;
        t(sigetStr + "8");
        t(sisetStr + "8");
        staticInt <<= 2;

        t(igetStr + "9");
        t(isetStr + "9");
        instanceInt >>>= 2;
        t(sigetStr + "9");
        t(sisetStr + "9");
        staticInt >>>= 2;

        t(bgetStr + "0");
        t(bsetStr + "0");
        bool &= true;
        t(bgetStr + "1");
        t(bsetStr + "1");
        bool |= false;
        t(bgetStr + "2");
        t(bsetStr + "2");
        bool ^= false;
    }
}
