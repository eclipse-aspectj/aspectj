// from Bug 28921 
import org.aspectj.lang.*;

public class DeclareSoftCf {

    public static void a(){
        b();
    }
    /**
     * Method b.
     */
    private static void b() {
        throw new RuntimeException("Orig");
    }
    
    public static void main(String[] args) {
        try {
            a();
        } catch (SoftException e) {
            System.out.println(e.getWrappedThrowable());
        }
    }
    
    public static interface Checked{
    }
    
    static aspect Softner{
        declare parents : Exception+ && !RuntimeException implements Checked;
        declare soft : Checked : within(DeclareSoftCf);  // ERR: Checked not a Throwable
    }
}