
/**
 * @testcase PR#715 PUREJAVA incrementing objects, arrays
 */
public class ArrayIncCE {
    private static int[] IRA = new int[]{0,1,2};
    private static Object OBJECT = new Object();
    static int[] getIRA() { return IRA; }
    static Object getObject() { return null; }

    public void testObjectIncrementingCE() {
        int i = 0;
        Object object = new Object();
        String[] sra = new String[]{""};
        ++getIRA();       // CE prefix ++ not applied to int[]
        ++getObject();    // CE prefix ++ not applied to Object
        getIRA()++;       // CE postfix ++ not applied to int[]
        getObject()++;    // CE postfix ++ not applied to Object
        getIRA() += 1;    // CE + not applied to int[], int
        getObject() += 1; // CE + not applied to Object, int
        "1" += sra[0];    // CE no literal on lhs
    }
}

