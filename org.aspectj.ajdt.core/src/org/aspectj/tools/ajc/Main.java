/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/



package org.aspectj.tools.ajc;

import org.aspectj.bridge.AbortException;
import org.aspectj.bridge.ICommand;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessageHandler;
import org.aspectj.bridge.IMessageHolder;
import org.aspectj.bridge.Message;
import org.aspectj.bridge.MessageHandler;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.bridge.ReflectionFactory;
import org.aspectj.bridge.Version;
import org.aspectj.util.LangUtil;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Programmatic and command-line interface to AspectJ compiler.
 * The compiler is an ICommand obtained by reflection.
 * Not thread-safe.
 */
public class Main {
    
    /** @param args the String[] of command-line arguments */
    public static void main(String[] args) throws IOException {
        new Main().runMain(args, true);
    }

    
    /** append nothing if numItems is 0,
     * numItems + label + (numItems > 1? "s" : "") otherwise,
     * prefixing with " " if sink has content
     */
    private static void appendNLabel(StringBuffer sink, String label, int numItems) {
        if (0 == numItems) {
            return;
        }
        if (0 < sink.length()) {
            sink.append(", ");
        }
        sink.append(numItems + " ");
        if (!LangUtil.isEmpty(label)) {
            sink.append(label);
        }
        if (1 < numItems) {
            sink.append("s");
        }
    }
    
    /** control iteration/continuation for command (compiler) */
    protected CommandController controller;
    
    /** ReflectionFactory identifier for command (compiler) */
    protected String commandName;
    
    /** client-set message sink */
    private IMessageHolder clientHolder;
    
    /** internally-set message sink */
    private final MessageHandler ourHandler;
        
    private int lastFails;
    private int lastErrors;

    public Main() {
        controller = new CommandController();
        commandName = ReflectionFactory.ECLIPSE;
        ourHandler = new MessageHandler(true);
    }    
    
    /**
     * Run without throwing exceptions but optionally using System.exit(..).
     * This sets up a message handler which emits messages immediately,
     * so report(boolean, IMessageHandler) only reports total number
     * of errors or warnings.
     * @param args the String[] command line for the compiler
     * @param useSystemExit if true, use System.exit(int) to complete
     *         unless one of the args is -noExit. 
     * and signal result (0 no exceptions/error, <0 exceptions, >0 compiler errors).
     */
    public void runMain(String[] args, boolean useSystemExit) {
        boolean verbose = (-1 != ("" + LangUtil.arrayAsList(args)).indexOf("-verbose"));
        IMessageHolder holder = clientHolder;
        if (null == holder) {
            holder = ourHandler;
            if (verbose) {
                ourHandler.setInterceptor(MessagePrinter.VERBOSE);
            } else {
                ourHandler.ignore(IMessage.INFO);
                ourHandler.setInterceptor(MessagePrinter.TERSE);
            }
        }
        run(args, holder);

        boolean skipExit = false;
        if (useSystemExit && !LangUtil.isEmpty(args)) {  // sigh - pluck -noExit
            for (int i = 0; i < args.length; i++) {
				if ("-noExit".equals(args[i])) {
                    skipExit = true;
                    break;
                }
			}
        }
        if (useSystemExit && !skipExit) {
            systemExit(holder);
        }
    }

    /**
     * Run without using System.exit(..), putting all messages in holder:
     * <ul>
     * <li>ERROR: compiler error</li>
     * <li>WARNING: compiler warning</li>
     * <li>FAIL: command error (bad arguments, exception thrown)</li>
     * </ul>
     * This handles incremental behavior:
     * <ul>
     * <li>If args include "-incremental", repeat for every input char
     *     until 'q' is entered.<li>
     * <li>If args include "-incrementalTagFile {file}", repeat every time
     *     we detect that {file} modification time has changed. </li>
     * <li>Either way, list files recompiled each time if args includes "-verbose".</li>
     * <li>Exit when the commmand/compiler throws any Throwable.</li>
     * </ul>
     * When complete, this contains all the messages of the final
     * run of the command and/or any FAIL messages produced in running
     * the command, including any Throwable thrown by the command itself.
     * 
     * @param args the String[] command line for the compiler
     * @param holder the MessageHandler sink for messages.
     */
    public void run(String[] args, IMessageHolder holder) {
        if (LangUtil.isEmpty(args)) {
            args = new String[] { "-help" };
        } else if (Arrays.asList(args).contains("-version")) {
        	System.out.println("AspectJ Compiler " + Version.text);
        	System.out.println();
        	return;
    	} else if (controller.running()) {
            fail(holder, "already running with controller: " + controller, null);
            return;
        } 
        args = controller.init(args, holder);
        if ((0 < holder.numMessages(IMessage.ERROR, true))
            || (!validArgs(args, controller.incremental(), holder))) {
            return;
        }      
        ICommand command = ReflectionFactory.makeCommand(commandName, holder);
        if (0 < holder.numMessages(IMessage.ERROR, true)) {
            return;
        }      
        try {
            boolean verbose = (-1 != ("" + Arrays.asList(args)).indexOf("-verbose"));
            boolean passed = command.runCommand(args, holder);
            if (report(passed, holder) && controller.incremental()) {
                final boolean onCommandLine = controller.commandLineIncremental();
                while (controller.doRepeatCommand()) {
                    passed = command.repeatCommand(holder);
                    if (!report(passed, holder)) {
                        break;
                    } else {
                        holder.clearMessages();
                    }
                }
            }
        } catch (AbortException ae) {
        	if (AbortException.ABORT.equals(ae)) { 
        		quit();
        	} else {
                IMessage message = ae.getIMessage();
                Throwable thrown = ae.getThrown();
                if (null == thrown) { // toss AbortException wrapper
                    if (null != message) {
                        holder.handleMessage(message);
                    } else {
                        fail(holder, "abort without message", ae);
                    }
                } else if (null == message) {
                    fail(holder, "aborted", thrown);
                } else {
                    String mssg = MessageUtil.MESSAGE_MOST.renderToString(message);
                    fail(holder, mssg, thrown);
                }
        	}
    	} catch (Throwable t) {
            fail(holder, "thrown?", t);
        }
    }
    
    /** call this to stop after the next iteration of incremental compile */
    public void quit() {
        controller.quit();
    }

    /**
     * Set holder to be passed all messages.
     * When holder is set, messages will not be printed by default.
     * @param holder the IMessageHolder sink for all messages
     *         (use null to restore default behavior)
     */
    public void setHolder(IMessageHolder holder) {
        clientHolder = holder;
    }
        
    /**
     * Nicer messages for some illegal argument combinations
     */
    protected boolean validArgs(
        String[] args, 
        boolean incremental, 
        IMessageHandler handler) {
        if (incremental) {
            List list = LangUtil.arrayAsList(args);
            if (!list.contains("-sourceroots")) { // XXX -sourceroot name
                fail(handler, "incremental mode requires -sourceroots", null);
                return false;
            }
            // XXX also check for -argfile, @... or ...[.java|.aj]
        }
        return true;
    }
    
    /**
     * Call System.exit(int) with values derived from the number
     * of failures/aborts or errors in messages.
     * @param messages the IMessageHolder to interrogate.
     * @param messages
     */
    protected void systemExit(IMessageHolder messages) {
        int num = lastFails; // messages.numMessages(IMessage.FAIL, true);
        if (0 < num) {
            System.exit(-num);
        }
        num = lastErrors; // messages.numMessages(IMessage.ERROR, false);
        if (0 < num) {
            System.exit(num);
        }
        System.exit(0);        
    }   

    /** Messages to the user */
    protected void outMessage(String message) {  // XXX coordinate with MessagePrinter
        System.out.print(message);
        System.out.flush();
    }
    
    /** 
     * Report results from a (possibly-incremental) compile run.
     * This delegates to any reportHandler or otherwise
     * prints summary counts of errors/warnings to System.err (if any errors) 
     * or System.out (if only warnings).
     * WARNING: this silently ignores other messages like FAIL,
     * but clears the handler of all messages when returning true. XXX false
     * 
     * This implementation ignores the pass parameter but
     * clears the holder after reporting
     * on the assumption messages were handled/printed already.
     * (ignoring UnsupportedOperationException from holder.clearMessages()).
     * @param pass true result of the command
     * @param holder IMessageHolder with messages from the command
     * @see reportCommandResults(IMessageHolder)
     * @return false if the process should abort
     */
    protected boolean report(boolean pass, IMessageHolder holder) {
        lastFails = holder.numMessages(IMessage.FAIL, true);
        boolean result = (0 == lastFails);
        if (holder == ourHandler) {
            lastErrors = holder.numMessages(IMessage.ERROR, false);
            int warnings = holder.numMessages(IMessage.WARNING, false);
            StringBuffer sb = new StringBuffer();
            appendNLabel(sb, "fail|abort", lastFails);
            appendNLabel(sb, "error", lastErrors);
            appendNLabel(sb, "warning", warnings);
            if (0 < sb.length()) {
                PrintStream out = (0 < (lastErrors + lastFails) 
                    ? System.err 
                    : System.out);
                out.println(""); // XXX "wrote class file" messages no eol?
                out.println(sb.toString());
                if (false) { // printed elsewhere so printed when found
                    if (0 < lastFails) {
                        MessageUtil.print(System.err, holder, "", MessageUtil.MESSAGE_ALL, MessageUtil.PICK_FAIL_PLUS);
                    }
                    if (0 < lastErrors) {
                        MessageUtil.print(System.err, holder, "", MessageUtil.MESSAGE_ALL, MessageUtil.PICK_ERROR);
                    }
                    if (0 < warnings) {
                        MessageUtil.print(System.err, holder, "", MessageUtil.MESSAGE_ALL, MessageUtil.PICK_WARNING);
                    }
                }
            }
        }
        return result;
    }
        
    /** convenience API to make fail messages (without MessageUtils's fail prefix) */
    protected static void fail(IMessageHandler handler, String message, Throwable thrown) {
        handler.handleMessage(new Message(message, IMessage.FAIL, thrown, null));
    }  
    
    /** interceptor IMessageHandler to print as we go */
    public static class MessagePrinter implements IMessageHandler {
        // XXX change as needed before each release
        public static final String THROWN_PREFIX =
            "Exception thrown from AspectJ "+ Version.text + LangUtil.EOL
        + ""+ LangUtil.EOL
        + "Please email to us as follows:" + LangUtil.EOL
        + "       to: jitterbug@aspectj.org" + LangUtil.EOL
        + "  subject: top stack trace File:line, e.g., \"SomeFile.java:243\"" + LangUtil.EOL
        + "  message: copy the entire stack trace." + LangUtil.EOL
        + "" + LangUtil.EOL
        + "Your message can also request follow-up or provide a workaround." + LangUtil.EOL
        + "To make the bug a priority, please include a test program." + LangUtil.EOL
        + "" + LangUtil.EOL
        + "You may search for duplicate bugs (i.e., known workarounds):" + LangUtil.EOL
        + "" + LangUtil.EOL
        + "http://aspectj.org/bugs" + LangUtil.EOL
        + "http://aspectj.org/bugs/incoming?expression=SomeFile.java:243" + LangUtil.EOL
        + ""  + LangUtil.EOL;
   
        public static final IMessageHandler VERBOSE 
            = new MessagePrinter(true);
        public static final IMessageHandler TERSE
            = new MessagePrinter(false);
            
        final boolean verbose;
		protected MessagePrinter(boolean verbose) {
            this.verbose = verbose;
        }
        
        /** 
         * Print errors and warnings to System.err,
         * and optionally info to System.out,
         * rendering message String only.
         * @return false always
         */
        public boolean handleMessage(IMessage message) {
			if (null != message) {
                PrintStream out = getStreamFor(message.getKind());
                if (null != out) {
                    out.println(render(message));
                }
            }
            return false;
		}
        
        protected String render(IMessage message) {
            IMessage.Kind kind = message.getKind();
            if (kind.equals(IMessage.ABORT)) {
                Throwable t = message.getThrown();
                if (null == t) {
                    return "abort (no message)";
                } else {
                    return render(t);
                }
            }
            String m = message.getMessage();
            if (LangUtil.isEmpty(m)) {
                m = message.toString();
            }
            return m;
        }

        protected String render(Throwable t) {
            String m = t.getMessage();
            return THROWN_PREFIX 
                + (null != m ? m + "\n": "") 
                + LangUtil.renderException(t, true);
        }
        		
        public boolean isIgnoring(IMessage.Kind kind) {
			return (null != getStreamFor(kind));
		}
        
        /** @return System.err for FAIL, ABORT, ERROR, and WARNING, 
         *           System.out for INFO if verbose.
         */
        protected PrintStream getStreamFor(IMessage.Kind kind) {
            if (IMessage.FAIL.equals(kind)
                || IMessage.ERROR.equals(kind)
                || IMessage.WARNING.equals(kind)
                || IMessage.ABORT.equals(kind)) {
                return System.err;
            } else if (verbose && IMessage.INFO.equals(kind)) {
                return System.out;
            } else {
                return null;
            }
        }
    }
    
    /** controller for repeatable command delays until input or file changed or removed */
    public static class CommandController {
        public static String TAG_FILE_OPTION = "-XincrementalFile"; 
        public static String INCREMENTAL_OPTION = "-incremental";
        
        /** maximum 10-minute delay between filesystem checks */
        public static long MAX_DELAY = 1000 * 600;
        
        /** default 5-second delay between filesystem checks */
        public static long DEFAULT_DELAY = 1000 * 5;
        
        /** @see init(String[]) */
        private static String[][] OPTIONS = new String[][]
            { new String[] { INCREMENTAL_OPTION },
                new String[] { TAG_FILE_OPTION, null } };


        /** true between init(String[]) and doRepeatCommand() that returns false */
        private boolean running;

        /** true after quit() called */
        private boolean quit;

        /** true if incremental mode, waiting for input other than 'q' */
        private boolean incremental;
        
        /** true if incremental mode, waiting for file to change (repeat) or disappear (quit) */
        private File tagFile;
        
        /** last modification time for tagFile as of last command - 0 to start */
        private long fileModTime;
        
        /** delay between filesystem checks for tagFile modification time */
        private long delay;
        
        public CommandController() {
            delay = DEFAULT_DELAY;
        }
        
        /**
         * @param argList read and strip incremental args from this
         * @param sink IMessageHandler for error messages
         * @return String[] remainder of args
         */
        public String[] init(String[] args, IMessageHandler sink) {
            running = true;
            if (!LangUtil.isEmpty(args)) {
                String[][] options = LangUtil.copyStrings(OPTIONS);
                args = LangUtil.extractOptions(args, options);
                incremental = (null != options[0][0]);
                if (null != options[1][0]) {
                    tagFile = new File(options[1][1]);
                    if (!tagFile.exists()) {
                        MessageUtil.abort(sink, "tag file does not exist: " + tagFile);
                    }
                }
            }
            return args;            
        }
        
        /** @return true if init(String[]) called but doRepeatCommand has not 
         * returned false */
        public boolean running() {
            return running;
        }
        
        /** @param delay milliseconds between filesystem checks */
        public void setDelay(long delay) {
            if ((delay > -1) && (delay < MAX_DELAY)) {
                this.delay = delay;
            }
        }
        
        /** @return true if INCREMENTAL_OPTION or TAG_FILE_OPTION was in args */
        public boolean incremental() {
            return (incremental || (null != tagFile));
        }
        

        /** @return true if INCREMENTAL_OPTION was in args */
        public boolean commandLineIncremental() {
            return incremental;
        }
        
        public void quit() {
            if (!quit) {
                quit = true;
            }
        }
        
        /** @return false if we should quit, true to do another command */
        boolean doRepeatCommand() {
            if (!running) {
                return false;
            }
            boolean result = false;
            if (quit) {
                result = false;
            } else if (incremental) {
                try {  
                    System.out.println("    press enter to recompile (q to quit): ");
                    System.out.flush();
                    boolean doMore = false;
                    // seek for one q or a series of [\n\r]...
                    do {
                        int input = System.in.read();
                        if ('q' == input) {
                            break;  // result = false;
                        } else if (('\n' == input) || ('\r' == input)) {
                            result = true;
                        } // else eat anything else
                    } while (!result);
                    System.in.skip(Integer.MAX_VALUE);
                } catch (IOException e) { // XXX silence for error?
                    result = false;
                } 
            } else if (null != tagFile) {
                long curModTime;
                while (true) {
                    if (!tagFile.exists()) {
                        result = false;
                        break;
                    } else if (fileModTime == (curModTime = tagFile.lastModified())) {
                        fileCheckDelay();
                    } else {
                        fileModTime = curModTime;
                        result = true;
                        break;
                    }
                }
            } // else, not incremental - false
            if (!result && running) {
                running = false;
            }        
            return result;
        }
        
        /** delay between filesystem checks, returning if quit is set */
        protected void fileCheckDelay() {
            final Thread thread = Thread.currentThread();
            long targetTime = System.currentTimeMillis() + delay;
            long curTime;
            while (targetTime > (curTime = System.currentTimeMillis())) {
                if (quit) {
                    return;
                }
                try { Thread.sleep(300); } // 1/3-second delta for quit check
                catch (InterruptedException e) {}
            }
        }
    }
}
