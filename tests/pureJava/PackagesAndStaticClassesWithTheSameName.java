/*
 * This test case reveals a limitation in the ajc unparser using fully qualified
 * names for types.  I don't believe that there is in fact a general solution to
 * this problem, so for now we move this to opentests
 */

import org.aspectj.testing.*;

public class PackagesAndStaticClassesWithTheSameName {
    public static void main(String[] args) {
        String string = "string";
        java.lang.Str str = new java.lang.Str(string);
        Tester.checkEqual(string + ":" + string, str+"");
    }

    static class java {
        static class lang {
            static class Str {
                private String str;
                Str(String str) {
                    this.str = str;
                }
                public String toString() { return str + ":" + str; }
            }
        }
    }
}
