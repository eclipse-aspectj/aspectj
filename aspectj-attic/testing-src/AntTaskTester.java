/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/


import org.apache.tools.ant.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;

/**
 * Provides utility methods to test ant tasks.
 */
public abstract class AntTaskTester implements BuildListener {

    public abstract String getAntFile();

    protected PrintStream out = System.out;
    protected PrintStream err = System.err;
    protected void info(Object msg) {
        out.println(" [" + msg + "]");
    }

    protected Project project;
    protected String taskname = "unset";

    protected List errors = new Vector();
    protected void throwable(Throwable t) {
        error(taskname, t);
        t.printStackTrace();
    }
    protected void error(String task, Object msg) {
        error("[" + task + "]: " + msg);
    }
    protected void error(Object msg) {
        err.println("Error: " + msg);
        errors.add(msg);
    }

    protected List wants = new Vector();
    protected void want(Object object) {
        wants.add(object);
    }
    protected List haves = new Vector();
    protected void have(Object object) {
        haves.add(object);
    }
    protected List donts = new Vector();
    protected void dont(Object object) {
        donts.add(object);
    }

    protected void clear() {
        wants = new Vector();
        haves = new Vector();
        donts = new Vector();
    }

    protected final boolean verbose = verbose();
    protected boolean verbose() { return false; }
    protected void log(String msg) {
        if (verbose) out.println("[ " + msg + " ]");
    }

    public void printSummary() {
        Iterator iter = errors.iterator();
        out.println();
        out.println("----------------------- Test Summary -----------------------");
        if (errors.size() == 0) {
            out.println("no errors");
        } else {
            out.println(errors.size() + " error" + (errors.size() > 1 ? "s" : ""));
            while (iter.hasNext()) {
                out.println(" " + iter.next());
            }
        }
        out.println("------------------------------------------------------------");
    }

    /**
     * Checks the all the assertions we wanted were achieved
     * and all those received were desired.
     */
    protected void checkAfterTask() {
        log("checking after task");
        for (Iterator i = wants.iterator(); i.hasNext();) {
            Object want = i.next();
            check(haves.contains(want),
                  "didn't see " + want + " in " + haves);
        }
        for (Iterator i = haves.iterator(); i.hasNext();) {
            Object have = i.next();
            check(wants.contains(have),
                  "shouldn't have seen " + have + " in " + wants);
        }
        for (Iterator i = donts.iterator(); i.hasNext();) {
            Object dont = i.next();
            check(!haves.contains(dont),
                  "should't have seen " + dont + " in " + haves);
        }
    }

    /**
     * Logs an error in <code>!b</code> with message <code>msg</code>.
     *
     * @param b   <code>true</code> for an error.
     * @param msg Failure message.
     */
    protected void check(boolean b, String msg) {
        if (!b) error(taskname, msg);
    }

    /**
     * Calls {@link #check(boolean,String)} with the result
     * of comparing equality of <code>o1</code> and <code>o2</code>,
     * failing with message <code>msg</code>.
     *
     * @param o1  First comparison.
     * @param o2  Other comparison.
     * @param msg Failure message.
     */
    protected void check(Object o1, Object o2, String msg) {
        if (o1 != null) {
            check(o1.equals(o2), msg);
        } else if (o2 != null) {
            check(o2.equals(o1), msg);
        } else {
            check(true, msg);
        }
    }

    /**
     * Calls {@link #runProject} with <code>args</code> and
     * the result of {@link #getAntFile}.
     *
     * @param args Arguments given on the command line.
     * @see   #runProject(String[], String)
     */
    public void runTests(String[] args) {
        runProject(args, getAntFile());
    }

    /**
     * Loads the project, collects a list of methods, and
     * passes these methods to {@link #runProject(Method[])}.
     *
     * @param args      Command line arguments.
     * @param buildFile XML file that we are giving to ANT.
     * @see #runProject(Method[])
     */
    public void runProject(String[] args, String buildFile) {
        loadProject(buildFile);
        Method[] methods = null;
        if (args == null || args.length == 0 || args[0].equals("${args}")) {
            methods = getClass().getMethods();
        } else {
            methods = new Method[args.length];
            for (int i = 0; i < args.length; i++) {
                String name = args[i];
                if (!Character.isJavaIdentifierStart(name.charAt(0)) || // todo wes: was (i)?
                    name.charAt(0) == '$') {
                    continue;
                }
                try {
                    methods[i] = getClass().getMethod(name, new Class[]{});
                } catch (NoSuchMethodException nsme) {
                    methods[i] = null;
                }
            }
        }
        runProject(methods);
    }

    /**
     * Execute the targets whose name matches those found in <code>methods</code>.
     *
     * @param methods List of methods to execute.
     */
    protected final void runProject(Method[] methods) {
        if (methods == null || methods.length < 1) {
            return;
        }
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            if (method == null) {
                error("a method is null!");
                continue;
            }
            taskname = method.getName();
            if (taskname.startsWith("test")) {
                info("test task: " + taskname);
                try {
                    method.invoke(this, new Object[]{});
                    beforeTasks();
                    execute(taskname);
                    afterTasks();
                } catch (Throwable t) {
                    throwable(t);
                } finally {
                    info("done task: " + taskname);
                }
            } else if (taskname.startsWith("fail")) {
                info("fail task: " + taskname);
                try {
                    beforeTasks();
                    want(taskname + "-error");
                    execute(taskname);
                    afterTasks();
                } catch (Throwable t) {
                    if (t instanceof BuildException) {
                        have(taskname + "-error");
                        try {
                            method.invoke(this, new Object[]{t});
                        } catch (Throwable tt) {
                            throwable(tt);
                        }
                    } else {
                        throwable(t);
                    }
                } finally {
                    info("done task: " + taskname);
                }
            }
        }
        printSummary();
    }

    /**
     * Called before every task.
     */
    private final void beforeTasks() {
        clear();
        beforeMethod();
        beforeEveryTask();
    }

    /**
     * Called after every task.
     */
    private final void afterTasks() {
        afterMethod();
        afterEveryTask();
        checkAfterTask();
    }    

    /**
     * Invokes the method with prefix <code>prefix</code>.
     *
     * @param prefix Prefix of the method to execute.
     */
    private final void taskMethod(String prefix) {
        String name = prefix + Character.toUpperCase(taskname.charAt(0)) +
            taskname.substring(1);
        try {
            Method method = getClass().getDeclaredMethod(name, new Class[]{});
            if (method != null) {
                method.invoke(this, new Object[]{});
            }
        } catch (Throwable t) {
        }
    }

    /**
     * Executes the method with prefix <code>after</code>.
     */
    private final void afterMethod() {
        taskMethod("after");
    }

    /**
     * Executes the method with prefix <code>before</code>.
     */
    private final void beforeMethod() {
        taskMethod("before");
    }

    /**
     * Override this to do some work before every task.
     */
    protected void beforeEveryTask() {}

    /**
     * Override this for initialization -- called at
     * the end of {@link #loadProject}.
     */
    protected void init() {}

    /**
     * Override this to do some work after every task.
     */
    protected void afterEveryTask() {}

    protected void setProperties(Map map, boolean user) {
        Iterator iter = map.keySet().iterator();
        while (iter.hasNext()) {
            Object key = iter.next();
            Object val = map.get(key);
            String keyString = key + "";
            String valString = val + "";
            if (user) {
                project.setUserProperty(keyString, valString);
            } else {
                project.setProperty(keyString, valString);
            }
        }
    }

    protected void setProperties() {
        setProperties(getProperties(), false);
    }

    protected void setUserProperties() {
        setProperties(getUserProperties(), true);
    }

    /**
     * Override this to provide user properties -- default to
     * an empty <code>HashMap</code>.
     *
     * @return Empty <code>HashMap</code>.
     */
    protected Map getUserProperties() {
        return new HashMap();
    }

    /**
     * Override this to provide system properties -- default to
     * an empty <code>HashMap</code>.
     *
     * @return Empty <code>HashMap</code>.
     */
    protected Map getProperties() {
        return new HashMap();
    }

    /**
     * Loads the project with file name <code>buildFile</code>.
     *
     * @param buildFile Name of the XML file to load.
     */
    public void loadProject(String buildFile) {
        project = new Project();
        project.init();
        project.setUserProperty("ant.file", new File(buildFile).getAbsolutePath() );
        setProperties();
        setUserProperties();
        project.addBuildListener(this);
        ProjectHelper.configureProject(project, new File(buildFile));
        init();
    }

    public void execute(String targetName) {
        try { 
            project.executeTarget(targetName);
        } finally { 
        }
    }

    private static class StringBufferOutputStream extends OutputStream {
        private StringBuffer buf;
        public StringBufferOutputStream(StringBuffer buf) {
            this.buf = buf;
        }
        public void write(int c) { 
            buf.append((char)c);
        }
    }

    public boolean verbosity(BuildEvent event) {
        int[] verbosities = verbosities();
        int priority = event.getPriority();
        for (int i = 0; i < verbosities.length; i++) {
            if (priority == verbosities[i]) return true;
        }
        return false;
    }

    public int[] verbosities() {
        return new int[] { /*Project.MSG_VERBOSE,*/ Project.MSG_INFO, Project.MSG_WARN, project.MSG_ERR };
    }

    // BuildListener
    public void buildFinished(BuildEvent event) {
    }
    public void buildStarted(BuildEvent event) {
    }
    public void messageLogged(BuildEvent event) {
        if (verbosity(event)) {
            out.println(event.getMessage());
        }
    }
    public void targetFinished(BuildEvent event) {
    }
    public void targetStarted(BuildEvent event) {
    }
    public void taskFinished(BuildEvent event) {
    }
    public void taskStarted(BuildEvent event) {
    }
}
