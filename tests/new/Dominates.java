import org.aspectj.testing.*;

/******************************
Full_Name: Eelco Rommes
Version: ajc version 0.7beta7 (built Oct 6, 2000 4:03 PM PST) running on java 1.3.0
OS: Win 98
Submission from: mede.serc.nl (192.87.7.62)


I have one aspect using introduction to  make some classes 
implement certain interfaces. In another aspect, these 
introductions are used to define pointcuts. The compiler 
warns me, however, that the advice coupled to these pointcuts 
have no target. This, because it apparently does not see 
how the introduction-aspect changes the class-hierarchy.
I have tried to use 'dominates' to force aspectJ to evaluate 
the introduction-aspect first, but it doesn't work.
******************************/                               

public class Dominates {
    public static void main(String[] args) {
        new Dominates().go(args);
    }

    void go(String[] args) {
        new A().run();
        new ExtendsA().run();
        new ExtendsRunnable().run();
        Tester.check(Flags.run1,        "Flags.run1");
        Tester.check(Flags.run2,        "Flags.run2");
        Tester.check(Flags.run3,        "Flags.run3");
        Tester.check(Flags.run4,        "Flags.run4");
        Tester.check(ExtendsFlags.run1, "ExtendsFlags.run1");
        Tester.check(ExtendsFlags.run2, "ExtendsFlags.run2");
        Tester.check(ExtendsFlags.run3, "ExtendsFlags.run3");
        Tester.check(ExtendsFlags.run4, "ExtendsFlags.run4");
        Tester.check(ExtendsFlags.run5, "ExtendsFlags.run5");
        Tester.check(ExtendsFlags.run6, "ExtendsFlags.run6");
    }
}

class A {
    
}

class Flags {
    public static boolean run1 = false;
    public static boolean run2 = false;
    public static boolean run3 = false;
    public static boolean run4 = false;            
}

aspect Aspect0 {
    pointcut run(): call(* run(..)) && target(A);
    before(): run() {
        Flags.run1 = true;
    }
}

aspect Aspect00 {
    pointcut run(): call(* run(..)) && target(Runnable+);
    before(): run() {
        Flags.run4 = true;
    }
}

aspect Aspect1 {
   declare parents:  A implements Runnable;
   public void A.run() {}
}

aspect Aspect2 {
    pointcut run(): call(* run(..)) && target(A);
    before(): run() {
        Flags.run2 = true;
    }
}

aspect Aspect3 {
    pointcut run(): call(* run(..)) && target(Runnable+);
    before(): run() {
        Flags.run3 = true;
    }
}

// ------------------------------

class ExtendsA {
    
}

class ExtendsRunnable {
    public void run() {}
}

class ExtendsFlags {
    public static boolean run1 = false;
    public static boolean run2 = false;
    public static boolean run3 = false;
    public static boolean run4 = false;
    public static boolean run5 = false;
    public static boolean run6 = false;     
}

aspect AspectExtends0 {
    pointcut run(): call(* run(..)) && target(ExtendsA);
    before(): run() {
        ExtendsFlags.run1 = true;
    }
}

aspect AspectExtends00 {
    pointcut run(ExtendsRunnable r): call(* run(..)) && target(r);
    before(ExtendsRunnable r): run(r) {
        if (r instanceof ExtendsA) {
            ExtendsFlags.run5 = true;
        } else {
            ExtendsFlags.run6 = true;
        }
    }
}

aspect AspectExtends1 {
    declare parents: ExtendsA extends ExtendsRunnable;
    public void ExtendsA.run() {}
}

aspect AspectExtends2 {
    pointcut run(): call(* run(..)) && target(ExtendsA);
    before(): run() {
        ExtendsFlags.run2 = true;
    }
}

aspect AspectExtends3 {
    pointcut run(ExtendsRunnable r): call(* run(..)) && target(r);
    before(ExtendsRunnable r): run(r) {
        if (r instanceof ExtendsA) {
            ExtendsFlags.run3 = true;
        } else {
            ExtendsFlags.run4 = true;
        }
    }
}
