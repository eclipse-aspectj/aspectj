
package language;

public class Initialization {
    public static void main(String[] argList) {
        String[] expected = new String[] 
        {   "none after-String-constructor-execution after-initialization after-any-constructor-call", 
            "hello after-String-constructor-execution after-initialization after-any-constructor-call", 
            "none after-String-constructor-execution after-initialization", 
            "hi from-AnotherThing after-String-constructor-execution after-initialization"
        };
        String[] actual = new String[4]; 
        Thing thing = new Thing();
        actual[0] = new Thing().message;
        actual[1] = new Thing("hello").message;
        actual[2] = new AnotherThing().message;
        actual[3] = new AnotherThing("hi").message;
        
        StringBuffer errs = new StringBuffer();
        for (int i = 0; i < actual.length; i++) {
            if (!expected[i].equals(actual[i])) {
                errs.append("expected ");
                errs.append(expected[i]);
                errs.append(" but got ");
                errs.append(actual[i]);
                errs.append("\n");
            }
        }
        if (0 < errs.length()) {
            throw new Error(errs.toString());
        }
    }    
}
/** @author Erik Hilsdale, Wes Isberg */

// START-SAMPLE language-initialization Understanding object creation join points
/*
 * To work with an object right when it is constructed,
 * understand the differences between the join points for 
 * constructor call, constructor execution, and initialization.
 */
 // ------- examples of constructors and the ways they invoke each other.
class Thing {
    String message;
    Thing() {
        this("none");
    }
    Thing(String message) {
        this.message = message;
    }
}

class AnotherThing extends Thing {
    AnotherThing() {
        super(); // this does not append to message as the one below does.
    }
    
    AnotherThing(String message) {
        super(message + " from-AnotherThing");
    }
}

aspect InitializationSample {
    // -------- constructor-call picks out the calls
    /**
     * After any call to any constructor, fix up the thing.
     * When creating an object, there is only one call to 
     * construct it, so use call(..) avoid duplicate advice.
     * There is no target for the call, but the object
     * constructed is returned from the call.
     * In AspectJ 1.1, this only picks  out callers in the input
     * classes or source files, and it does not pick out
     * invocations via <code>super(..)</code> 
     * or <code>this(..)</code>.
     */
    after() returning (Thing thing): 
            call(Thing.new(..)) { 
        thing.message += " after-any-constructor-call";
    }

    // -------- constructor-execution picks out each body
    /**
     * After executing the String constructor, fix up the thing.
     * The object being-constructed is available as either
     * <code>this</code> or <code>target</code>.
     * This works even if the constructor was invoked using 
     * <code>super()</code> or <code>this()</code> or by code 
     * outside the control of the AspectJ compiler.
     * However, if you advise multiple constructors, you'll advise
     * a single instance being constructed multiple times 
     * if the constructors call each other.
     * In AspectJ 1.1, this only affects constructors in the input
     * classes or source files.
     */
    after(Thing thing) returning : target(thing) && 
            execution(Thing.new(String)) { 
        thing.message += " after-String-constructor-execution";
    }

    /**
     * DANGER -- BAD!!  Before executing the String constructor,
     * this uses the target object, which is not constructed.
     */
    before (Thing thing): this(thing) && execution(Thing.new(String)) { 
        // DANGER!! thing not constructed yet.
        //thing.message += " before-String-constructor-execution";
    }
    
    // -------- initialization picks out any construction, once
    /**
     * This advises all Thing constructors in one join point, 
     * even if they call each other with <code>this()</code>, etc.
     * The object being-constructed is available as either
     * <code>this</code> or <code>target</code>.
     * In AspectJ 1.1, this only affects types input to the compiler.
     */
    after(Thing thing) returning: this(thing) 
            && initialization(Thing.new(..)) {
        thing.message += " after-initialization";
    }
}
//END-SAMPLE language-initialization

aspect B {
    static boolean log = false;
    before() : withincode(void Initialization.main(String[])) 
            && call(Thing.new(..)) && if(log) {
        System.out.println("before: " + thisJoinPointStaticPart.getSourceLocation().getLine());
    }
    before() : within(A) && adviceexecution() && if(log)  {
        System.out.println("advice: " + thisJoinPointStaticPart.getSourceLocation().getLine());
    }
}
