
import org.aspectj.testing.*;
import java.lang.reflect.*;

// XXX incomplete - find all cases of generated static fields
/** @testcase PR#704 mark generated static fields transient */
public class GeneratedStaticAsTransient {
    public static void main (String[] args) {
        new GeneratedStaticAsTransient().callRun();
        checkStatic(GeneratedStaticAsTransient.class, false);
        Tester.checkAllEvents();
    } 
    public void callRun() { run(); }
    
    public void run() {
        Tester.event("run");
        Tester.check(null != A.aspectOf(this),"null != A.hasAspect(this)");
        Tester.check(null != C.aspectOf(this),"null != C.hasAspect(this)");
        Tester.check(null != B.aspectOf(),"null != B.hasAspect()");
    }
    static {
        Tester.expectEvent("after returning - target");
        Tester.expectEvent("after returning - this");
        Tester.expectEvent("after returning - cflow");
        Tester.expectEvent("run");
    }
    public static void checkStatic(Class c, boolean requireStatic) {
        boolean gotStatic = false;
        Field[] fields = c.getFields();
        for (int i = 0; i < fields.length; i++) {
            int mods = fields[i].getModifiers();
            //System.err.println("checking " + fields[i]);
            if (Modifier.isStatic(mods)) {
                //System.err.println("  static " + fields[i]); 
                if (!gotStatic) gotStatic = true;
                if (!Modifier.isTransient(mods)) {
                    String m = "field " + i + " " 
                        + c.getName() + "." + fields[i].getName()
                        + " is static but not transient. mods=" + mods;
                    //System.err.println(m); 
                    Tester.check(false, m);
                }
            }
        } 
        if (requireStatic) {
            Tester.check(gotStatic, c + "no static field");
        }
    }
}

aspect A pertarget(callRun()) {
    pointcut callRun() : call(void GeneratedStaticAsTransient.run()); 
    after () returning : callRun() {
        Tester.event("after returning - target");
        GeneratedStaticAsTransient.checkStatic(A.class, false);
    }
}
aspect B percflow(A.callRun()) {
    after () returning : A.callRun() {
        Tester.event("after returning - cflow");
        GeneratedStaticAsTransient.checkStatic(B.class, true);
    }
}
aspect C perthis(A.callRun()) {
    after () returning : A.callRun() {
        Tester.event("after returning - this");
        GeneratedStaticAsTransient.checkStatic(C.class, false);
    }
}
