public class VoidField {
    void f; // ERR field is void
    public static void main(String[] args) {
        void var; // ERR var is void
    }
    void m(void x) { } // ERR formal is void
}

