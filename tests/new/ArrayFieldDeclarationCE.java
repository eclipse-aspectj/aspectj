

/** @testcase PR#829 CE expected when declaring fields on arrays */
public class ArrayFieldDeclarationCE {

    public static void main(String[] args) {
        throw new Error("should not run");
    }
}

class C { }

aspect A {
    public int C[].foo; // CE 14 cannot declare fields on arrays
}
