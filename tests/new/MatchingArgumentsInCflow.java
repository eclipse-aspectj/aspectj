import org.aspectj.testing.*;
import java.util.*;

/**
 * PR#479
 * Variant of Hunter Kelly's bug report PR#479.
 * Hunter tried to bind two arguments using withincode(..)
 * and call(..), but received an error.  This does it the right
 * way and is working as of 1.0alpha1.
 *
 * @since  2001.08.06
 * @author Jeff Palm
 * @report 479
 */
public class MatchingArgumentsInCflow {
    public static void main(String[] args) {
        new MethodParam().someMethod("arg");
    }
}

class MethodParam
{
    public void someMethod(String arg)
    {
	List list = new LinkedList();
	list.add(new String(arg+":"+arg));
    }
}

aspect MethodParamAspect
{
    /*
     * Match the top of the call and bind argument
     */
    pointcut flow(String s):
        cflow(execution(void someMethod(String)) && args(s));

    /*
     * Bind o to the argument to the list
     */
    pointcut some(Object o):
        call(* List.add(Object)) && args(o);
    /*
     * Make sure these arguments are different
     * and assert the values.
     */
    before (String s, Object o): flow(s) && some(o)  {
        Tester.checkEqual(s, "arg");
        Tester.checkEqual(o, "arg:arg");
        Tester.checkNotEqual(s, o);
    }
}
