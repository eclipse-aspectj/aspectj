import org.aspectj.weaver.tools.Trace;
import org.aspectj.weaver.tools.TraceFactory;

public class TracingTest {
	
	public void test () {
		TraceFactory factory = TraceFactory.getTraceFactory();
		Trace trace = factory.getTrace(getClass());
		System.out.println("? TracingTest.main() trace=" + trace);
	}
	
	public static void main (String[] args) {
		new TracingTest().test();
	}
	
}