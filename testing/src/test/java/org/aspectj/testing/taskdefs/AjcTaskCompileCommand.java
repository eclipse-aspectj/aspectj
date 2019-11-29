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

package org.aspectj.testing.taskdefs;

//import java.awt.Frame;
import java.io.File;
import java.io.IOException;
//import java.lang.reflect.*;
//import java.util.List;
import java.util.ArrayList;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.aspectj.bridge.ICommand;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessageHandler;
import org.aspectj.bridge.IMessageHolder;
import org.aspectj.bridge.MessageHandler;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.tools.ant.taskdefs.AjcTask;
import org.aspectj.util.FileUtil;
import org.aspectj.util.LangUtil;

/**
 * Drive tests using the Ant taskdef.
 * The non-incremental case is quite easy to implement,
 * but incremental compiles require running the compiler
 * in another thread using an incremental tag file.
 * This is imprecise because it assumes
 * incremental compiles are complete 
 * when messages stop coming from the compiler.
 * Also, the client should call quit() when done compiling
 * to halt the process.
 * XXX build up ICommand with quit (and done-with-last-invocation?)
 *     to avoid the kludge workarounds
 */
public class AjcTaskCompileCommand implements ICommand {
    /** 
     * 20 seconds of quiet in message holder 
     * before we assume incremental compile is done 
     */
    public static int COMPILE_SECONDS_WITHOUT_MESSAGES = 20;
    
    /** 5 minutes maximum time to wait for a compile to complete */
    public static int COMPILE_MAX_SECONDS = 300;

    /**
     * Wait until this holder has not changed the number of messages
     * in secs seconds, as a weak form of determining when the
     * compile has completed.
     * XXX implement "compile-complete" message instead.
     * @param holder the IMessageHolder to wait for
     * @param seconds the number of seconds that the messages should be the same
     * @param timeoutSeconds the int number of seconds after which to time out
     * @return true if the messages quiesced before the timeout
     *   false if parameters are 0 or negative or if
     *   seconds => timeoutSeconds
     */
    static boolean waitUntilMessagesQuiet(
            IMessageHolder holder, 
            int seconds,
            int timeoutSeconds) {
        LangUtil.throwIaxIfNull(holder, "holder");
        if ((0 >= timeoutSeconds) || (0 >= seconds)
            || (timeoutSeconds <= seconds)) {
            return false;
        }
        long curTime = System.currentTimeMillis();
        final long timeout = curTime + (timeoutSeconds*1000);
//        final Thread thread = Thread.currentThread();
        final long targetQuietTime = 1000 * seconds;
        int numMessages = holder.numMessages(null, true);
        long numMessagesTime = curTime;
        // check for new messages every .5 to 3 seconds
        final long checkInterval;
        {
            long interval = seconds / 10;
            if (interval > 3000) {
                interval = 3000;
            } else if (interval < 200) {
                interval = 500;
            }
            checkInterval = interval;
        }
        while ((curTime < timeout)
                && (curTime < (numMessagesTime + targetQuietTime))) {
            // delay until next check
            long nextCheck = curTime + checkInterval;
            while (nextCheck > curTime) {
                try {
                    Thread.sleep(nextCheck - curTime);
                } catch (InterruptedException e) {
                    // ignore
                }
                curTime = System.currentTimeMillis();            
            }
            int newNumMessages = holder.numMessages(null, true);
            if (newNumMessages != numMessages) {
                numMessages = newNumMessages;
                numMessagesTime = curTime;
            }
        }
        return (curTime >= (numMessagesTime + targetQuietTime));
    }
    
    // single-threaded driver
    MessageHandler messages = new MessageHandler();
    AjcTask ajcTask;
    File incrementalFile;
    Thread incrementalCompileThread;

    /** 
     * Stop waiting for any further incremental compiles.
     * Safe to call in non-incremental modes.
     */
    public void quit() { // XXX requires downcast from ICommand, need validator,IMessageHandler interface
        AjcTask task = ajcTask;
        if (null != task) {
            task.quit(); // signal task to quit, thread to die
            ajcTask = null; // XXX need for cleanup?
        }        
        updateIncrementalFile(false, true);
        Thread thread = incrementalCompileThread;
        if (null != thread) {
            if (thread.isAlive()) {
                try {
                    thread.join(3000);
                } catch (InterruptedException e) {
                    // ignore
                }
                if (thread.isAlive()) {
                    String s = "WARNING: abandoning undying thread ";
                    System.err.println(s + thread.getName());
                }
            }
            incrementalCompileThread = null;
        }
    }
    
    // --------- ICommand interface
    public boolean runCommand(String[] args, IMessageHandler handler) {
        return (makeAjcTask(args, handler) 
            && doCommand(handler, false));
    }

    /**
     * Fails if called before runCommand or if
     * not in incremental mode or if the command
     * included an incremental file entry.
     * @return
     */
    public boolean repeatCommand(IMessageHandler handler) {
        return doCommand(handler, true);
    }
    
    protected boolean doCommand(IMessageHandler handler, boolean repeat) {
        messages.clearMessages();
        if (null == ajcTask) {
            MessageUtil.fail(messages, "ajcTask null - repeat without do");
        }
        try {
            // normal, non-incremental case
            if (!repeat && (null == incrementalFile)) {
                ajcTask.execute();
            // rest is for incremental mode
            } else if (null == incrementalFile)  {
                MessageUtil.fail(messages, "incremental mode not specified");
            } else if (!updateIncrementalFile(false, false)) {
                MessageUtil.fail(messages, "can't update incremental file");
            } else if (!repeat) { // first incremental compile
                incrementalCompileThread = new Thread(
                    new Runnable() {
                        public void run() {
                            ajcTask.execute();
                        }
                    }, "AjcTaskCompileCommand-incremental");
                incrementalCompileThread.start();
                waitUntilMessagesQuiet(messages, 10, 180);
            } else {
                Thread thread = incrementalCompileThread;
                if (null == thread) {
                    MessageUtil.fail(messages, "incremental process stopped");
                } else if (!thread.isAlive()) {
                    MessageUtil.fail(messages, "incremental process dead");
                } else {
                    waitUntilMessagesQuiet(messages, 10, 180);
                }
            }
        } catch (BuildException e) {
            Throwable t = e.getCause();
            while (t instanceof BuildException) {
                t = ((BuildException) t).getCause();
            }
            if (null == t) {
                t = e;
            }
            MessageUtil.abort(messages, "BuildException " + e.getMessage(), t);
        } finally {
            MessageUtil.handleAll(handler, messages, false);
        }
       return (0 == messages.numMessages(IMessage.ERROR, true)); 
    }


    protected boolean makeAjcTask(String[] args, IMessageHandler handler) {
        ajcTask = null;
        incrementalFile = null;
        AjcTask result = null;
        try {
            result = new AjcTask();
            Project project = new Project();
            project.setName("AjcTaskCompileCommand");
            result.setProject(project);
            result.setMessageHolder(messages);
            // XXX argh - have to strip out "-incremental"
            // because tools.ajc.Main privileges it over tagfile
            ArrayList newArgs = new ArrayList();
            boolean incremental = false;
            for (int i = 0; i < args.length; i++) {
                if ("-incremental".equals(args[i])) {
                    incremental = true;
                } else if ("-XincrementalFile".equals(args[i])) {
                    // CommandController.TAG_FILE_OPTION = "-XincrementalFile";
                    incremental = true;
                    i++;
                } else {
                    newArgs.add(args[i]);
                }
            }
            if (incremental) {
                args = (String[]) newArgs.toArray(new String[0]);
            }
                                
            result.readArguments(args);
            
            if (incremental || result.isInIncrementalMode()) {
                // these should be impossible...
                if (result.isInIncrementalFileMode()) {
                    String m = "incremental file set in command";
                    MessageUtil.fail(handler, m);
                    return false;
                } else if (null != incrementalFile) {
                    String m = "incremental file set already";
                    MessageUtil.fail(handler, m);
                    return false;
                }
                String prefix = "AjcTaskCompileCommand_makeAjcTask";
                File tmpDir = FileUtil.getTempDir(prefix);
                incrementalFile = new File(tmpDir, "tagfile.tmp");
                boolean create = true;
                boolean delete = false;
                updateIncrementalFile(create, delete);
                result.setTagFile(incrementalFile);
            }
            // do not throw BuildException on error
            result.setFailonerror(false);
            ajcTask = result;
        } catch (BuildException e) {
            MessageUtil.abort(handler,"setting up AjcTask", e);
        }
        return (null != ajcTask);
    }
    
    protected boolean updateIncrementalFile(boolean create, boolean delete) {
        File file = incrementalFile;
        if (delete) {
            try {
                return (null == file)
                    || !file.exists() 
                    || file.delete();
            } finally {
                incrementalFile = null;
            }
        }
        if (null == file) {
            return false;
        }
        if (file.exists()) {
            return file.setLastModified(System.currentTimeMillis());
        } else {
            try {
                return create && file.createNewFile();
            } catch (IOException e) { // XXX warn in verbose mode?
                return false;
            }
        }
    }
    
}

