
import java.io.IOException;

public class ClassClient {
    public static void main(String[] a) {
        try {
            new C().run();
            throw new Error("test failed to throw IOException");
        } catch (IOException e) {
        }
    }
}

class C extends lib.LibraryClass {}