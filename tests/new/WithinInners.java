import org.aspectj.testing.Tester;

public class WithinInners {
    public static void main(String[] args) {
	C c = new C();

	c.getRunnable().run();
		// 1.1 doesn't capture withincode and execution of local types
        //(1.0 behavior)Tester.checkEqual(A.notes, "before-within:before-withincode:around-in:run:around-out:");
        Tester.checkEqual(A.notes, "before-within:around-in:run:around-out:");
    }
}

class C {
    public C() {
	class Inner {
	    public void write( String text ) {
		System.out.println( "write( String )" );
		System.out.println( text );
	    }
	}
	
	Inner i = new Inner();
	String s = "TEXT";
	i.write( s );
    }

    public Runnable getRunnable() {
	return new Runnable() {
		public void run() {
		    A.notes += "run:"; 
		}
	    };
    }
}


aspect A {
    public static String notes = "";

    /* These don't work because we can't give local types reasonable top-level names */
    before(String s): call(void write(String)) && args(s) { //&& withincode(C.new()) {
	System.out.println(s);
    }

    void around(String s): call(void write(String)) && args(s) && withincode(C.new()) {
	proceed(s.toLowerCase());
    }

    /* These now work and are checked */
    //XXX not being able to do this(c) is a pain
    before(Runnable runnable): execution(void Runnable.run()) && target(runnable) && within(C) { // && this(c) {
	//System.out.println("about to call Runnable.run in " + c + " on " + runnable);
        notes += "before-within:";
    }
    before(): execution(void run()) && withincode(Runnable C.getRunnable()) {
	//System.out.println("about to call Runnable.run in C");
        notes += "before-withincode:";
    }
    void around(): execution(void run()) {
	//System.out.println("about to call Runnable.run in C");
        notes += "around-in:";
	proceed();
        notes += "around-out:";
    }
}
