
package language;

public class Initialization {
    public static void main(String[] argList) {
        Thing thing = new Thing();
        if (12 != thing.counter) {
            System.err.println("expected 12, got " + thing.counter);
        }
        thing = new Thing(20);
        if (32 != thing.counter) {
            System.err.println("expected 32, got " + thing.counter);
        }
        thing = new AnotherThing();
        if (2 != thing.counter) {
            System.err.println("expected 2, got " + thing.counter);
        }
        thing = new AnotherThing(20);
        if (23 != thing.counter) {
            System.err.println("expected 23, got " + thing.counter);
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
class Thing {
    int counter;
    Thing() {
        this(1);
    }
    Thing(int value) {
        counter = value;
    }
}

class AnotherThing extends Thing {
    AnotherThing() {
        super();
    }
    
    AnotherThing(int i) {
        super(++i);
    }
}

aspect A {
    /**
     * After any call to any constructor, fix up the thing.
     * In AspectJ 1.1, this only affects callers in the input
     * classes or source files, but not super calls.
     */
    after() returning (Thing thing): 
            call(Thing.new(..)) { 
        postInitialize(thing);
    }

    /**
     * After executing the int constructor, fix up the thing.
     * This works regardless of how the constructor was called
     * (by outside code or by super), but only for the
     * specified constructors.
     */
    after() returning (Thing thing): execution(Thing.new(int)) { 
        thing.counter++;
    }

    /**
     * DANGER -- BAD!!  Before executing the int constructor,
     * this uses the target object, which is not constructed.
     */
    before (Thing thing): this(thing) && execution(Thing.new(int)) { 
        // thing.counter++; // DANGER!! thing not constructed yet.
    }
    
    /**
      * This advises all Thing constructors in one join point, 
      * even if they call each other with this().
      */
    after(Thing thing) returning: this(thing) 
            && initialization(Thing.new(..)) {
       thing.counter++;
    }
    
    protected void postInitialize(Thing thing) {
        thing.counter += 10;
    }
}
//END-SAMPLE language-initialization

