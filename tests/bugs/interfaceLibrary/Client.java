
import java.io.IOException;

public class Client {
    public static void main(String[] a) {
        try {
            new C().run();
            throw new Error("test failed to throw IOException");
        } catch (IOException e) {
        }
    }
}

class C implements lib.LibraryInterface {}