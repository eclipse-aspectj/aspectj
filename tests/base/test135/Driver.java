// proper values for thisJoinPoint attributes 
// Currently there is a bug (?) in that the parameters value
//  of the joinpoint seems to always be null.
package test135;

import org.aspectj.testing.Tester;

public class Driver {
    public static void main(String[] args) { test(); }

    public static void test() {

        TopFoo foo = new TopFoo();
        JoinPointFields jpf = JoinPointFields.aspectOf();

        foo.bar(1, "one");

        Tester.checkEqual(jpf.className, "test135.TopFoo", "className");
        Tester.checkEqual(jpf.methodName, "bar", "methodName");
        Tester.checkEqual(jpf.parameterNames,
                           new String[] {"intParam", "stringParam"}, 
                           "parameterNames");
        Tester.checkEqual(jpf.parameterTypes,
                           new Class[] {Integer.TYPE, String.class},
                           "parameterTypes");
        //System.out.println(jpf.parameters);
        Tester.checkEqual(jpf.parameters,
                           new Object[] {new Integer(1), "one"}, 
                           "parameters"); //!!!

        test135.pack.PackFoo foo1 = new test135.pack.PackFoo();
        test135.pack.PackJoinPointFields jpf1 = 
            test135.pack.PackJoinPointFields.aspectOf();

        foo1.bar(2, "two");
        Tester.checkEqual(jpf1.className, "test135.pack.PackFoo", "className");
        Tester.checkEqual(jpf1.methodName, "bar", "methodName");
        Tester.checkEqual(jpf1.parameterNames,
                           new String[] {"packIntParam", "packStringParam"}, 
                           "parameterNames");
        Tester.checkEqual(jpf1.parameterTypes,
                           new Class[] {Integer.TYPE, String.class}, 
                           "parameterTypes");
        Tester.checkEqual(jpf1.parameters,
                           new Object[] {new Integer(2), "two"},
                           "parameters");
    }
}


