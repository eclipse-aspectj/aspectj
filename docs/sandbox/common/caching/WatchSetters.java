//@author Ricardo Giacomin, Wes Isberg
//XXX author n/a at old address for explicit authorization


//START-SAMPLE caching-dirty-reflectiveSetters Use getter/setter pattern to track dirtiness
package caching;

import java.lang.reflect.Method;

/**
 * Watch setters to skip if new value is same as old
 * or to set a dirty flag otherwise.
 * Clients classes opt-in by implementing IWatched,
 * and anyone can read the dirty and dirty-valid flags.
 * <pre>
 * class Foo implements WatchSetters.IWatched {
 *    ...
 * }
 * Foo foo = new Foo();
 * ...
 * if (!foo.isDirtyValid() || foo.isDirty()) {
 *    foo.write();
 * } 
 * </pre>
 * 
 * (Initial draft was sent to aspectj-users@eclipse.org by
 * Ricardo on 5/13/2003 
 * (http://dev.eclipse.org/mhonarc/lists/aspectj-users/msg00482.html)
 * but his email fails now, so we
 * did not get explicit authorization to post.)
 * 
 * @author Ricardo Giacomin, Wes Isberg
 */
public aspect WatchSetters {
    // just to invoke test code below
    public static void main(String[] args) {
        Client.handleTimer(new Timer());
    }

    private static final Class[] GETTER_ARG_TYPES = new Class[]{};
    private static final Object[] GETTER_ARGS = new Object[]{};
    private static final Object NONE = new Object();

    /** maintain dirty flag for any IWatched */
    public interface IWatched {}

    /** true if new value sent to any setter */
    private boolean IWatched.dirty;

    /** false if unable to maintain dirty b/c no privileges, no getter... */
    private boolean IWatched.dirtyValid = true;

    /** clients can use dirty flag */
    public boolean IWatched.isDirty() {
        return dirty;
    }

    /** clients handle case when dirty flag is invalid */
    public boolean IWatched.isDirtyValid() {
        return dirtyValid;
    }
    
    /** Setters are instance methods returning void,
     * prefixed "set..." and taking exactly 1 argument.
     * Does not use args(id), since that requires the
     * argument be non-null.
     */
    public pointcut setters(IWatched watched) : target(watched)
        && execution(void IWatched+.set*(*)); // advice uses args[0]

    /**
     * Skip setter if arg is same as current value;
     * otherwise, set dirty flag after proceeding with setter.
     * Skip this advice if we tried it but failed because
     * there wasn't a corresponding setter, we didn't
     * have the right security permissions, etc.
     */
    void around(IWatched watched) : setters(watched) 
            && if(watched.dirtyValid) {
        // get value by invoking getter method
        Object value = NONE;
        try {
            String getterName = "g" +
                thisJoinPoint.getSignature().getName().substring(1);
            Method method = watched.getClass()
                .getMethod(getterName, GETTER_ARG_TYPES);
            value = method.invoke(watched, GETTER_ARGS);
        } catch (Throwable t) {
            // NoSuchMethodException, SecurityException, 
            // InvocationTargetException...
        }
        if (NONE == value) {
            watched.dirtyValid = false;
            proceed(watched);
            return;
        }

        // compare value with arg being set - pointcut has exactly 1 parm
        Object arg = thisJoinPoint.getArgs()[0];
        if (!(null == arg ? value == null : arg.equals(value))) {
            proceed(watched);
            watched.dirty = true;
        }
    }
}

// ----------- sample clients of WatchSetter
// classes may opt in - can also use aspects to declare.
class Timer implements WatchSetters.IWatched {
    private static int ID;
    public final int id = ++ID;
    private int counter;
    public int getCounter() { 
        return counter;
    }
    public void setCounter(int i) { 
        counter = i;
    }
    public void write() {
        System.out.println("writing " + this);
    }
    public String toString() {
        return "Timer " + id + "==" + counter;  
    }
}

// clients can use dirty flag directly
class Client {
   static void handleTimer(Timer timer) {
       timer.setCounter(0); // should result in no write
       if (!timer.isDirtyValid() || timer.isDirty()) {
           timer.write();
       }
       timer.setCounter(2);
       if (!timer.isDirtyValid() || timer.isDirty()) {
           timer.write();
       }
   }
}
 
// ---- aspects use dirty to implement cache, etc.
// Volatile things are flushed when dirty
abstract aspect Volatile {
    // subaspects declare targets using Volatile.ITag
    protected interface ITag {}
    declare precedence : Volatile+, WatchSetters;
    after(WatchSetters.IWatched watched) returning : 
            WatchSetters.setters(watched) {
        if (!watched.isDirtyValid() || watched.isDirty()) {
            flushCache(watched);
        }
    }
    abstract void flushCache(Object o);
}

// treat Timer as volatile, write when flushing
aspect VolatileTimer extends Volatile {
    declare parents: Timer implements ITag;
    void flushCache(Object o) {
        Timer timer = (Timer) o;
        timer.write();
    }
}

//END-SAMPLE caching-dirty-reflectiveSetters

aspect Testing {

    void signal(String s) {
        org.aspectj.testing.Tester.event(s);
    }
    
    static {
        org.aspectj.testing.Tester.expectEvent("client-write");
        org.aspectj.testing.Tester.expectEvent("volatile-write");
    }

    before() : withincode(void VolatileTimer.flushCache(Object)) 
        && call(void Timer.write()) {
        signal("volatile-write");
    }

    before() : withincode(void Client.handleTimer(Timer)) 
        && call(void Timer.write()) {
        signal("client-write");
    }

    after() returning : execution(void WatchSetters.main(String[])) {
        org.aspectj.testing.Tester.checkAllEvents();
    }
}