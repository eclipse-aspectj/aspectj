
import org.aspectj.testing.Tester;
import org.aspectj.testing.Tester; 

public class CflowAlone {
    public static void main(String[] args){
        new testclass1(); 
        Tester.check(0 < Filteraspect.i, 
                     "0 < Filteraspect.i: " + Filteraspect.i);
    }
}
class testclass1 {}
class testclass2 {}
aspect Filteraspect {
    static int i;
    // all these variants fail
    //pointcut goCut(): cflow(this(testclass2));
    //pointcut goCut(): cflow(target(testclass2));
    //pointcut goCut(): cflow(args(testclass2));
    //pointcut goCut(): cflow(!within(FilterAspect));
    //pointcut goCut(): cflow(within(FilterAspect));
    //pointcut goCut(): cflow(within(testclass1));
    pointcut goCut(): !within(Filteraspect) && cflow(within(testclass1)) 
    		&& !preinitialization(new(..)) && !initialization(new(..));
    // works ok
    //pointcut goCut(): within(Filteraspect);
    Object around(): goCut() { i++; return proceed(); }
    // no bug when using before or after
    //after(): goCut() { int i = 1; System.getProperties().put("foo", "bar");}
}
