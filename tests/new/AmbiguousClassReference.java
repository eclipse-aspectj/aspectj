import java.sql.*;
import java.lang.reflect.*;

/** support mail from Dave Trombley */
public class AmbiguousClassReference {

    /** @testcase PR#631 PUREJAVA expecting CE for ambiguous reference */
    public static void main(String[] args) {
        int[] gh;
        gh = new int[5];
        Array.getLength(gh);
        throw new Error("Expecting compiler error, not compile/run");
    }
}
 
