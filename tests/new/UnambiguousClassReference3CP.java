import java.sql.Array;

import org.aspectj.testing.Tester;

public class UnambiguousClassReference3CP {

    /** @testcase PR#701 PUREJAVA no CE for unambiguous reference 
     *  see also testcase PR#631 */
    public static void main(String[] args) {
        int[] gh = new int[5];
        int len = java.lang.reflect.Array.getLength(gh); // not ambiguous
        Tester.check(5==len, "Array.getLength failed: " + len);
    }
}
 
