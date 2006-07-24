/* *******************************************************************
 * Copyright (c) 2003 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Wes Isberg     initial implementation 
 * ******************************************************************/

package org.aspectj.testing.ajde;

import java.awt.Frame;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;

import org.aspectj.testing.harness.bridge.Globals;
import org.aspectj.ajde.*;
import org.aspectj.ajde.ui.*;
import org.aspectj.ajde.ui.internal.*;
import org.aspectj.ajde.ui.swing.*;
import org.aspectj.asm.*;
import org.aspectj.bridge.*;
import org.aspectj.util.FileUtil;

/**
 * This re-uses the same config file to setup ajde
 * so that recompiles appear to be of the same configuration.
 * @since Java 1.3 (uses dynamic proxies)
 */
public class CompileCommand implements ICommand {
    // time out waiting for build at three minutes
    long MAX_TIME = 180 * 1000;
    // this proxy ignores calls
    InvocationHandler proxy = new VoidInvocationHandler();
    InvocationHandler loggingProxy = new LoggingInvocationHandler();
    MyTaskListManager myHandler = new MyTaskListManager();
    long endTime;
    boolean buildNextFresh;
    File tempDir;

    /**
     * Clients call this before repeatCommand as a one-shot
     * request for a full rebuild of the same configuration.  
     * (Requires a downcast from ICommand to CompileCommand.)
     */
    public void buildNextFresh() {
        buildNextFresh = true;
    }

    // --------- ICommand interface
    public boolean runCommand(String[] args, IMessageHandler handler) {
        setup(args);
        myHandler.start();
        long startTime = System.currentTimeMillis();
        try {
            Ajde.getDefault().getBuildManager().buildFresh();
            // System.err.println("compiling " + Arrays.asList(args));
            waitForCompletion(startTime);
        } finally {
            myHandler.finish(handler);
            runCommandCleanup();
        }
        return !myHandler.hasError();
    }

    public boolean repeatCommand(IMessageHandler handler) {
        myHandler.start();
        long startTime = System.currentTimeMillis();
        // System.err.println("recompiling...");
        if (buildNextFresh) {
            buildNextFresh = false;
            Ajde.getDefault().getBuildManager().buildFresh();
        } else {
            Ajde.getDefault().getBuildManager().build();
        }
        waitForCompletion(startTime);
        myHandler.finish(handler);
        return !myHandler.hasError();
    }
    void runCommandCleanup() {
        if (null != tempDir) {
            FileUtil.deleteContents(tempDir);
            tempDir.delete();
        }
    }

    // set by build progress monitor when done
    void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    private void waitForCompletion(long startTime) {
        long maxTime = startTime + MAX_TIME;
        while ((startTime > endTime)
            && (maxTime > System.currentTimeMillis())) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
            }
        }
    }

    private void setup(String[] args) {
        File config = writeConfig(args);
        if (null == config) {
            throw new Error("unable to write config file");
        }
        EditorAdapter editorAdapter =
            (EditorAdapter) makeProxy(EditorAdapter.class);
        TaskListManager taskListManager = myHandler;
        BuildProgressMonitor buildProgressMonitor =
            new DefaultBuildProgressMonitor(new Frame()) {
            public void finish(boolean b) {
                super.finish(b);
                setEndTime(System.currentTimeMillis());
            }
        };
        String classesDir = "../testing/bin/classes";
        for (int i = 0; i < args.length; i++) {
            if ("-d".equals(args[i]) && ((1 +i) < args.length)) {
                classesDir = args[1 + i];
                break;
            }
        }

        ProjectPropertiesAdapter projectPropertiesAdapter =
            new ProjectProperties(classesDir);
            // neither of these are in the true classpath
        //            new NullIdeProperties("");  // in testsrc
        //            = new BrowserProperties();  // in ajbrowser
        BuildOptionsAdapter buildOptionsAdapter =
            new AjcBuildOptions(new UserPreferencesStore(false));
        IdeUIAdapter ideUIAdapter =
            (IdeUIAdapter) makeProxy(IdeUIAdapter.class);
        ErrorHandler errorHandler =
            (ErrorHandler) makeProxy(ErrorHandler.class);

        AbstractIconRegistry iconRegistry = new AbstractIconRegistry() {
            protected AbstractIcon createIcon(String path) {
                return new AbstractIcon(new Object());
            }
        };
        StructureViewNodeFactory structureViewNodeFactory =
            new StructureViewNodeFactory(iconRegistry) {
	            protected IStructureViewNode createDeclaration(
	                IProgramElement node,
	                AbstractIcon icon,
	                List children) {
	                return new SwingTreeViewNode(node, icon, children);
	            }
				protected IStructureViewNode createRelationship(
					IRelationship node,
					AbstractIcon icon) {
					return new SwingTreeViewNode(node, icon);
				}	            
				protected IStructureViewNode createLink(
					IProgramElement node,
					AbstractIcon icon) {
					return new SwingTreeViewNode(node, icon);
				}	 
        };

        Ajde.init(
            editorAdapter,
            taskListManager,
            buildProgressMonitor,
            projectPropertiesAdapter,
            buildOptionsAdapter,
            structureViewNodeFactory,
            ideUIAdapter,
            errorHandler);

        Ajde.getDefault().getConfigurationManager().setActiveConfigFile(
            config.getAbsolutePath());
    }

    private File writeConfig(String[] args) {
        tempDir = FileUtil.getTempDir("CompileCommand");
        File result = new File(tempDir, "config.lst");
        OutputStream out = null;
        try {
            out = new FileOutputStream(result);
            PrintStream outs = new PrintStream(out, true);
            for (int i = 0; i < args.length; i++) {
                outs.println(args[i]);
            }
            return result;
        } catch (IOException e) {
            return null;
        } finally {
            try {
                out.close();
            } catch (IOException e) {
            }
        }
    }

//    private Object makeLoggingProxy(Class interfac) {
//        return Proxy.newProxyInstance(
//            interfac.getClassLoader(),
//            new Class[] { interfac },
//            loggingProxy);
//    }
    
    private Object makeProxy(Class interfac) {
        return Proxy.newProxyInstance(
            interfac.getClassLoader(),
            new Class[] { interfac },
            proxy);
    }
}

class MyTaskListManager
    extends MessageHandler
    implements TaskListManager {
    boolean hasError;
    boolean hasWarning;
    MyTaskListManager() {
        super(true);
    }
    public void addProjectTask(String message, IMessage.Kind kind) {
        maintainHasWarning(kind);
    }

    public void addSourcelineTask(IMessage message) {
        maintainHasWarning(message.getKind());
        handleMessage(message);
    }

    public void addSourcelineTask(
        String message,
        ISourceLocation sourceLocation,
        IMessage.Kind kind) {
        addSourcelineTask(
            new Message(message, kind, null, sourceLocation));
    }

    public void clearTasks() {
        if (hasWarning) {
            hasWarning = false;
        }
        if (hasError) {
            hasError = false;
        }
        init(true);
    }

    public boolean hasWarning() {
        return hasWarning;
    }

    boolean hasError() {
        return hasError;
    }

    void start() {
        clearTasks();
    }
    void finish(IMessageHandler copyTo) {
        if (copyTo == this) {
            return;
        }
        IMessage[] messages = getMessages(null, true);
        for (int i = 0; i < messages.length; i++) {
            copyTo.handleMessage(messages[i]);
        }
    }
    private void maintainHasWarning(IMessage.Kind kind) {
        if (!hasError) {
            if (IMessage.ERROR.isSameOrLessThan(kind)) {
                hasError = true;
                hasWarning = true;
            }
        }
        if (!hasWarning && IMessage.WARNING.isSameOrLessThan(kind)) {
            hasWarning = true;
        }
    }
	public void buildSuccessful(boolean wasFullBuild) {
		// TODO Auto-generated method stub
		
	}
}

class VoidInvocationHandler implements InvocationHandler {
    public Object invoke(Object me, Method method, Object[] args)
        throws Throwable {
        //       System.err.println("Proxying"
        //       // don't call toString on self b/c proxied
        //        // + " me=" + me.getClass().getName() 
        //        + " method=" + method
        //        + " args=" + (LangUtil.isEmpty(args) 
        //            ? "[]" : Arrays.asList(args).toString()));
        return null;
    }
}

class LoggingInvocationHandler implements InvocationHandler {
    public Object invoke(Object me, Method method, Object[] args)
        throws Throwable {
        System.err.println("Proxying " + render(method, args));
        return null;
    }
    public static String render(Class c) {
        if (null == c) {
            return "(Class) null";
        }
        String result = c.getName();
        if (result.startsWith("java")) {
            int loc = result.lastIndexOf(".");
            if (-1 != loc) {
                result = result.substring(loc+1);
            }
        }
        return result;
    }

    public static String render(Method method, Object[] args) {
        StringBuffer sb = new StringBuffer();
        sb.append(render(method.getReturnType()));
        sb.append(" ");
        sb.append(method.getName());
        sb.append("(");
        Class[] parmTypes = method.getParameterTypes();
        int parmTypesLength = (null == parmTypes ? 0 : parmTypes.length);
        int argsLength = (null == args ? 0 : args.length);
        boolean doType = (parmTypesLength == argsLength);
        for (int i = 0; i < argsLength; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            if (doType) {
                sb.append("(");
                sb.append(render(parmTypes[i]));
                sb.append(") ");
            }
            if (null == args[i]) {
                sb.append("null");                
            } else { // also don't recurse into proxied toString?
                sb.append(args[i].toString());
            }
        }
        sb.append(")");
        return sb.toString();
    }
}


class ProjectProperties implements ProjectPropertiesAdapter {
    final private static String PREFIX 
        = ProjectProperties.class.getName() + ": ";
    final private String outputDir;
    private Set inJars;
    private Set inpath;
    private Set sourceRoots;
    private Set aspectPath;
    private String outJar;

    public ProjectProperties(String outputDir) {
        this.outputDir = outputDir;
    }

    // known used, per logging proxy
    public String getDefaultBuildConfigFile() { return null; }
    public void setInJars(Set input) { inJars = input; }
    public void setInpath(Set input) { inpath = input; }
    public Set getInJars( ) { return inJars; }
    public Set getInpath() { return inpath; }
    public void setSourceRoots(Set input) { sourceRoots = input; }
    public Set getSourceRoots() { return sourceRoots; }
    public void setAspectPath(Set path) { aspectPath = path; }        
    public Set getAspectPath() { return aspectPath; }
    public String getClasspath() { return Globals.S_aspectjrt_jar;  }
    public String getBootClasspath() { return null; }
    public void setOutJar(String input){ outJar = input; }
    public String getOutJar() { return outJar; }
    public String getOutputPath() { return outputDir; }
    
    public OutputLocationManager getOutputLocationManager() {
    	return null;
    }

    // not known if used - log any calls to it
    public List getBuildConfigFiles() { return logs("buildConfigFiles"); }
    public String getLastActiveBuildConfigFile() { return log("lastActiveBuildConfigFile"); }
    public String getProjectName() { return log("projectName"); } 
    public String getRootProjectDir() { return log("rootProjectDir"); }
    public List getProjectSourceFiles() { return logs("projectSourceFiles"); }
    public String getProjectSourcePath() { return log("projectSourcePath"); }
    public String getAjcWorkingDir() { return log("ajcWorkingDir"); }
    public String getClassToExecute() { return log("classToExecute"); }
    public String getExecutionArgs() { return log("executionArgs"); }
    public String getVmArgs() { return log("vmArgs"); }
    private String log(String s) {
        System.out.println(PREFIX + s);
        return null;
    }
    private List logs(String s) {
        log(s);
        return null;
    }

	public Map getSourcePathResources() {
		return null;
	}

}
