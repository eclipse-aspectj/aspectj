

/** @testcase PR#722 loop expressions not declarations */
public class DeclarationsInLoopsCE {
    void method() {
        boolean b = true;
        for (;b;) int j = 0; // CE 7 illegal start of expression, not a statement
        while (b) int k = 0; // CE 8 illegal start of expression, not a statement
        do int l = 0;  while (b); // CE 9 illegal start of expression, not a statement
    }
}
