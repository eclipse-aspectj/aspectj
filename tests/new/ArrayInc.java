

import org.aspectj.testing.Tester;
/**
 * @testcase PR#715 PUREJAVA incrementing array
 */
public class ArrayInc {
    private static void testCheck(boolean b, String s) {
        Tester.check(b,s);
        //if (!b) System.err.println("error: " + s);
    }

    private static int[] IRA=new int[]{0,1,2};
    static int[] getIRA() { return IRA; }
    static int[] throwError() { throw new Error(""); }

    public static void main(String[] args) {
        ArrayInc me = new ArrayInc();
        me.testArrayExpressionOrdering();
        me.testArrayExpression();
    }

    public void testArrayExpression() {
        IRA = new int[]{0};
        String[] sra = new String[] {""};
        sra[0] += "1";
        sra[0] += "string concat";
        testCheck(0==getIRA()[0],
                     "0==getIRA()[0]: " + IRA[0]);
        testCheck(0==IRA[0]++, "0==IRA[0]++: " + IRA[0]);
        testCheck(1==IRA[0], "1==IRA[0]: " + IRA[0]);
        testCheck(2==++getIRA()[0],
                  "2==++getIRA()[0]: " + IRA[0]);;
        testCheck(2==IRA[0], "2==IRA[0]: " + IRA[0]);
    }
    
    public void testArrayExpressionOrdering() {
        boolean gotError = false;
        int i = 0;
        try { int k = throwError()[++i]; } 
        catch (Error e) { gotError = true; }
        testCheck(i==0, "i=" + i);
        testCheck(gotError, "no error");

        i = 0;
        gotError = false;
        try { int k = throwError()[i++]; } 
        catch (Error e) { gotError = true; }
        testCheck(i==0, "i++ !=0: " + i);
        testCheck(gotError, "no error");
    }
}

