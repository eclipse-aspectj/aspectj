import org.aspectj.testing.*;
import java.awt.event.*;
import javax.swing.*;

public class Inners {

    public static void main(String[] args) {
        new Inners().realMain(args);
    }
    
    public void realMain(String[] args) {
        unincludedInterfaces();
        includedInterfaces();
        unincludedAbstractClases();
        includedAbstractClases();
        Tester.checkAllEvents();
    }

    static {
        // Ensure all methods run
        Tester.expectEventsInString("localRunnable0,localRunnable1,anonRunnable,staticInnerRunnable0" +
           ",innerRunnable0,staticInnerRunnable1,innerRunnable1");
        Tester.expectEventsInString("localStuff0,localStuff1,anonStuff,staticInnerStuff0" +
           ",innerStuff0,staticInnerStuff1,innerStuff1");
        Tester.expectEventsInString("localAbstractAction0,localAbstractAction1,,localAbstractAction2" +
           ",anonAbstractAction0,anonAbstractAction1," +
           ",staticInnerAbstractAction0,staticInnerAbstractAction1,staticInnerAbstractAction2" +
           ",innerAbstractAction0,innerAbstractAction1,innerAbstractAction2");
        Tester.expectEventsInString("localAbstractStuff0,localAbstractStuff1,,localAbstractStuff2" +
           ",anonAbstractStuff0,anonAbstractStuff1," +
           ",staticInnerAbstractStuff0,staticInnerAbstractStuff1,staticInnerAbstractStuff2" +
           ",innerAbstractStuff0,innerAbstractStuff1,innerAbstractStuff2");        
    }

    static String s = "";
    static void c(Object o) {
        s = o + "";
    }

    static int localRunnableI = 0;
    private void unincludedInterfaces() {
        class LocalRunnable implements Runnable {
            public void run() { a("localRunnable" + (localRunnableI++)); }
        }
        Runnable r0 = new StaticInnerRunnable();
        Runnable r1 = new InnerRunnable();
        Runnable r2 = new LocalRunnable();
        Runnable r3 = new Runnable() {
                public void run() { a("anonRunnable"); }
            };
        StaticInnerRunnable r4 = new StaticInnerRunnable();
        InnerRunnable r5 = new InnerRunnable();
        LocalRunnable r6 = new LocalRunnable();
        c("ui-r0"); r0.run();
        c("ui-r1"); r1.run();
        c("ui-r2"); r2.run();
        c("ui-r3"); r3.run();
        c("ui-r4"); r4.run();
        c("ui-r5"); r5.run();
        c("ui-r6"); r6.run();
    }
    
    static int localStuffI = 0;
    private void includedInterfaces() {
        class LocalStuff implements Stuff {
            public void run() { a("localStuff" + (localStuffI++)); }
        }
        Stuff r0 = new StaticInnerStuff();
        Stuff r1 = new InnerStuff();
        Stuff r2 = new LocalStuff();
        Stuff r3 = new Stuff() {
                public void run() { a("anonStuff"); }
            };
        StaticInnerStuff r4 = new StaticInnerStuff();
        InnerStuff r5 = new InnerStuff();
        LocalStuff r6 = new LocalStuff();
        c("ii-r0"); r0.run();
        c("ii-r1"); r1.run();
        c("ii-r2"); r2.run();
        c("ii-r3"); r3.run();
        c("ii-r4"); r4.run();
        c("ii-r5"); r5.run();
        c("ii-r6"); r6.run();        
    }

    static int localAbstractActionI = 0;
    private void unincludedAbstractClases() {
        class LocalAbstractAction extends AbstractAction {
            public void actionPerformed(ActionEvent e) { a("localAbstractAction" + (localAbstractActionI++)); }
        }
        AbstractAction r0 = new StaticInnerAbstractAction();
        AbstractAction r1 = new InnerAbstractAction();
        AbstractAction r2 = new LocalAbstractAction();
        AbstractAction r3 = new AbstractAction() {
                public void actionPerformed(ActionEvent e) { a("anonAbstractAction0"); }
            };
        StaticInnerAbstractAction r4 = new StaticInnerAbstractAction();
        InnerAbstractAction r5 = new InnerAbstractAction();
        LocalAbstractAction r6 = new LocalAbstractAction();
        ActionListener r7 = new StaticInnerAbstractAction();
        ActionListener r8 = new InnerAbstractAction();
        ActionListener r9 = new LocalAbstractAction();
        ActionListener r10 = new AbstractAction() {
                public void actionPerformed(ActionEvent e) { a("anonAbstractAction1"); }
            };
        c("ua-r0");  r0.actionPerformed(null);
        c("ua-r1");  r1.actionPerformed(null);
        c("ua-r2");  r2.actionPerformed(null);
        c("ua-r3");  r3.actionPerformed(null);
        c("ua-r4");  r4.actionPerformed(null);
        c("ua-r5");  r5.actionPerformed(null);
        c("ua-r6");  r6.actionPerformed(null);
        c("ua-r7");  r7.actionPerformed(null);
        c("ua-r8");  r8.actionPerformed(null);
        c("ua-r9");  r9.actionPerformed(null);
        c("ua-r10"); r10.actionPerformed(null);        
    }
    
    static int localAbstractStuffI = 0;    
    private void includedAbstractClases() {
        class LocalAbstractStuff extends AbstractStuff {
            public void run() { a("localAbstractStuff" + (localAbstractStuffI++)); }
        }
        AbstractStuff r0 = new StaticInnerAbstractStuff();
        AbstractStuff r1 = new InnerAbstractStuff();
        AbstractStuff r2 = new LocalAbstractStuff();
        AbstractStuff r3 = new AbstractStuff() {
                public void run() { a("anonAbstractStuff0"); }
            };
        StaticInnerAbstractStuff r4 = new StaticInnerAbstractStuff();
        InnerAbstractStuff r5 = new InnerAbstractStuff();
        LocalAbstractStuff r6 = new LocalAbstractStuff();
        Stuff r7 = new StaticInnerAbstractStuff();
        Stuff r8 = new InnerAbstractStuff();
        Stuff r9 = new LocalAbstractStuff();
        Stuff r10 = new AbstractStuff() {
                public void run() { a("anonAbstractStuff1"); }
            };        
        c("ia-r0");  r0.run();
        c("ia-r1");  r1.run();
        c("ia-r2");  r2.run();
        c("ia-r3");  r3.run();
        c("ia-r4");  r4.run();
        c("ia-r5");  r5.run();
        c("ia-r6");  r6.run();
        c("ia-r7");  r7.run();
        c("ia-r8");  r8.run();
        c("ia-r9");  r9.run();
        c("ia-r10"); r10.run();
    }

    
    /* Implement an unincluded interface */
    static class StaticInnerRunnable implements Runnable {
        static int i = 0;
        public void run() { Tester.event("staticInnerRunnable" + (i++)); }
    }
    static int innerRunnableI = 0;
    class InnerRunnable implements Runnable {
        public void run() { Tester.event("innerRunnable" + (innerRunnableI++)); }
    }

    /* Implement a included interface */
    static class StaticInnerStuff implements Stuff {
        static int i = 0;
        public void run() { Tester.event("staticInnerStuff" + (i++)); }
    }
    static int innerStuffI = 0;
    class InnerStuff implements Stuff {
        public void run() { Tester.event("innerStuff" + (innerStuffI++)); }
    }

    /* Extend an unincluded abstract class */
    static class StaticInnerAbstractAction extends AbstractAction {
        static int i = 0;
        public void actionPerformed(ActionEvent e) { Tester.event("staticInnerAbstractAction" + (i++)); }
    }
    static int innerAbstractActionI = 0;
    class InnerAbstractAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) { Tester.event("innerAbstractAction" + (innerAbstractActionI++)); }
    }    

    /* Extend a included abstract class */
    static class StaticInnerAbstractStuff extends AbstractStuff {
        static int i = 0;
        public void run() { Tester.event("staticInnerAbstractStuff" + (i++)); }
    }
    static int innerAbstractStuffI = 0;
    class InnerAbstractStuff extends AbstractStuff {
        public void run() { Tester.event("innerAbstractStuff" + (innerAbstractStuffI++)); }
    }    
}

aspect Aspect {
    static void advise(String str, int start, int num) {
        for (int i = start; i < num; i++) {
            addAll(str, i);
        }
    }
    static void advise(String str, int num) {
        advise(str, 0, num);
    }
    static void advise(String str, int[] ints) {
        for (int i = 0; i < ints.length; i++) {
            addAll(str, ints[i]);
        }
    }
    static void addAll(String str, int i) {
        Tester.expectEvent("before-" + str + "-r" + i);
        Tester.expectEvent("after-" + str + "-r" + i);
        Tester.expectEvent("around-" + str + "-r" + i);
    }
    static void add(String str) {
        Tester.event(str + "-" + Inners.s);
    }

    /* unincludedInterfaces */
    pointcut receptionsRunnable(): receptions(void run()) && instanceof(Runnable);
    static { advise("receptions-ui", 7); }
    static before(): receptionsRunnable() { add("before-receptions"); }
    static after(): receptionsRunnable() { add("after-receptions"); }
    static around() returns void: receptionsRunnable() { add("around-receptions"); proceed(); }

    pointcut executionsRunnable(): executions(void run()) && instanceof(Runnable);
    static { advise("executions-ui", 7); }
    static before(): executionsRunnable() { add("before-executions"); }
    static after(): executionsRunnable() { add("after-executions"); }
    static around() returns void: executionsRunnable() { add("around-executions"); proceed(); }

    pointcut receptionsStaticInnerRunnable(): receptions(void run()) && instanceof(Inners.StaticInnerRunnable);
    static { advise("receptions-ui", new int[]{0,4}); }
    static before(): receptionsStaticInnerRunnable() { add("before-receptions"); }
    static after(): receptionsStaticInnerRunnable() { add("after-receptions"); }
    static around() returns void: receptionsStaticInnerRunnable() { add("around-receptions"); proceed(); }

    pointcut executionsStaticInnerRunnable(): executions(void run()) && instanceof(Inners.StaticInnerRunnable);
    static { advise("executions-ui", new int[]{0,4}); }
    static before(): executionsStaticInnerRunnable() { add("before-executions"); }
    static after(): executionsStaticInnerRunnable() { add("after-executions"); }
    static around() returns void: executionsStaticInnerRunnable() { add("around-executions"); proceed(); }

    pointcut receptionsInnerRunnable(): receptions(void run()) && instanceof(Inners.InnerRunnable);
    static { advise("receptions-ui", new int[]{1,5}); }
    static before(): receptionsInnerRunnable() { add("before-receptions"); }
    static after(): receptionsInnerRunnable() { add("after-receptions"); }
    static around() returns void: receptionsInnerRunnable() { add("around-receptions"); proceed(); }

    pointcut executionsInnerRunnable(): executions(void run()) && instanceof(Inners.InnerRunnable);
    static { advise("executions-ui", new int[]{1,5}); }
    static before(): executionsInnerRunnable() { add("before-executions"); }
    static after(): executionsInnerRunnable() { add("after-executions"); }
    static around() returns void: executionsInnerRunnable() { add("around-executions"); proceed(); }



    /* includedInterfaces */
    pointcut receptionsStuff(): receptions(void run()) && instanceof(Stuff);
    static { advise("receptions-ii", 7); }
    static before(): receptionsStuff() { add("before-receptions"); }
    static after(): receptionsStuff() { add("after-receptions"); }
    static around() returns void: receptionsStuff() { add("around-receptions"); proceed(); }

    pointcut executionsStuff(): executions(void run()) && instanceof(Stuff);
    static { advise("executions-ii", 7); }
    static before(): executionsStuff() { add("before-executions"); }
    static after(): executionsStuff() { add("after-executions"); }
    static around() returns void: executionsStuff() { add("around-executions"); proceed(); }

    pointcut receptionsStaticInnerStuff(): receptions(void run()) && instanceof(Inners.StaticInnerStuff);
    static { advise("receptions-ii", new int[]{0,4}); }
    static before(): receptionsStaticInnerStuff() { add("before-receptions"); }
    static after(): receptionsStaticInnerStuff() { add("after-receptions"); }
    static around() returns void: receptionsStaticInnerStuff() { add("around-receptions"); proceed(); }

    pointcut executionsStaticInnerStuff(): executions(void run()) && instanceof(Inners.StaticInnerStuff);
    static { advise("executions-ii", new int[]{0,4}); }
    static before(): executionsStaticInnerStuff() { add("before-executions"); }
    static after(): executionsStaticInnerStuff() { add("after-executions"); }
    static around() returns void: executionsStaticInnerStuff() { add("around-executions"); proceed(); }

    pointcut receptionsInnerStuff(): receptions(void run()) && instanceof(Inners.InnerStuff);
    static { advise("receptions-ii", new int[]{1,5}); }
    static before(): receptionsInnerStuff() { add("before-receptions"); }
    static after(): receptionsInnerStuff() { add("after-receptions"); }
    static around() returns void: receptionsInnerStuff() { add("around-receptions"); proceed(); }

    pointcut executionsInnerStuff(): executions(void run()) && instanceof(Inners.InnerStuff);
    static { advise("executions-ii", new int[]{1,5}); }
    static before(): executionsInnerStuff() { add("before-executions"); }
    static after(): executionsInnerStuff() { add("after-executions"); }
    static around() returns void: executionsInnerStuff() { add("around-executions"); proceed(); }



    /* unincludedAbstractClases */
    pointcut receptionsAbstractAction():
        receptions(void actionPerformed(ActionEvent)) && instanceof(AbstractAction);
    static { advise("receptions-ua", 7); }
    static before(): receptionsAbstractAction() { add("before-receptions"); }
    static after(): receptionsAbstractAction() { add("after-receptions"); }
    static around() returns void: receptionsAbstractAction() { add("around-receptions"); proceed(); }

    pointcut executionsAbstractAction():
        executions(void actionPerformed(ActionEvent)) && instanceof(AbstractAction);
    static { advise("executions-ua", 7); }
    static before(): executionsAbstractAction() { add("before-executions"); }
    static after(): executionsAbstractAction() { add("after-executions"); }
    static around() returns void: executionsAbstractAction() { add("around-executions"); proceed(); }

    pointcut receptionsActionListener():
        receptions(void actionPerformed(ActionEvent)) && instanceof(ActionListener);
    static { advise("receptions-ua", 11); }
    static before(): receptionsActionListener() { add("before-receptions"); }
    static after(): receptionsActionListener() { add("after-receptions"); }
    static around() returns void: receptionsActionListener() { add("around-receptions"); proceed(); }

    pointcut executionsActionListener():
        executions(void actionPerformed(ActionEvent)) && instanceof(ActionListener);
    static { advise("executions-ua", 11); }
    static before(): executionsActionListener() { add("before-executions"); }
    static after(): executionsActionListener() { add("after-executions"); }
     static around() returns void: executionsActionListener() { add("around-executions"); proceed(); }    

    pointcut receptionsStaticInnerAbstractAction():
        receptions(void actionPerformed(ActionEvent)) && instanceof(Inners.StaticInnerAbstractAction);
    static { advise("receptions-ua", new int[]{0,4,7}); }
    static before(): receptionsStaticInnerAbstractAction() { add("before-receptions"); }
    static after(): receptionsStaticInnerAbstractAction() { add("after-receptions"); }
    static around() returns void: receptionsStaticInnerAbstractAction() { add("around-receptions"); proceed(); }

    pointcut executionsStaticInnerAbstractAction():
        executions(void actionPerformed(ActionEvent)) && instanceof(Inners.StaticInnerAbstractAction);
    static { advise("executions-ua", new int[]{0,4,7}); }
    static before(): executionsStaticInnerAbstractAction() { add("before-executions"); }
    static after(): executionsStaticInnerAbstractAction() { add("after-executions"); }
    static around() returns void: executionsStaticInnerAbstractAction() { add("around-executions"); proceed(); }

    pointcut receptionsInnerAbstractAction():
        receptions(void actionPerformed(ActionEvent)) && instanceof(Inners.InnerAbstractAction);
    static { advise("receptions-ua", new int[]{1,5,8}); }
    static before(): receptionsInnerAbstractAction() { add("before-receptions"); }
    static after(): receptionsInnerAbstractAction() { add("after-receptions"); }
    static around() returns void: receptionsInnerAbstractAction() { add("around-receptions"); proceed(); }

    pointcut executionsInnerAbstractAction():
        executions(void actionPerformed(ActionEvent)) && instanceof(Inners.InnerAbstractAction);
    static { advise("executions-ua", new int[]{1,5,8}); }
    static before(): executionsInnerAbstractAction() { add("before-executions"); }
    static after(): executionsInnerAbstractAction() { add("after-executions"); }
    static around() returns void: executionsInnerAbstractAction() { add("around-executions"); proceed(); }
    
    
    /* includedAbstractClases */
    pointcut receptionsAbstractStuff(): receptions(void run()) && instanceof(AbstractStuff);
    static { advise("receptions-ia", 7); }
    static before(): receptionsAbstractStuff() { add("before-receptions"); }
    static after(): receptionsAbstractStuff() { add("after-receptions"); }
    static around() returns void: receptionsAbstractStuff() { add("around-receptions"); proceed(); }

    pointcut executionsAbstractStuff(): executions(void run()) && instanceof(AbstractStuff);
    static { advise("executions-ia", 7); }
    static before(): executionsAbstractStuff() { add("before-executions"); }
    static after(): executionsAbstractStuff() { add("after-executions"); }
    static around() returns void: executionsAbstractStuff() { add("around-executions"); proceed(); }

    pointcut receptionsStuff2(): receptions(void run()) && instanceof(Stuff);
    static { advise("receptions-ia", 11); }
    static before(): receptionsStuff2() { add("before-receptions"); }
    static after(): receptionsStuff2() { add("after-receptions"); }
    static around() returns void: receptionsStuff2() { add("around-receptions"); proceed(); }

    pointcut executionsStuff2(): executions(void run()) && instanceof(Stuff);
    static { advise("executions-ia", 11); }
    static before(): executionsStuff2() { add("before-executions"); }
    static after(): executionsStuff2() { add("after-executions"); }
    static around() returns void: executionsStuff2() { add("around-executions"); proceed(); }    

    pointcut receptionsStaticInnerAbstractStuff():
        receptions(void run()) && instanceof(Inners.StaticInnerAbstractStuff);
    static { advise("receptions-ia", new int[]{0,4,7}); }
    static before(): receptionsStaticInnerAbstractStuff() { add("before-receptions"); }
    static after(): receptionsStaticInnerAbstractStuff() { add("after-receptions"); }
    static around() returns void: receptionsStaticInnerAbstractStuff() { add("around-receptions"); proceed(); }

    pointcut executionsStaticInnerAbstractStuff():
        executions(void run()) && instanceof(Inners.StaticInnerAbstractStuff);
    static { advise("executions-ia", new int[]{0,4,7}); }
    static before(): executionsStaticInnerAbstractStuff() { add("before-executions"); }
    static after(): executionsStaticInnerAbstractStuff() { add("after-executions"); }
    static around() returns void: executionsStaticInnerAbstractStuff() { add("around-executions"); proceed(); }

    pointcut receptionsInnerAbstractStuff():
        receptions(void run()) && instanceof(Inners.InnerAbstractStuff);
    static { advise("receptions-ia", new int[]{1,5,8}); }
    static before(): receptionsInnerAbstractStuff() { add("before-receptions"); }
    static after(): receptionsInnerAbstractStuff() { add("after-receptions"); }
    static around() returns void: receptionsInnerAbstractStuff() { add("around-receptions"); proceed(); }

    pointcut executionsInnerAbstractStuff():
        executions(void run()) && instanceof(Inners.InnerAbstractStuff);
    static { advise("executions-ia", new int[]{1,5,8}); }
    static before(): executionsInnerAbstractStuff() { add("before-executions"); }
    static after(): executionsInnerAbstractStuff() { add("after-executions"); }
    static around() returns void: executionsInnerAbstractStuff() { add("around-executions"); proceed(); }
}

interface Stuff {
    public void run();
}

abstract class AbstractStuff implements Stuff {
    public abstract void run();
}
