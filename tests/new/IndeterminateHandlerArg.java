
import org.aspectj.testing.Tester;
import org.aspectj.lang.*;
import org.aspectj.lang.reflect.*;

/** @testcase PR#764 binding handler args with indeterminate prefix and suffix */
public class IndeterminateHandlerArg {
    public static void main (String[] args) {
        Throwable throwable = new Throwable("throwable");
        Error error = new Error("error");
        RuntimeException runtime = new RuntimeException("runtime") { 
                RuntimeException f() { return this; } 
            }.f();

        try { throw error; }
        catch (Error e) { A.event(e.getMessage()); }

        try { throw throwable; }
        catch (Throwable e) { A.event(e.getMessage()); }
        try { throw error; }
        catch (Throwable e) { A.event(e.getMessage()); }
        try { throw runtime; }
        catch (Throwable e) { A.event(e.getMessage()); }

        try { throw runtime; }
        catch (RuntimeException e) { A.event(e.getMessage()); }

        Tester.checkEventsFromFile("IndeterminateHandlerArg.events");
    } 
}

aspect A {
    void e(String label, JoinPoint jp) {
        e(label, jp, (Throwable) jp.getArgs()[0]);
    }
    void e(String label, JoinPoint jp, Throwable t) {
        String m = jp.toLongString() 
            + ": " + t.getClass().getName() 
            + " - " + t.getMessage()
            + " @ " + label;
        event(m);
    }
    static void event(String m) { 
        Tester.event(m); 
    }

    pointcut hd() : withincode(static void main(..)) && handler(*);

    before (Throwable t) : hd() && args(t)            { e("before Throwable", thisJoinPoint, t); }
    before (Error t)     : hd() && args(t)            { e("before Error", thisJoinPoint, t); }
    before ()            : hd() && args(Throwable)    { e("before args(Throwable)", thisJoinPoint); }
    before ()            : hd() && args(Error)        { e("before args(Error)",     thisJoinPoint); }
    before ()            : hd() && args(Throwable,..) { e("before args(Throwable,..)", thisJoinPoint); }
    before ()            : hd() && args(..,Throwable) { e("before args(..,Throwable)", thisJoinPoint); }
    before ()            : hd() && args(Error,..)     { e("before args(Error,..)",     thisJoinPoint); }
    before ()            : hd() && args(..,Error)     { e("before args(..,Error)",     thisJoinPoint); }
    before (Throwable t) : hd() && args(t,..)         { e("before Throwable,..", thisJoinPoint, t); }
    before (Error t)     : hd() && args(t,..)         { e("before Error,..", thisJoinPoint, t); }
    before (Throwable t) : hd() && args(..,t)         { e("before ..,Throwable", thisJoinPoint, t); }
    before (Error t)     : hd() && args(..,t)         { e("before ..,Error", thisJoinPoint, t); }

    before ()            : hd() && args(Throwable,*) { Tester.check(false, "args(Throwable,*)"); }
    before ()            : hd() && args(*,Throwable) { Tester.check(false, "args(*,Throwable)"); }
    before ()            : hd() && args(Error,*)     { Tester.check(false, "args(Error,*)"); }
    before ()            : hd() && args(*,Error)     { Tester.check(false, "args(*,Error)"); }
    before (Throwable t) : hd() && args(t,*)         { Tester.check(false, "args((Throwable)t,*)"); }
    before (Error t)     : hd() && args(t,*)         { Tester.check(false, "args((Error)t,*)"); }
    before (Throwable t) : hd() && args(*,t)         { Tester.check(false, "args(*,(Throwable)t)"); }
    before (Error t)     : hd() && args(*,t)         { Tester.check(false, "args(*,(Error)t)"); }
}

