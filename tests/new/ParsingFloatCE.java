
public class ParsingFloatCE {
    /** @testcase PR#642 PUREJAVA invalid floating-point constant */
    public void notrun() {
        float f = 10e-f; // expecting CE here
        double d = 10ee10; // expecting CE here
    }
}
