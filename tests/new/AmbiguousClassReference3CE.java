public class AmbiguousClassReference3CE {

    /** @testcase PR#701 PUREJAVA CE for ambiguous type reference (two type declarations)
     *  see also testcase PR#631 */
    public static void main(String[] args) {
        throw new Error("Expecting compiler error, not compile/run");
    }
}
class Foo {}          // expect: "duplicate type name"
interface Foo {}      // expect: "duplicate type name"
class Bar {
    Foo f;
}
 
