/* *******************************************************************
 * Copyright (c) 2003 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Wes Isberg     initial implementation 
 * ******************************************************************/

package org.aspectj.testing.ajde;

import java.awt.Frame;
import java.io.*;
import java.lang.reflect.*;
import java.util.List;

import org.aspectj.ajde.*;
import org.aspectj.ajde.ui.*;
import org.aspectj.ajde.ui.internal.*;
import org.aspectj.ajde.ui.swing.*;
import org.aspectj.asm.StructureNode;
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
    MyTaskListManager myHandler = new MyTaskListManager();
    long endTime;
    boolean buildNextFresh;

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
        Ajde.getDefault().getBuildManager().buildFresh();
        // System.err.println("compiling " + Arrays.asList(args));
        waitForCompletion(startTime);
        myHandler.finish(handler);
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
    
    // set by build progress monitor when done
    void setEndTime(long endTime) {
        this.endTime = endTime;
    }
    
    private void waitForCompletion(long startTime) {
        long maxTime = startTime + MAX_TIME;
        while ((startTime > endTime) 
                && (maxTime > System.currentTimeMillis())){
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
        EditorAdapter editorAdapter 
            = (EditorAdapter) makeProxy(EditorAdapter.class);
        TaskListManager taskListManager = myHandler;
        BuildProgressMonitor buildProgressMonitor 
            = new DefaultBuildProgressMonitor(new Frame()){
                public void finish() {
                    super.finish();
                    setEndTime(System.currentTimeMillis());
                }                
            };
        ProjectPropertiesAdapter projectPropertiesAdapter 
            = new NullIdeProperties("");
        BuildOptionsAdapter buildOptionsAdapter 
            = new AjcBuildOptions(new UserPreferencesStore(false));
        IdeUIAdapter ideUIAdapter 
            = (IdeUIAdapter) makeProxy(IdeUIAdapter.class);
        ErrorHandler errorHandler 
            = (ErrorHandler) makeProxy(ErrorHandler.class);
        
        AbstractIconRegistry iconRegistry = new AbstractIconRegistry() {
            protected AbstractIcon createIcon(String path) {
                return new AbstractIcon(new Object());
            }
        };
        StructureViewNodeFactory structureViewNodeFactory = 
            new StructureViewNodeFactory(iconRegistry) {
                protected StructureViewNode createConcreteNode(
                    StructureNode node,
                    AbstractIcon icon,
                    List children) {
                    return new SwingTreeViewNode(node, icon, children);
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
            errorHandler
        );
        
        Ajde.getDefault().getConfigurationManager().setActiveConfigFile(config.getAbsolutePath());
    }
    
    private File writeConfig(String[] args) {
        File result = new File(FileUtil.getTempDir("CompileCommand"), "config.lst");
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
        } finally{
            try {
                out.close();
            } catch (IOException e) {
            }
        }
    }
    
    private Object makeProxy(Class interfac){
        return Proxy.newProxyInstance(
            interfac.getClassLoader(),
            new Class[] { interfac },
            proxy);          
    }
}

class MyTaskListManager extends MessageHandler 
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
        addSourcelineTask(new Message(message, kind, null, sourceLocation));
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

