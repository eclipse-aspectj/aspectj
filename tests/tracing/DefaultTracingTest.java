import org.aspectj.weaver.tools.*;

public class DefaultTracingTest {
	
	public void test () {
		TraceFactory factory = new DefaultTraceFactory();
		Trace trace = factory.getTrace(getClass());
		System.out.println("? DefaultTracingTest.main() trace=" + trace);
	}
	
	public static void main (String[] args) {
		new DefaultTracingTest().test();
	}
	
}