import com.sun.jdi.*;
import com.sun.jdi.connect.*;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequestManager;

import java.util.*;
import java.io.*;

/**
 * @version 	1.0
 * @author
 */
public class JdiTest {
	static Thread errThread, outThread;

	public static void main(String[] xxx) throws Exception {
		LaunchingConnector lc = findLaunchingConnector();
		String mainArgs = "Hello";
		Map args = connectorArguments(lc, mainArgs);
		System.out.println(args);
		VirtualMachine vm = lc.launch(args);
		System.out.println(vm);
		redirectOutput(vm);
		vm.resume();

		while (vm.classesByName("Hello").size() == 0) {}


		ReferenceType rt = (ReferenceType)vm.classesByName("Hello").get(0);
		System.out.println(rt);
		System.out.println(rt.availableStrata());
		try {
			System.out.println(rt.sourceDebugExtension());
			System.out.println("Names: " + rt.sourceNames("AspectJ"));
		} catch (AbsentInformationException aie) {
			System.out.println("NO source debug extension");
		}
		System.out.println("Name: " + rt.sourceName());


		for (Iterator i = rt.allLineLocations().iterator(); i.hasNext(); ) {
			Location loc = (Location)i.next();
			System.out.println("   " + loc.sourceName() + ":" + loc.lineNumber());
		}


		try {
			//eventThread.join();
			errThread.join(); // Make sure output is forwarded
			outThread.join(); // before we exit
		} catch (InterruptedException exc) {
			// we don't interrupt
		}

	}

	/**
	 * Find a com.sun.jdi.CommandLineLaunch connector
	 */
	static LaunchingConnector findLaunchingConnector() {
		List connectors = Bootstrap.virtualMachineManager().allConnectors();
		Iterator iter = connectors.iterator();
		while (iter.hasNext()) {
			Connector connector = (Connector) iter.next();
			if (connector.name().equals("com.sun.jdi.CommandLineLaunch")) {
				return (LaunchingConnector) connector;
			}
		}
		throw new Error("No launching connector");
	}

	/**
	 * Return the launching connector's arguments.
	 */
	static Map connectorArguments(LaunchingConnector connector, String mainArgs) {
		Map arguments = connector.defaultArguments();
		Connector.Argument mainArg = (Connector.Argument) arguments.get("main");
		if (mainArg == null) {
			throw new Error("Bad launching connector");
		}
		mainArg.setValue(mainArgs);

//		if (watchFields) {
//			// We need a VM that supports watchpoints
//			Connector.Argument optionArg = (Connector.Argument) arguments.get("options");
//			if (optionArg == null) {
//				throw new Error("Bad launching connector");
//			}
//			optionArg.setValue("-classic");
//		}
		return arguments;
	}

    static void redirectOutput(VirtualMachine vm) {
        Process process = vm.process();

        // Copy target's output and error to our output and error.
        errThread = new StreamRedirectThread("error reader",
                                             process.getErrorStream(),
                                             System.err);
        outThread = new StreamRedirectThread("output reader",
                                             process.getInputStream(),
                                             System.out);
        errThread.start();
        outThread.start();
    }

}

class StreamRedirectThread extends Thread {

    private final Reader in;
    private final Writer out;

    private static final int BUFFER_SIZE = 2048;

    /**
     * Set up for copy.
     * @param name  Name of the thread
     * @param in    Stream to copy from
     * @param out   Stream to copy to
     */
    StreamRedirectThread(String name, InputStream in, OutputStream out) {
	super(name);
	this.in = new InputStreamReader(in);
	this.out = new OutputStreamWriter(out);
	setPriority(Thread.MAX_PRIORITY-1);
    }

    /**
     * Copy.
     */
    public void run() {
        try {
	    char[] cbuf = new char[BUFFER_SIZE];
	    int count;
	    while ((count = in.read(cbuf, 0, BUFFER_SIZE)) >= 0) {
		out.write(cbuf, 0, count);
	    }
            out.flush();
	} catch(IOException exc) {
	    System.err.println("Child I/O Transfer - " + exc);
	}
    }
}
