

public class ClientCE {
    public static void main(String[] a) {
        new C().run(); // CE 5 expected: declare IOException
    }
}

class C implements lib.LibraryInterface {}