
import org.aspectj.testing.Tester;

// PR#218

public class ImportWithinClassBody { 
   
    import java.util.Vector;
    
    public static void test() {
        Tester.check("".equals(""), ""); 
    }
}
