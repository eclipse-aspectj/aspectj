
import java.io.IOException;

aspect LibraryAspect {
    public void lib.LibraryInterface.run() throws IOException {
        throw new IOException("LibraryAspect-defined run() for " + this);
    }
}