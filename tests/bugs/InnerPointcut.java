// for Bug#:  32428 
import org.aspectj.testing.Tester;


public class InnerPointcut {
	public static void main(String[] args) {
		Tester.checkEqual(TrackTestCase.note, "ran");
	}
	
	pointcut testcutOuter(): within(InnerPointcut);
	
    static aspect TrackTestCase {
    	static String note = "not run yet";
        pointcut testcut() : execution(public void mai*(..));
        before() : testcut() && testcutOuter() {
        	note = "ran";
        }        
    }

}
