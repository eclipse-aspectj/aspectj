import org.aspectj.tools.ajdb.Main;
import org.aspectj.debugger.tty.CommandLineDebugger;
import org.aspectj.debugger.base.*;
import com.sun.jdi.*;
import com.sun.jdi.event.*;
import java.io.*;
import java.util.*;
import org.aspectj.testing.Tester;

public class AJDBTest implements StopListener, VMListener {
    
    String classpath;
    {
        classpath = "\"" + Tester.outputDir();
        String javaClasspath = System.getProperty("java.class.path");
        if (javaClasspath != null) {
            classpath += ";" + javaClasspath;
        }
        classpath += "\"";
    }

    protected String[] stringCommands() {
        return new String[] {
            "workingdir " + Tester.workingDir(),
            "use " + "./new",
            "stop on AJDBClass.java:24",
            "run -classpath " + classpath + " AJDBClass",
        };
    }
    protected String getSourceName() {
        return "AJDBClass.java";
    }

    // Methods for VMListener
    protected void death(VMDeathEvent e) {
        System.out.println("*** Death: " + e);
    }
    protected void disconnect(VMDisconnectEvent e) {
        System.out.println("*** Disconnect: " + e);
    }
    protected void start(VMStartEvent e) {
        System.out.println("*** Start: " + e);
    }
    
    // Methods for StopListener
    protected void access(AccessWatchpointEvent e) {
    }
    protected void breakpoint(BreakpointEvent e) {
        checkLines((List) ex("where"), "next");
    }
    protected void exception(ExceptionEvent e) {
    }
    protected void modification(ModificationWatchpointEvent e) {
    }
    protected void step(StepEvent e) {
        List lines = (List) ex("where");
        checkLines(lines);
        try {
            StackFrame frame = (StackFrame) lines.get(0);
            Location loc = frame.location();
            if (loc.sourceName().equals("Thread.java") &&
                loc.method().name().equals("exit")) {
                isRunning = false;
            }
        } catch (Throwable t) {}
        ex("next");

    }

    public void checkLines(Collection lines, Object then) {
        checkLines(lines);
        if (then != null) ex(then);
    }

    public void checkLines(Collection lines) {
        Iterator iter = lines.iterator();
        while (iter.hasNext()) {
            StackFrame frame = (StackFrame) iter.next();
            String source = "no.source";
            try {
                source = debugger.sourceName(frame.location());
            } catch (Throwable t) {}
            int line = debugger.lineNumber(frame.location());
            if (source.equals(getSourceName())) {
                Tester.check(line>0, "non-mapping line for " + frame);
            }
        }
    }

    // VMListener
    public void vmDeathEvent(VMDeathEvent e) {
        death(e);
    }
    public void vmDisconnectEvent(VMDisconnectEvent e) {
        disconnect(e);
    }
    public void vmStartEvent(VMStartEvent e) {
        start(e);
    }

    // StopListener
    public final void accessWatchpointEvent(AccessWatchpointEvent e) {
        access(e);
    }
    public final void breakpointEvent(BreakpointEvent e) {
        breakpoint(e);
    }
    public final void exceptionEvent(ExceptionEvent e) {
        exception(e);
    }
    public final void modificationWatchpointEvent(ModificationWatchpointEvent e) {
        modification(e);
    }
    public final void stepEvent(StepEvent e) {
        step(e);
    }

    AJDebugger debugger;
    CommandLineDebugger ajdb;
    
    public void realMain(String[] args) {
        String fileName = null;
        String[] newArgs = args;
        if (args.length > 0) {
            fileName = args[0];
            newArgs = new String[args.length-1];
            System.arraycopy(args, 1, newArgs, 0, newArgs.length);
        }
        realMain(fileName, newArgs);
    }

    private void realMain(String fileName, String[] args) {
        debugger = (ajdb = new Main().debug(args)).getDebugger();
        debugger.addStopListener(this);
        debugger.addVMListener(this);
        ex(fileName == null ? commands() : commands(fileName));
        while (isRunning) {
            //System.out.println(">>>>>>>> " + debugger.isRunning());
        }
    }
    private boolean isRunning = true;

    public final Collection commands() {
        Collection list = new Vector();
        String[] commands = stringCommands();
        for (int i = 0; i < commands.length; i++) {
            list.add(commands[i]);
        }
        return list;
    }

    Object ex(Object command) {
        return ajdb.executeCommand(command+"");
    }

    void ex(Collection list) {
        Iterator iter = list.iterator();
        while (iter.hasNext()) {
            ex(iter.next());
        }
    }

    final static String COMMENT = "#";
    final static String FILENAME = "script.txt";
    Collection commands(String fileName) {
        Collection list = new Vector();
        try {
            BufferedReader in = new BufferedReader(new FileReader(fileName));
            String line;
            while ((line = in.readLine()) != null) {
                line = line.trim();
                if (line.startsWith(COMMENT)) {
                    continue;
                }
                list.add(line);
            }
            in.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
            System.exit(-1);
        }
        return list;
    }

    public static void main(String[] args) {
        new AJDBTest().realMain(args);
    }
}
