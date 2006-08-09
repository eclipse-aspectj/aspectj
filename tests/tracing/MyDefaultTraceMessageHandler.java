import org.aspectj.weaver.loadtime.TraceMessageHandler;
import org.aspectj.weaver.tools.*;

public class MyDefaultTraceMessageHandler extends TraceMessageHandler {
	
	private static Trace trace = new DefaultTrace(MyDefaultTraceMessageHandler.class);
	
	public MyDefaultTraceMessageHandler () {
		super(trace);
		trace.setTraceEnabled(true);
	}
}