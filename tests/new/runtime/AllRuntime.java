
import org.aspectj.lang.*;
import org.aspectj.lang.reflect.*;

/**
 * Run via main or driveTest.
 * If you want the verbose output,
 * use "-p{rint}" to print when invoking via main,
 * or set resultCache with your result sink before running:
 * <pre>StringBuffer sb = new StringBuffer();
 * AllRuntime.resultCache(sb);
 * int errors = AllRuntime.driveTest();
 * System.err.println(sb.toString());
 * System.err.println("Errors: " + errors);</pre>
 * <p>
 * This was written to run in a 1.1 VM,
 * outside the Tester or Collections or...
 * 
 * @testcase PR#474 rt.java uses 1.2-only variant of Class.forName 
 */ 
public class AllRuntime {
    public static void resultCache(StringBuffer cache) {
        A.resultCache(cache);
    }

    public static void main(String[] args) {
        StringBuffer result = null;
        if ((null != args) && (0 < args.length) 
            && (args[0].startsWith("-p"))) {
           result = new StringBuffer();
           resultCache(result);
        }
        int errors = driveTest();
        A.log("Errors: " + errors);
        if (null != result) {
            System.err.println(result.toString());
        }
    }

    /** @return number of errors detected */
    public static int driveTest() {
        int result = 0;
        boolean ok = testNoAspectBoundException();
        if (!ok) result++;
        A.log("testNoAspectBoundException: " + ok);
        ok = testMultipleAspectsBoundException();
        if (!ok) result++;
        A.log("testMultipleAspectsBoundException: " + ok);

        TargetClass me = new TargetClass();

        ok = me.catchThrows();
        if (!ok) result++;


        int temp = me.publicIntMethod(2);
        if (temp != 12) result++;

        StringBuffer sb = new StringBuffer();
        sb.append("" + me);  // callee-side join point
        if (sb.length() < 1) result++;
        A.log("Callee-side join point " + sb.toString());

        try {
            ok = false;
            me.throwException = true;
            me.run();
        } catch (SoftException e) {
            ok = true;
        }
        if (!ok) result++;
        A.log("SoftException: " + ok);
        A a = A.aspectOf();
        if (null != a) {
            ok = a.report();
            if (!ok) result++;
            A.log("  => all advice was run: " + ok);
        }
        return result;
    }

    /** todo: need test case for multiple aspects */
    public static boolean testMultipleAspectsBoundException() {
        return true;
    }

    public static boolean testNoAspectBoundException() {
        boolean result = false;
        try {
            B a = B.aspectOf(new Object());
        } catch (NoAspectBoundException e) {
            result = true;
        }
        return result;
    }
}


/** This has all relevant join points */
class TargetClass {
    private static int INDEX;
    static {
        INDEX = 10;
    }
    private int index = INDEX;
    private int shadow = index;

    public int publicIntMethod(int input) { 
        return privateIntMethod(input); 
    }

    public boolean catchThrows() {
        try {
            throw new Exception("hello");
        } catch (Exception e) {
            if (null != e) return true;
        }
        return false;
    }

    /** print in VM-independent fashion */
    public String toString() { 
        return "TargetClass " + shadow; 
    }

    private int privateIntMethod(int input) { 
        return shadow = index += input; 
    }
}

/** used only for NoAspectBoundException test */
aspect B perthis(target(TargetClass)) { }

aspect A {
    /** log goes here if defined */
    private static StringBuffer CACHE;
    /** count number of join points hit */
    private static int jpIndex = 0;
    /** count number of A created */
    private static int INDEX = 0;
    /** index of this A */
    private int index;
    /** count for each advice of how many times invoked */
    private final int[] adviceHits;
    A() { 
        index = INDEX++; 
        adviceHits = new int[21];
    }

    public static void resultCache(StringBuffer cache) {
        if (CACHE != cache) CACHE = cache;
    }

    public static void log(String s) { 
        StringBuffer cache = CACHE;
        if (null != cache) {
            cache.append(s);
            cache.append("\n");
        }
    }

    private void log(int i) { adviceHits[i]++; }

    /** report how many times each advice was run
     * logging report.
     * @return false if any advice was not hit 
     */
    public boolean report() { 
        StringBuffer sb = new StringBuffer();
        boolean result = report(this, sb);
        log(sb.toString()); 
        return result;
    }

    /** report how many times each advice was run
     * @return false if any advice was not hit 
     */
    public static boolean report(A a, StringBuffer sb) { 
        boolean result = true;
        if (null == a.adviceHits) {
            sb.append("[]");
        } else {
            sb.append("[");
            int[] adviceHits = a.adviceHits;
            for (int i = 0; i < adviceHits.length; i++) {
                if (i > 0) sb.append(", ");
                sb.append(i+"="+adviceHits[i]);
                if (result && (0 == adviceHits[i])) {
                    result = false;
                }
            }
            sb.append("]");
        }
        return result;
    }

    public static void throwsException() throws Exception { 
        throw new Exception("exception"); 
    }
    public String toString() { return  "A " + index; }

    //-------------------------------------- pointcuts
    pointcut safety() 
        : !within(A) 
        && !cflow(execution(String TargetClass.toString()))
        && !call(String TargetClass.toString())
        ;
    pointcut  intMethod() : call(int TargetClass.publicIntMethod(int)); 


    //-------------------------------------- declare, introductions
    declare parents : TargetClass implements Runnable;
    declare soft : Exception : execution(void TargetClass.run());
    
    /** unused - enable to throw exception from run() */
    public boolean TargetClass.throwException;
    public void TargetClass.run() {
        if (throwException) throwsException();
    }

    //-------------------------------------- advice 
    /** was callee-side call join point, now is execution */ // todo: not being invoked, though TargetClass.toString is???
    before() : execution(public String toString()) 
        && target(TargetClass) { 
        /* comment out test to avoid StackOverflow
        test(thisJoinPoint, thisJoinPointStaticPart, this, 
             "before() : call(String TargetClass.toString())"); 
        */
        log(1);
    }

    /** caller-side call join point */
    before() : call(int TargetClass.privateIntMethod(int))  {
        test(thisJoinPoint, thisJoinPointStaticPart, this,
             "before() : call(int TargetClass.privateIntMethod()) ");
        log(2);
    }
    /** call join point */
    before() : intMethod() { 
        test(thisJoinPoint, thisJoinPointStaticPart, this,
             "before() : pc() ");
        log(3);
    }

    /** execution join point */
    before() : execution(int TargetClass.privateIntMethod(int)) {
        test(thisJoinPoint, thisJoinPointStaticPart, this,
             "before() : execution(int TargetClass.privateIntMethod()) ");
        log(4);
    }

    /** execution join point for constructor */
    before() : execution(TargetClass.new(..))  {
        test(thisJoinPoint, thisJoinPointStaticPart, this,
             "before() : execution(TargetClass.new(..))  ");
        log(5);
    }

    /** initialization join point */
    before() : initialization(TargetClass+.new(..)) {
        test(thisJoinPoint, thisJoinPointStaticPart, this,
             "before() : initialization(TargetClass+.new(..)) ");
        log(6);
    }

    /** static initialization join point */
    before() : initialization(TargetClass+.new(..))  {
        test(thisJoinPoint, thisJoinPointStaticPart, this,
             "before() : initialization(TargetClass+.new(..))  ");
        log(7);
    }

    /** cflow join point */
    before() : cflow(execution(int TargetClass.publicIntMethod(int)))  
        && safety() {
        test(thisJoinPoint, thisJoinPointStaticPart, this,
             "before() : cflow(execution(int TargetClass.publicIntMethod(int)))");
        log(8);
    }

    /** cflowbelow join point */
    before() : cflowbelow(execution(int TargetClass.publicIntMethod(int)))  
        && safety() {
        test(thisJoinPoint, thisJoinPointStaticPart, this,
             "before() : cflowbelow(execution(int TargetClass.publicIntMethod(int)))");
        log(9);
    }

    /** initialization join point */
    before() : initialization(TargetClass+.new(..)) {
        test(thisJoinPoint, thisJoinPointStaticPart, this,
             "before() : initialization(TargetClass+.new(..)) ");
        log(10);
    }

    /** field set join point */
    before() : set(int TargetClass.index) && safety() {
        test(thisJoinPoint, thisJoinPointStaticPart, this,
             "before() : set(int TargetClass.index)  ");
        log(11);
    }

    /** field get join point */
    before() : get(int TargetClass.index)  && safety() {
        test(thisJoinPoint, thisJoinPointStaticPart, this,
             "before() : get(int TargetClass.index) ");
        log(12);
    }

    /** within join point (static) */
    before() : within(TargetClass+) && safety() {
        test(thisJoinPoint, thisJoinPointStaticPart, this,
             "before() : within(TargetClass+) ");
        log(13);
    }

    /** withincode join point (static) */
    before() : withincode(int TargetClass+.publicIntMethod(int)) && safety() {
        test(thisJoinPoint, thisJoinPointStaticPart, this,
             "before() : withincode(int TargetClass+.publicIntMethod(int)) ");
        log(14);
    }

    /** this join point */
    before(TargetClass t) : this(t)  && safety() {
        test(thisJoinPoint, thisJoinPointStaticPart, this,
             "before(TargetClass t) : this(t)  && safety() This t: " + t + " this: " + this);
        log(15);
    }

    /** target join point */
    before(TargetClass t) : target(t)  && safety() {
        test(thisJoinPoint, thisJoinPointStaticPart, this,
             "before(TargetClass t) : target(t)  && safety() target t: " + t + " this: " + this);
        log(16);
    }

    /** args join point */
    before(int i) : args(i)  && safety() {
        test(thisJoinPoint, thisJoinPointStaticPart, this,
             "before(int i) : args(i)  && safety() args i: " + i);
        log(17);
    }

    /** handler join point */
    before() : handler(Exception) { // && args(e) {
        test(thisJoinPoint, thisJoinPointStaticPart, this,
             "before(Throwable e) : handler(Throwable) && args(e) && within(TargetClass+) args e: " ); 
        log(18);
    }

    /** if pcd join point */
    before(int i) : args(i) && if(i > 0) && safety() {
        test(thisJoinPoint, thisJoinPointStaticPart, this,
             "before(int i) : args(i) && if(i > 0) && safety() args i: " + i);
        log(19);
    }

    /** call join point for constructor */
    before() : call(TargetClass.new(..))  {
        test(thisJoinPoint, thisJoinPointStaticPart, this,
             "before() : call(TargetClass.new(..))  ");
        log(20);
    }

    /** everything join point */
    before(TargetClass t) 
        : (target(t) )
        && (call(int TargetClass.privateIntMethod(int))
            || execution(int TargetClass.privateIntMethod(int))
            || initialization(TargetClass.new())
            || (cflow(call(int TargetClass.privateIntMethod(int)))
                && !cflowbelow(call(int TargetClass.privateIntMethod(int))))
            )
        && (!cflow(call(void TargetClass.catchThrows())))
        && (!call(void TargetClass.run()))
        && (!set(int TargetClass.index))
        && (!get(int TargetClass.index))
        && safety() 
        && if(null != t) {
        test(thisJoinPoint, thisJoinPointStaticPart, this,
             "everything"); // todo: add args
        log(0);
    }

    private void test(JoinPoint jp, JoinPoint.StaticPart jpsp, Object tis, 
                      String context) {
        StringBuffer sb = new StringBuffer();
        sb.append("\n join pt: " + jpIndex++);
        sb.append("\n      jp: " + jp);
        render(jp, sb);
        sb.append("\n    jpsp: " + jpsp);
        sb.append("\n     tis: " + tis);
        sb.append("\n    this: " + this);
        sb.append("\n context: " + context);
        log(sb.toString());
    }

    private void render(JoinPoint jp, StringBuffer sb) {
        if (null == jp) {
            sb.append("null");
        } else {
            //sb.append("\n        args: " + jp.getArgs());
            sb.append("\n        args: ");
            render(jp.getArgs(), sb);
            sb.append("\n        kind: " + jp.getKind());
            sb.append("\n         sig: " );
            render(jp.getSignature(), sb);
            sb.append("\n         loc: " );
            render(jp.getSourceLocation(), sb);
            sb.append("\n        targ: " + jp.getTarget());
            sb.append("\n        this: " + jp.getThis());
        }
    }

    /** render to check subtype of Signature, print in VM-independent fashion */
    private void render(Signature sig, StringBuffer sb) {
        if (null == sig) {
            sb.append("null");
        } else {
            if (sig instanceof AdviceSignature) {
                sb.append("AdviceSignature ");
                sb.append(sig.getName() + " " );
                sb.append(""+((AdviceSignature ) sig).getReturnType());
            } else if (sig instanceof CatchClauseSignature) {
                sb.append("CatchClauseSignature ");
                sb.append(sig.getName() + " " );
                sb.append(""+((CatchClauseSignature ) sig).getParameterType());
            } else if (sig instanceof ConstructorSignature) {
                sb.append("ConstructorSignature ");
                sb.append(sig.getName() + " " );
                sb.append(""+((ConstructorSignature) sig).getName());
            } else if (sig instanceof FieldSignature) {
                sb.append("FieldSignature ");
                sb.append(sig.getName() + " " );
                sb.append(""+((FieldSignature ) sig).getFieldType());
            } else if (sig instanceof InitializerSignature) {
                sb.append("InitializerSignature ");
                sb.append(sig.getName() + " " );
            } else if (sig instanceof MethodSignature) {
                sb.append("MethodSignature ");
                sb.append(sig.getName() + " " );
                sb.append(""+((MethodSignature) sig).getReturnType());
            } else if (sig instanceof MemberSignature) {
                sb.append("MemberSignature?? ");
                sb.append(sig.getName() + " " );
            } else if (sig instanceof CodeSignature) {
                sb.append("CodeSignature ??");
                sb.append(sig.getName() + " " );
            } else {
                sb.append("Unknown ??");
                sb.append(sig.getName() + " " );
            }
        }
    }
    private void render(SourceLocation sl, StringBuffer sb) {
        if (null == sl) {
            sb.append("null");
        } else {
            String path = sl.getFileName();
            int loc = path.lastIndexOf("/");
            if (-1 != loc) {
                path = path.substring(loc+1);
            } else {
                // todo: not portable to other machines
                loc = path.lastIndexOf("\\");
                if (-1 != loc) {
                    path = path.substring(loc+1);
                }
            }
            sb.append(path);
            sb.append(":" + sl.getLine());
            //sb.append(":" + sl.getColumn());
        }
    }

    private void render(Object[] args, StringBuffer sb) {
        if (null == args) {
            sb.append("null");
        } else {
            sb.append("Object[" + args.length + "] = {");
            for (int i = 0; i < args.length; i++) {
               if (i > 0) sb.append(", ");
               sb.append("" + args[i]);
            }
            sb.append("}");
        }
    }
}

