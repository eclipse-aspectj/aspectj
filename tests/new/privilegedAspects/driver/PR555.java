
package driver;

//package pack; // check test passing 
// import pack.DefaultTarget; // not expected to work
import org.aspectj.testing.Tester;
import org.aspectj.testing.Tester; 

public class PR555 {
    public static void main(String[] args) {
        Tester.expectEvent("runner");
        int i ; 
        i = PA.readPrivateStaticInt();
        Tester.check(1==i,"PA.readPrivateStaticInt()");
        i = PA.readPrivateStaticIntFromInner();
        Tester.check(1==i,"PA.readPrivateStaticIntFromInner()");
        i = PA.runIntroducedAnonymousInnerClass();
        Tester.check(1==i,"PA.runIntroducedAnonymousInnerClass()");
        Tester.checkAllEvents();
    }

}

/* test access to private target variables */
privileged aspect PA {

    public static int readPrivateStaticInt() {
        return pack.DefaultTarget.privateStaticInt;
    }

    public static int readPrivateStaticIntFromInner() {
        return new Inner().readPrivateIntFromInner();
    }

    public static int runIntroducedAnonymousInnerClass() {
        pack.DefaultTarget dtarget = new pack.DefaultTarget();
        dtarget.runner.run();
        return dtarget.privateInt;
        //return dtarget.publicInt;
    }

    static class Inner {
        public static final int i = pack.DefaultTarget.privateStaticInt;
        //public static final int i = pack.DefaultTarget.publicStaticInt;
        public int readPrivateIntFromInner() {
            pack.DefaultTarget defaultTarget 
                = new pack.DefaultTarget();
            return defaultTarget.privateInt;
            //return defaultTarget.publicInt;
        }
    }

    // todo: bug used unprivileged aspect to introduce
    // todo: inner class of aspect or of DefaultTarget?
    Runnable pack.DefaultTarget.runner = new Runnable() {
            public void run() {
                Tester.event("runner");
                //if (1 != publicInt) {
                if (1 != privateInt) {
                    throw new Error("1 != privateInt");
                }
            }
        };
}

