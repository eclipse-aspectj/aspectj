//package debugger;

import com.sun.jdi.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;
import java.io.*;
import java.util.*;
import org.aspectj.tools.debugger.*;

/**
 * Tester.java
 *
 *
 * Created: Wed Sep 06 15:53:29 2000
 *
 * @author <a href="mailto:palm@parc.xerox.com"Jeffrey Palm</a>
 */

public abstract class Tester extends DebuggerAdapter implements DebuggerListener {

    public abstract boolean test();
    public abstract String getClassName();

    public       static String  ROOT         = "."; //"C:/aspectj/tests/debugger";
    public final static String  PCKG         = ""; //"debugger.";
    public final static String  PATH         = ""; //"debugger/";
    public              String  FILE         = getClassName() + ".java";
    public              String  CLASS        = PCKG + getClassName();
    public       static String  classPath    = "..";
    public              int     time         = 0;    
    public       static boolean verboseSuccess = false; //true;    
    protected AJDebugger d;
    protected PrintStream out = System.out;
    protected PrintStream err = System.err;
    protected boolean mutex;
    protected boolean good = true;
    protected Vector failures = new Vector();

    protected static boolean debug = false; //true;
    public static void setDebug(boolean _debug) {
        debug = _debug;
    }

    protected final static String  errFile = "err.txt";

    public Tester(boolean debug) {
        this.debug = debug;
        this.classPath = classPath;
        if (debug()) {
            outln("Testing..." + getClassName());
        }
        setErr();
    }
    
    public Tester() {
        this(false);
    }

    public void go(String[] args){
        good &= test();
        sd();
        if (!good) {
            outln("The test failed with the following:\n" + d.iter(failures));
        }
    }

    public boolean debug() {
        return debug | false;
    }

    public static void setClassPath(String _classPath) {
        classPath = _classPath;
    }

    public static void setRoot(String root) {
        ROOT = root;
    }

    public static void setVerbose(boolean _verboseSuccess) {
        verboseSuccess = _verboseSuccess;
    }

    /****************************** Tests ******************************/

    protected HashMap breaks = new HashMap();

    static class IntVector extends Vector {
        public void add(int i) {
            super.add(new Integer(i));
        }
    }

    protected void quit() throws DebuggerException {
        db("Quitting tester..." + getClassName());
        d.quitCommand();
        //d.exit(false);
        d = null;
        db("Quit.");
    }

    protected Value print(Object obj) throws DebuggerException {
        return d.printCommand(obj);
    }

    protected void stopin(String method) throws DebuggerException {
        stopin(getClassName(), method);
    }

    protected void stopin(String className, String method) throws DebuggerException {
        d.stopInCommand(PCKG + className, method);        
    }

    protected void stopat(int line) throws DebuggerException {
        stopat(CLASS, line);
    }

    protected void stopat(String className, int line) throws DebuggerException {
        d.stopAtCommand(PCKG + className, line);
    }

    protected void stopon(int line) throws DebuggerException {
        d.stopOnCommand(PATH + FILE, line);
    }

    protected void clear(int line) throws DebuggerException {
        d.clearOnCommand(PATH + FILE, line);
    }

    protected void clear(String className, int line) throws DebuggerException {
        d.clearAtCommand(PCKG + className, line);
    }

    protected void clear(String method) throws DebuggerException {
        clear(CLASS, method);
    }

    protected void clear(String className, String method) throws DebuggerException {
        d.clearInCommand(PCKG + className, method);        
    }    

    protected void step() throws DebuggerException {
        d.stepCommand();
    }

    protected void stepi() throws DebuggerException {
        d.stepiCommand();
    }

    protected void stepup() throws DebuggerException {
        d.stepUpCommand();
    }

    protected void next() throws DebuggerException {
        d.nextCommand();
    }

    protected void de(Throwable de) {
        de.printStackTrace();
        good = false;
    }
   
    static class Case {
        String msg;
        int line;
        int frames;
        List locals;
        List names;
        List sizes;
        int time;
        public Case(String msg, int line, int frames, List locals, List names, List sizes, int time) {
            this.msg = msg;
            this.line = line;
            this.frames = frames;
            this.locals = locals;
            this.names = names;
            this.sizes = sizes;
            this.time  = time;
        }
        public String toString() {
            return
                "msg=" + msg +
                " line=" + line +
                " frames=" + frames +
                " locals=" + locals +
                " names=" + names +
                " sizes=" + sizes +
                " time=" + time;
        }
    }

    protected void stop(final Vector cases) {
        d.addStopListener(new StopAdapter() {
                public void breakpointEvent(BreakpointEvent e) {
                    try {                            
                        if (cases.size() > time) {
                            Case caze = (Case) cases.get(time);
                            //System.out.println(caze);
                            //System.out.println(d.format(e));
                            String msg = caze.msg;
                            int line = caze.line;
                            int frames = caze.frames;
                            List locals = caze.locals;
                            List names = caze.names;
                            List sizes = caze.sizes;
                            int caseTime = caze.time;
                            check(time == caseTime, "Out of sync " + time + ":" + caseTime);
                            int lineNumber = d.lineNumber(e.location());
                            String methodName = d.methodName(e);
                            if (lineNumber > 0) {
                                check(lineNumber == line, "Lines don't match " +
                                       lineNumber + ":" + line);
                            } else {
                                check(msg.endsWith(methodName), 
                                       "Method '" + msg + "' does not match '" + methodName + "'.");
                            }
                            msg(msg + ": " + d.format(e));                                
                            threads(names, sizes);
                            where("", frames);
                            locals(locals);
                            cont();
                        }
                    } catch (/*Debugger*/Exception de) {
                        de.printStackTrace(out);
                        good = false;
                    }
                    time++;
                }});        
    }

    protected boolean locals(List locals) throws DebuggerException {
        List vars = d.localsCommand();
        boolean allGood = true;
        for (int i = 0; i < locals.size(); i++) {
            boolean there = false;
            if (vars != null) {
                for (int j = 0; j < vars.size(); j++) {
                    LocalVariable lv = (LocalVariable) vars.get(j);
                    if (lv.name().equals(locals.get(i))) {
                        there = true;
                    }
                }
            }
            allGood &= check(there, "The local variable '" + locals.get(i) +
                              "' was not found in\n" + d.locals(vars));
        }
        return allGood;
    }

    protected void threads(List names, List sizes) throws DebuggerException {
        for (int i = 0; i < names.size(); i++) {
            List threads = d.threadsCommand(names.get(i) + "");
            check(threads.size() == ((Integer) sizes.get(i)).intValue(),
                   "need " + sizes.get(i) + " thread(s) in '" + names.get(i) + "':\n" + d.threads(threads));
        }
    }
    
    protected void where(String name, int frames) throws DebuggerException {
        try {
            List stack = d.whereCommand(name);            
            check(stack.size() == frames,
                   "need " + frames + " frame(s) in '" + name + "':\n" + d.frames(stack));
        } catch (WhereRequest.BadThreadStateException e) {
            //TODO
        }
    }

    /****************************** DebuggerListener ******************************/

    public void requestSetEvent(RequestEvent re) {
        msg("Set " + re.getRequest());
    }
    public void requestClearEvent(RequestEvent re) {
        msg("Cleared " + re.getRequest());
    }    
    public void requestDeferredEvent(RequestEvent re) {

    }
    public void requestFailedEvent(RequestEvent re) {
        msg("Unable to set " + re.getRequest() + " : " + re.getErrorMessage());
    }    


    /****************************** Misc. ******************************/

    protected void setErr() {
        try {
            err = new PrintStream(new BufferedOutputStream(new FileOutputStream(errFile)), true) {
                    public void write(int b) {
                        super.write(b);
                    }
                };            
        } catch (IOException ioe) {
        }
        System.setErr(err);        
    }

    protected void setOut() {
        PrintStream redirect = new PrintStream(new OutputStream() {
                public void write(int b) {}
            });        
        System.setOut(redirect);
    }

    protected void down() {
        mutex = true;
    }

    protected void up() {
        mutex = false;
    }

    protected void stall() {
        stall(getMaxStallTime());
    }

    protected long getMaxStallTime() {
        return (long) 20000;
    }

    protected void stall(long time) {
        long start = System.currentTimeMillis();
        while (mutex) {
            if ((System.currentTimeMillis() - start) > time) {
                errln("Stalled for too long");
                break;
            }
        }
    }

    protected void cont() {
        try {
            d.contCommand();
        } catch (DebuggerException de) {
        }
    }

    protected void sd() {
        if (d != null) {
            d.shutDown();
        }
        d = null;
    }

    protected void db(Object o) {
        if (debug()) {
            System.out.println(o);
        }
    }
    
    protected void db() {
        sd();
        d = new AJDebugger(this, false);
        d.addDebuggerListener(this);
        ex("use " + ROOT);
    }

    protected void stop() {
        stop(5000);
    }

    protected void stop(long time) {
        long start = System.currentTimeMillis();
        while (!d.isAtBreakpoint()) {
            if ((System.currentTimeMillis() - start) > time) {
                errln("Stopped for too long");
                break;
            }
        }
    }
    
    protected Object ex(String command) {
        return d.execute(command);
    }
    
    public void outln(Object o) {
        if ((o+"").startsWith("Initializing ajdb...")) {
            return;
        }
        out(o);
        out("\n");
    }
    
    protected void out(Object o) {
        out.print(o);
        out.flush();
    }

    protected void err(Object o) {
        err.print(o);
        err.flush();
    }

    protected void errln(Object o) {
        err(o);
        err("\n");
    }

    protected boolean check(boolean b, String msg) {
        if (!b) {
            outln("<<FAIL>> " + msg);
            good = false;
            failures.add(msg);
        } else if (verboseSuccess) {
            outln("<<SUCESS>> " + msg);
        }
        return b;
    }

    protected boolean  check(Object o, String msg) {
        return check(o != null, msg);
    }
    
    protected void msg(Object o) {
        if (debug()) {
            outln(o);
        } else {
            err.println(o);
        }
    }

    private String runArgs = "";
    public String getRunArgs() {
        return runArgs;
    }
    public void setRunArgs(String runArgs) {
        this.runArgs = runArgs;
    }

    private final String _getArgs() {
        String args = getRunArgs();
        if (args != null && !args.equals("") && !args.startsWith(" ")) {
            args = " " + args;
        }
        return args;
    }

    protected void startTest() {
        String cmd = "run " + classPath() + " " + CLASS + _getArgs();
        startTest(cmd);
    }

    protected static String classPath() {
        if (classPath == null || classPath.equals("")) {
            return "";
        }
        return "-classpath \"" + classPath + "\"";
    }

    protected void startTest(String cmd) {
        d.addVMListener(new VMAdapter() {
                public void vmDisconnectEvent(VMDisconnectEvent e) {
                    msg("Done");
                    up();
                }});
        ex(cmd);
        down();
        stall();
    }
}
