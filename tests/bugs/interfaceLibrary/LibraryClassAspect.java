
import java.io.IOException;

aspect LibraryClassAspect {
    public void lib.LibraryClass.run() throws IOException {
        throw new IOException("LibraryClassAspect-defined run() for " + this);
    }
}