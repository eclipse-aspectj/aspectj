
//package debugger;

import com.sun.jdi.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;
import java.io.*;
import java.util.*;
import org.aspectj.tools.debugger.*;

/**
 * BreakpointTester.java
 *
 *
 * Created: Wed Sep 06 15:53:29 2000
 *
 * @author <a href="mailto:palm@parc.xerox.com"Jeffrey Palm</a>
 */

public class BreakpointTester extends Tester {

    public static void main(String[] args) {
        new Main(new BreakpointTester(false), args);
    }

    public BreakpointTester(boolean d) {
        super(d);
    }

    public String getClassName() {
        return "TestClass";
    }

    public boolean test() {
        db();
        Vector locals = new Vector();
        Vector names = new Vector();
        names.add("");
        names.add("system");
        names.add("main");
        IntVector sizes = new IntVector();
        sizes.add(4);
        sizes.add(3);
        sizes.add(1);
        Vector cases = new Vector();
        
        try {
            stopin("main");
            stopin("a");
            stopin("b");
            //stopat("TestClassAspect", 41);
            stopin("c");
            stopat("TestClassAspect", 54);

            int i = 0;
            locals = new Vector();
            locals.add("args");
            cases.add(new Case(PCKG + "TestClass.main", 5, 1, locals, names, sizes, (i++)));

            locals = new Vector();
            cases.add(new Case(PCKG + "TestClass.a", -2, 3, locals, names, sizes, (i++)));

            locals = new Vector();
            cases.add(new Case(PCKG + "TestClass.b", -2, 5, locals, names, sizes, (i++)));

//              locals = new Vector();
//              locals.add("thisJoinPoint");
//              cases.add(new Case(PCKG + "TestClassAspect:41", 41, 6, locals, names, sizes, (i++)));

            locals = new Vector();
            cases.add(new Case(PCKG + "TestClass.c", -2, 7, locals, names, sizes, (i++)));

            locals = new Vector();
            locals.add("thisJoinPoint");
            cases.add(new Case(PCKG + "TestClassAspect:54", 54, 8, locals, names, sizes, (i++)));

            stop(cases);
            startTest();
            
            return true;
        } catch (DebuggerException de) { de.printStackTrace(out);
        }
        return false;
    }
}
