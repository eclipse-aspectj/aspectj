

public class ClassClientCE {
    public static void main(String[] a) {
        new C().run(); // CE 5 expected: declare IOException
    }
}

class C extends lib.LibraryClass {}