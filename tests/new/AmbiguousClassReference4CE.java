
public class AmbiguousClassReference4CE {

    /** @testcase PR#701 PUREJAVA CE for ambiguous type reference (two inner types)
     *  see also testcase PR#631 */
    public static void main(String[] args) {
        throw new Error("Expecting compiler error, not compile/run");
    }
    static class Foo {}          // CE: "duplicate type name"
    static interface Foo {}      // CE: "duplicate type name"
}
 
