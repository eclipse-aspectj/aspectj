import org.aspectj.testing.Tester;

/** @testcase Bugzilla Bug 29691  
   Static inner aspects cannot reference user defined pointcuts 
 
 */
public class PcdLookup {

    public static void main(String[] args) {
    }
    
    public static aspect Referencer {
        pointcut mainCall() : call(void main(..));
        pointcut myMainCall() : mainCall() && outer(); 
    }   
    
    pointcut outer(): within(PcdLookup);
}

