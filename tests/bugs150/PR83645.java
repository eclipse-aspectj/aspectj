
import org.aspectj.lang.NoAspectBoundException;

interface IP {}

/** @testcase PR83645 pertypewithin on interface */
public class PR83645 implements IP {
    public static void main(String[] args) {
        try {
        	boolean hasAspect = PT.hasAspect(IP.class);
        	if (hasAspect) throw new Error("Shouldn't have an instance for an interface");
            boolean yes = (PT.aspectOf(IP.class) instanceof PT);
            throw new Error("expected NoAspectBoundException, got instance?: " + yes);
        } catch (NoAspectBoundException e) {
            // ok
        }
    }
}

aspect PT pertypewithin(IP+) {
    static int INDEX;
    final int index = INDEX++;
    
    public PT() {}
    
    public String toString() {
        return "me " + index;
    }
}