//Bugzilla Bug 36046  
//   inter-type declaration bug with abstract classes 

public class Driver {
	public static void main(String args[]) {
		Derived generator = new Derived();
		System.out.println(generator.getExecutions("processEvents"));
	}
	static aspect MonitorBase {
		declare parents: Base implements ExecutionMonitor.MonitoredItem;
	}    
}

class Derived extends Base {
	public String getName() {
		return null;
	}
}

abstract class Base {
	abstract public String getName();
}

aspect ExecutionMonitor {
	/** marker interface to indicate the execution monitor should track calls 
and executions on this class. */
	public interface MonitoredItem {
		int getExecutions(String methodName);
	}

	/** a Map of events to mutable integers */
	public int MonitoredItem.getExecutions(String methodName) {
		return 0;
	}
}