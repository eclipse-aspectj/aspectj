import org.aspectj.testing.Tester;
import org.aspectj.lang.*;
import org.aspectj.lang.reflect.*;
import java.util.*;

public class Coverage extends Args {
    public static void main(String[] args) {
        Args.args = args;
	Args.c = new C();
	Args.c.x += 1;
	Args.c.mumble("foo");
    }
}

class C {
    public int x;
    void mumble(String arg1) {
        Args.arg1 = arg1;
	try {
	    throw new Exception("test");
	} catch (Exception e) {
	}
    }
}

class Args {
    public static String[] args;
    public static String arg1;
    public static C c;
}

class SubC {
}

aspect JoinPoints extends Helper {    

    before(): set(* C.*) {
        JoinPoint jp = thisJoinPoint;
	p("sets", jp);
        a(jp.getKind(),"field-set");
        Integer integer = null;
        Object o = null;
        try {
            o = jp.getTarget().getClass().getField
                (jp.getSignature().getName()).get(jp.getTarget());
        } catch (Exception e) { a(e); return; }

        try {
            integer = (Integer)o;
        } catch (ClassCastException cce) {
            a(ni(o, Integer.class, "set-1"));
            return;
        }
        a(integer.intValue() == 0, "set value i != 0");
        o = jp.getArgs()[0];
        try {
            integer = (Integer) o;
        } catch (ClassCastException cce) {
            a(ni(o, Integer.class, "set-2"));
            return;
        }
        a(integer.intValue() == 1, "set newvalue i != 1");
        Object ex = jp.getThis();
        JoinPoint.StaticPart sp = jp.getStaticPart();
        FieldSignature fs = null;
        Signature sig = jp.getSignature();
        try {
            fs = (FieldSignature)sig;
        } catch (ClassCastException cce) {
            a(ni(sig, FieldSignature.class, "set-1"));

        }
    }
    
    before(): get(* C.*) {
        JoinPoint jp = thisJoinPoint;
        p("gets", jp);
        a(jp.getKind(),"field-get");
        Integer integer = null;
        Object o = null;
        try {
            o = jp.getTarget().getClass().getField
                (jp.getSignature().getName()).get(jp.getTarget());
        } catch (Exception e) { a(e); }
        try {
            integer = (Integer) o;
        } catch (ClassCastException cce) {
            a(ni(o, Integer.class, "get"));
            return;
        }
        a(integer.intValue() == 0, "get value i != 0");
        Object ex = jp.getThis();
        JoinPoint.StaticPart sp = jp.getStaticPart();
        FieldSignature fs = null;
        Signature sig = jp.getSignature();
        try {
            fs = (FieldSignature)sig;
        } catch (ClassCastException cce) {
            a(ni(sig, FieldSignature.class, "get"));
            return;
        }
    }
    
    before(): execution(* C.*(..)) {
        JoinPoint jp = thisJoinPoint;
	p("executions", jp);
        a(jp.getKind(), "method-execution");
        Signature sig = jp.getSignature();
        CodeSignature cs = null;
        try {
            cs = (CodeSignature)sig;
        } catch (ClassCastException cce) {
            a(ni(sig, CodeSignature.class, "execution"));
        }
        String name = cs.getName();
        got(name + ".execution");
        if (name.equals("mumble")) {
            Object[] params = jp.getArgs();
            Object target = jp.getThis();
            String arg1 = null;
            try {
                arg1 = (String) params[0];
            } catch (ArrayIndexOutOfBoundsException ae) {
                a("params.size < 1");
                return;
            } catch (ClassCastException cce) {
                a(ni(params[0], String.class));
                return;
            }
            a(target == Args.c, target + " != " + Args.c + ", should be c - 2");
        }        
    }
    before(): call(C.new(..)) {
        JoinPoint jp = thisJoinPoint;
	p("calls", jp);
        a(jp.getKind(),"constructor-call");
        Signature sig = jp.getSignature();
        ConstructorSignature cs = null;
        try {
            cs = (ConstructorSignature)sig;
        } catch (ClassCastException cce) {
            a(ni(cs, ConstructorSignature.class));
        }
        Object[] params = jp.getArgs();
        Object target = jp.getThis();
        String name = cs.getName();
        a(name, "<init>");
        a(new Integer(params.length), new Integer(0));
        a(target == Args.c, target + " != " + Args.c + ", should be c - 3");        
    }
    before(): execution(C.new(..)) {
        JoinPoint jp = thisJoinPoint;
	p("executions", jp);
        a(jp.getKind(),"constructor-execution");
        Signature sig = jp.getSignature();
        ConstructorSignature cs = null;
        try {
            cs = (ConstructorSignature)sig;
        } catch (ClassCastException cce) {
            a(ni(sig, ConstructorSignature.class));
        }
        Object[] params = jp.getArgs();
        Object target = jp.getThis();
        String name = cs.getName();
        a(name, "<init>");
        a(new Integer(params.length), new Integer(0));
    }
    before(): handler(*) && !within(JoinPoints) {
        JoinPoint jp = thisJoinPoint;
	p("handle", jp);
        a(jp.getKind(),"exception-handler");
        Signature sig = jp.getSignature();
        CatchClauseSignature ccs = null;
        try {
            ccs = (CatchClauseSignature)sig;
        } catch (ClassCastException cce) {
            a(ni(sig, CatchClauseSignature.class));
        }
        Throwable t = null;
        try {
            t = (Throwable)jp.getArgs()[0];
        } catch (ArrayIndexOutOfBoundsException _) {
            a("handlers out of bounds");
        } catch (ClassCastException __) {
            a(ni(jp.getArgs()[0], Throwable.class, "handlers"));
        }
        a(t.getMessage(), "test");
        Object ex = jp.getThis();
        a(ex, Args.c);
        JoinPoint.StaticPart sp = jp.getStaticPart();
    } 
}

abstract aspect Helper {

    final static String[] strings = {
        "mumble.execution",
        "calls",
        "executions",
        "gets",
        "sets",
        "executions",
        "handle",
    };

    static Map hash = new HashMap();
    static List names = new Vector();
    static {
        for (int i = 0; i < strings.length; i++) {
            need(strings[i]);
        }
    }

    after(): execution(* main(..)) {
        checkAll();
    }

    static void checkAll() {
        Iterator iter = names.iterator();
        while (iter.hasNext()) {
            Object method = iter.next();
            Object call = hash.get(method); 
            a(call != null, method + " was not called");
        }
    }

    static void got(Object method) {
        hash.put(method, method);
    }

    static void need(Object method) {
        names.add(method);
    }
    
    static void a(Object o1, Object o2) {
        String msg = "" + o1 + " != " + o2;
        a(o1, o2, msg);
    }
    static void a(Object o1, Object o2, String msg) {
        if      (o1 != null) a(o1.equals(o2), msg);
        else if (o2 != null) a(o2.equals(o1), msg);
    }
    static void a(boolean b, Object o) {
        Tester.check(b, o + "");
    }
    static void a(Object o) {
        a(false, o);
    }
    static String ni(Object o, Class c, String s) {
        return ni("(" + s + ") " + o, c);
    }
    static String ni(Object o, Class c) {
        return o +
            (o == null ? " is null " : " is a " + o.getClass()) +
            " is not a " + c;
    }
    static boolean verbose = false;
    static void p(Object o) {
        if (verbose) System.out.println(o);
    }
    static void p(Object o, JoinPoint jp) {
        got(o);
    }
    static void po(Object o) {
        boolean ov = verbose;
        verbose = true;
        p(o);
        verbose = ov;
    }
}
