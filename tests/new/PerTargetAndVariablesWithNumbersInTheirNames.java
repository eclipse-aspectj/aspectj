import org.aspectj.testing.*;

/**
 * PR#490
 * Came from a bug from Svan Macke:
 *
 * Here is another problem that occured when I changed
 * from aspectj0.8b3 to aspectj0.8b4. It seems that
 * (under a very special condition) aspectJ has problems
 * with the numbers that are appended to variable names
 * inside the generated advice code.
 * 
 * Here is the "special condition" where the error
 * occured. I know the discussion about 'of eachobject'
 * and I also know that in the following code it is
 * absolutely unnecessary to use 'of eachobject' (don't
 * ask me why I wrote such terrible code, I do not know
 * it myself), but however, I think it is correct aspectj
 * code and should therefore compile correctly.
 *
 * @since  2000.08.06
 * @author Jeff Palm
 * @report 408
 */
public class PerTargetAndVariablesWithNumbersInTheirNames {
    public static void main(String[] args) {
        new C();
        Tester.checkAllEvents();
    }
    static {
        Tester.expectEvent("Hello 1");
        Tester.expectEvent("World 2");
        Tester.expectEvent("Hello World around 1");
        Tester.expectEvent("Hello World around 2");
        Tester.expectEvent("Hello World around 3");
        Tester.expectEvent("Hello World around 4");
    }
}

class C
{
    public C()
    {
	doSomething("Hello", "World");
    }
    public void doSomething(String arg1, String arg2)
    {
        Tester.event(arg1 + " 1");
        Tester.event(arg2 + " 2");
    }
}

/*
 * A pertarget aspect.
 */
aspect A1 pertarget(target(C)) {
    void around(String arg1, String arg2):
        target(C) &&
        call(public void doSomething(String,String)) &&
        args(arg1, arg2) {
            Tester.event(arg1 + " " + arg2 + " around 1");
            proceed(arg1, arg2);
        }
}

/*
 * Another pertarget aspect.
 */
aspect A2 pertarget(target(C)) {
    void around(String arg1, String arg2):
        target(C) &&
        call(public void doSomething(String,String)) &&
        args(arg1, arg2) {
            Tester.event(arg1 + " " + arg2 + " around 2");
            proceed(arg1, arg2);
        }
}

/*
 * A 'static' aspect.
 */
aspect A3 {
    void around(String arg1, String arg2):
        target(C) &&
        call(public void doSomething(String,String)) &&
        args(arg1, arg2) {
            Tester.event(arg1 + " " + arg2 + " around 3");
            proceed(arg1, arg2);
        }
}

/*
 * Another 'static' aspect.
 */
aspect A4 {
    void around(String arg1, String arg2):
        target(C) &&
        call(public void doSomething(String,String)) &&
        args(arg1, arg2) {
            Tester.event(arg1 + " " + arg2 + " around 4");
            proceed(arg1, arg2);
        }
}
