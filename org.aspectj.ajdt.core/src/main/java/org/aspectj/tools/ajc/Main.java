/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/

package org.aspectj.tools.ajc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.aspectj.ajdt.internal.core.builder.AjBuildManager;
import org.aspectj.bridge.AbortException;
import org.aspectj.bridge.ICommand;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessage.Kind;
import org.aspectj.bridge.IMessageHandler;
import org.aspectj.bridge.IMessageHolder;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.bridge.Message;
import org.aspectj.bridge.MessageHandler;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.bridge.ReflectionFactory;
import org.aspectj.bridge.Version;
import org.aspectj.bridge.context.CompilationAndWeavingContext;
import org.aspectj.util.FileUtil;
import org.aspectj.util.LangUtil;
import org.aspectj.weaver.Dump;

/**
 * Programmatic and command-line interface to AspectJ compiler. The compiler is an ICommand obtained by reflection. Not thread-safe.
 * By default, messages are printed as they are emitted; info messages go to the output stream, and warnings and errors go to the
 * error stream.
 * <p>
 * Clients can handle all messages by registering a holder:
 * 
 * <pre>
 * Main main = new Main();
 * IMessageHolder holder = new MessageHandler();
 * main.setHolder(holder);
 * </pre>
 * 
 * Clients can get control after each command completes by installing a Runnable:
 * 
 * <pre>
 * main.setCompletionRunner(new Runnable() {..});
 * </pre>
 * 
 */
public class Main {
	/** Header used when rendering exceptions for users */
	public static final String THROWN_PREFIX = "Exception thrown from AspectJ " + Version.getText() + LangUtil.EOL + "" + LangUtil.EOL
			+ "This might be logged as a bug already -- find current bugs at" + LangUtil.EOL
			+ "  http://bugs.eclipse.org/bugs/buglist.cgi?product=AspectJ&component=Compiler" + LangUtil.EOL + "" + LangUtil.EOL
			+ "Bugs for exceptions thrown have titles File:line from the top stack, " + LangUtil.EOL
			+ "e.g., \"SomeFile.java:243\"" + LangUtil.EOL + "" + LangUtil.EOL
			+ "If you don't find the exception below in a bug, please add a new bug" + LangUtil.EOL
			+ "at http://bugs.eclipse.org/bugs/enter_bug.cgi?product=AspectJ" + LangUtil.EOL
			+ "To make the bug a priority, please include a test program" + LangUtil.EOL + "that can reproduce this exception."
			+ LangUtil.EOL;

	private static final String OUT_OF_MEMORY_MSG = "AspectJ " + Version.getText() + " ran out of memory during compilation:"
			+ LangUtil.EOL + LangUtil.EOL + "Please increase the memory available to ajc by editing the ajc script " + LangUtil.EOL
			+ "found in your AspectJ installation directory. The -Xmx parameter value" + LangUtil.EOL
			+ "should be increased from 64M (default) to 128M or even 256M." + LangUtil.EOL + LangUtil.EOL
			+ "See the AspectJ FAQ available from the documentation link" + LangUtil.EOL
			+ "on the AspectJ home page at http://www.eclipse.org/aspectj";

	private static final String MESSAGE_HOLDER_OPTION = "-messageHolder";

	/** @param args the String[] of command-line arguments */
	public static void main(String[] args) throws IOException {
		new Main().runMain(args, true);
	}

	/**
	 * Convenience method to run ajc and collect String lists of messages. This can be reflectively invoked with the List collecting
	 * parameters supplied by a parent class loader. The String messages take the same form as command-line messages.
	 * This method does not catch unchecked exceptions thrown by the compiler.
	 * 
	 * @param args the String[] args to pass to the compiler
	 * @param useSystemExit if true and errors, return System.exit(errs)
	 * @param fails the List sink, if any, for String failure (or worse) messages
	 * @param errors the List sink, if any, for String error messages
	 * @param warnings the List sink, if any, for String warning messages
	 * @param infos the List sink, if any, for String info messages
	 * @return number of messages reported with level ERROR or above
	 */
	public static int bareMain(String[] args, boolean useSystemExit, List fails, List errors, List warnings, List infos) {
		Main main = new Main();
		MessageHandler holder = new MessageHandler();
		main.setHolder(holder);
		try {
			main.runMain(args, useSystemExit);
		} finally {
			readMessages(holder, IMessage.FAIL, true, fails);
			readMessages(holder, IMessage.ERROR, false, errors);
			readMessages(holder, IMessage.WARNING, false, warnings);
			readMessages(holder, IMessage.INFO, false, infos);
		}
		return holder.numMessages(IMessage.ERROR, true);
	}

	/** Read messages of a given kind into a List as String */
	private static void readMessages(IMessageHolder holder, IMessage.Kind kind, boolean orGreater, List sink) {
		if ((null == sink) || (null == holder)) {
			return;
		}
		IMessage[] messages = holder.getMessages(kind, orGreater);
		if (!LangUtil.isEmpty(messages)) {
			for (IMessage message : messages) {
				sink.add(MessagePrinter.render(message));
			}
		}
	}

	/**
	 * @return String rendering throwable as compiler error for user/console, including information on how to report as a bug.
	 * @throws NullPointerException if thrown is null
	 */
	public static String renderExceptionForUser(Throwable thrown) {
		String m = thrown.getMessage();
		return THROWN_PREFIX + (null != m ? m + "\n" : "") + "\n" + CompilationAndWeavingContext.getCurrentContext()
				+ LangUtil.renderException(thrown, true);
	}

	private static String parmInArgs(String flag, String[] args) {
		int loc = 1 + (null == args ? -1 : Arrays.asList(args).indexOf(flag));
		return ((0 == loc) || (args.length <= loc) ? null : args[loc]);
	}

	private static boolean flagInArgs(String flag, String[] args) {
		return ((null != args) && (Arrays.asList(args).contains(flag)));
	}

	/**
	 * append nothing if numItems is 0, numItems + label + (numItems > 1? "s" : "") otherwise, prefixing with " " if sink has
	 * content
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

	protected ICommand command;

	/** client-set message sink */
	private IMessageHolder clientHolder;

	/** internally-set message sink */
	protected final MessageHandler ourHandler;

	private int lastFails;
	private int lastErrors;

	/** if not null, run this synchronously after each compile completes */
	private Runnable completionRunner;

	public Main() {
		controller = new CommandController();
		commandName = ReflectionFactory.ECLIPSE;
		CompilationAndWeavingContext.setMultiThreaded(false);
		try {
			String value = System.getProperty("aspectj.multithreaded");
			if (value != null && value.equalsIgnoreCase("true")) {
				CompilationAndWeavingContext.setMultiThreaded(true);
			}
		} catch (Exception e) {
			// silent
		}
		ourHandler = new MessageHandler(true);
	}

	public MessageHandler getMessageHandler() {
		return ourHandler;
	}

	// for unit testing...
	void setController(CommandController controller) {
		this.controller = controller;
	}

	public void setCommand(ICommand command) {
		this.command = command;
	}

	/**
	 * Run without throwing exceptions but optionally using System.exit(..). This sets up a message handler which emits messages
	 * immediately, so report(boolean, IMessageHandler) only reports total number of errors or warnings.
	 * 
	 * @param args the String[] command line for the compiler
	 * @param useSystemExit if true, use System.exit(int) to complete unless one of the args is -noExit. and signal result (0 no
	 *        exceptions/error, &lt;0 exceptions, &gt;0 compiler errors).
	 */
	public void runMain(String[] args, boolean useSystemExit) {
		// Urk - default no check for AJDT, enabled here for Ant, command-line
		AjBuildManager.enableRuntimeVersionCheck(this);
		final boolean verbose = flagInArgs("-verbose", args);
		final boolean timers = flagInArgs("-timers", args);
		if (null == this.clientHolder) {
			this.clientHolder = checkForCustomMessageHolder(args);
		}
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

		// make sure we handle out of memory gracefully...
		try {
			// byte[] b = new byte[100000000]; for testing OoME only!
			long stime = System.currentTimeMillis();
			// uncomment next line to pause before startup (attach jconsole)
			// try {Thread.sleep(5000); }catch(Exception e) {}
			run(args, holder);
			long etime = System.currentTimeMillis();
			if (timers) {
				System.out.println("Compiler took " + (etime - stime) + "ms");
			}
			holder.handleMessage(MessageUtil.info("Compiler took " + (etime - stime) + "ms"));
			// uncomment next line to pause at end (keeps jconsole alive!)
			// try { System.in.read(); } catch (Exception e) {}
		} catch (OutOfMemoryError outOfMemory) {
			IMessage outOfMemoryMessage = new Message(OUT_OF_MEMORY_MSG, null, true);
			holder.handleMessage(outOfMemoryMessage);
			System.exit(-1); // we can't reasonably continue from this point.
		} finally {
			CompilationAndWeavingContext.reset();
			Dump.reset();
		}

		boolean skipExit = false;
		if (useSystemExit && !LangUtil.isEmpty(args)) { // sigh - pluck -noExit
			for (String arg : args) {
				if ("-noExit".equals(arg)) {
					skipExit = true;
					break;
				}
			}
		}
		if (useSystemExit && !skipExit) {
			systemExit(holder);
		}
	}

	// put calls around run() call above to allowing connecting jconsole
	// private void pause(int ms) {
	// try {
	// System.err.println("Pausing for "+ms+"ms");
	// System.gc();
	// Thread.sleep(ms);
	// System.gc();
	// System.err.println("Continuing");
	// } catch (Exception e) {}
	// }

	/**
	 * @param args
	 */
	private IMessageHolder checkForCustomMessageHolder(String[] args) {
		IMessageHolder holder = null;
		final String customMessageHolder = parmInArgs(MESSAGE_HOLDER_OPTION, args);
		if (customMessageHolder != null) {
			try {
				holder = (IMessageHolder) Class.forName(customMessageHolder).newInstance();
			} catch (Exception ex) {
				holder = ourHandler;
				throw new AbortException("Failed to create custom message holder of class '" + customMessageHolder + "' : " + ex);
			}
		}
		return holder;
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
	 * <li>If args include "-incremental", repeat for every input char until 'q' is entered.
	 * <li>
	 * <li>If args include "-incrementalTagFile {file}", repeat every time we detect that {file} modification time has changed.</li>
	 * <li>Either way, list files recompiled each time if args includes "-verbose".</li>
	 * <li>Exit when the commmand/compiler throws any Throwable.</li>
	 * </ul>
	 * When complete, this contains all the messages of the final run of the command and/or any FAIL messages produced in running
	 * the command, including any Throwable thrown by the command itself.
	 * 
	 * @param args the String[] command line for the compiler
	 * @param holder the MessageHandler sink for messages.
	 */
	public void run(String[] args, IMessageHolder holder) {

		PrintStream logStream = null;
		FileOutputStream fos = null;
		String logFileName = parmInArgs("-log", args);
		if (null != logFileName) {
			File logFile = new File(logFileName);
			try {
				logFile.createNewFile();
				fos = new FileOutputStream(logFileName, true);
				logStream = new PrintStream(fos, true);
			} catch (Exception e) {
				fail(holder, "Couldn't open log file: " + logFileName, e);
			}
			Date now = new Date();
			logStream.println(now.toString());
			if (flagInArgs("-verbose", args)) {
				ourHandler.setInterceptor(new LogModeMessagePrinter(true, logStream));
			} else {
				ourHandler.ignore(IMessage.INFO);
				ourHandler.setInterceptor(new LogModeMessagePrinter(false, logStream));
			}
			holder = ourHandler;
		}

		if (LangUtil.isEmpty(args)) {
			args = new String[] { "-?" };
		} else if (controller.running()) {
			fail(holder, "already running with controller: " + controller, null);
			return;
		}
		args = controller.init(args, holder);
		if (0 < holder.numMessages(IMessage.ERROR, true)) {
			return;
		}
		if (command == null) {
			command = ReflectionFactory.makeCommand(commandName, holder);
		}
		if (0 < holder.numMessages(IMessage.ERROR, true)) {
			return;
		}
		try {
			outer: while (true) {
				boolean passed = command.runCommand(args, holder);
				if (report(passed, holder) && controller.incremental()) {
					while (controller.doRepeatCommand(command)) {
						holder.clearMessages();
						if (controller.buildFresh()) {
							continue outer;
						} else {
							passed = command.repeatCommand(holder);
						}
						if (!report(passed, holder)) {
							break;
						}
					}
				}
				break;
			}
		} catch (AbortException ae) {
			if (ae.isSilent()) {
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
			fail(holder, "unexpected exception", t);
		} finally {
			if (logStream != null) {
				logStream.close();
				logStream = null;
			}
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					fail(holder, "unexpected exception", e);
				}
				fos = null;
			}
			command = null;
		}
	}

	/** call this to stop after the next iteration of incremental compile */
	public void quit() {
		controller.quit();
	}

	/**
	 * Set holder to be passed all messages. When holder is set, messages will not be printed by default.
	 * 
	 * @param holder the IMessageHolder sink for all messages (use null to restore default behavior)
	 */
	public void setHolder(IMessageHolder holder) {
		clientHolder = holder;
	}

	public IMessageHolder getHolder() {
		return clientHolder;
	}

	/**
	 * Install a Runnable to be invoked synchronously after each compile completes.
	 * 
	 * @param runner the Runnable to invoke - null to disable
	 */
	public void setCompletionRunner(Runnable runner) {
		this.completionRunner = runner;
	}

	/**
	 * Call System.exit(int) with values derived from the number of failures/aborts or errors in messages.
	 * 
	 * @param messages the IMessageHolder to interrogate.
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
	protected void outMessage(String message) { // XXX coordinate with MessagePrinter
		System.out.print(message);
		System.out.flush();
	}

	/**
	 * Report results from a (possibly-incremental) compile run. This delegates to any reportHandler or otherwise prints summary
	 * counts of errors/warnings to System.err (if any errors) or System.out (if only warnings). WARNING: this silently ignores
	 * other messages like FAIL, but clears the handler of all messages when returning true. XXX false
	 * 
	 * This implementation ignores the pass parameter but clears the holder after reporting on the assumption messages were
	 * handled/printed already. (ignoring UnsupportedOperationException from holder.clearMessages()).
	 * 
	 * @param pass true result of the command
	 * @param holder IMessageHolder with messages from the command
	 * @return false if the process should abort
	 */
	protected boolean report(boolean pass, IMessageHolder holder) {
		lastFails = holder.numMessages(IMessage.FAIL, true);
		boolean result = (0 == lastFails);
		final Runnable runner = completionRunner;
		if (null != runner) {
			runner.run();
		}
		if (holder == ourHandler) {
			lastErrors = holder.numMessages(IMessage.ERROR, false);
			int warnings = holder.numMessages(IMessage.WARNING, false);
			StringBuffer sb = new StringBuffer();
			appendNLabel(sb, "fail|abort", lastFails);
			appendNLabel(sb, "error", lastErrors);
			appendNLabel(sb, "warning", warnings);
			if (0 < sb.length()) {
				PrintStream out = (0 < (lastErrors + lastFails) ? System.err : System.out);
				out.println(""); // XXX "wrote class file" messages no eol?
				out.println(sb.toString());
			}
		}
		return result;
	}

	/** convenience API to make fail messages (without MessageUtils's fail prefix) */
	protected static void fail(IMessageHandler handler, String message, Throwable thrown) {
		handler.handleMessage(new Message(message, IMessage.FAIL, thrown, null));
	}

	/**
	 * interceptor IMessageHandler to print as we go. This formats all messages to the user.
	 */
	public static class MessagePrinter implements IMessageHandler {

		public static final IMessageHandler VERBOSE = new MessagePrinter(true);
		public static final IMessageHandler TERSE = new MessagePrinter(false);

		final boolean verbose;

		protected MessagePrinter(boolean verbose) {
			this.verbose = verbose;
		}

		/**
		 * Print errors and warnings to System.err, and optionally info to System.out, rendering message String only.
		 * 
		 * @return false always
		 */
		@Override
		public boolean handleMessage(IMessage message) {
			if (null != message) {
				PrintStream out = getStreamFor(message.getKind());
				if (null != out) {
					out.println(render(message));
				}
			}
			return false;
		}

		/**
		 * Render message differently. If abort, then prefix stack trace with feedback request. If the actual message is empty, then
		 * use toString on the whole. Prefix message part with file:line; If it has context, suffix message with context.
		 * 
		 * @param message the IMessage to render
		 * @return String rendering IMessage (never null)
		 */
		public static String render(IMessage message) {
			// IMessage.Kind kind = message.getKind();

			StringBuffer sb = new StringBuffer();
			String text = message.getMessage();
			if (text.equals(AbortException.NO_MESSAGE_TEXT)) {
				text = null;
			}
			boolean toString = (LangUtil.isEmpty(text));
			if (toString) {
				text = message.toString();
			}
			ISourceLocation loc = message.getSourceLocation();
			String context = null;
			if (null != loc) {
				File file = loc.getSourceFile();
				if (null != file) {
					String name = file.getName();
					if (!toString || (!text.contains(name))) {
						sb.append(FileUtil.getBestPath(file));
						if (loc.getLine() > 0) {
							sb.append(":" + loc.getLine());
						}
						int col = loc.getColumn();
						if (0 < col) {
							sb.append(":" + col);
						}
						sb.append(" ");
					}
				}
				context = loc.getContext();
			}

			// per Wes' suggestion on dev...
			if (message.getKind() == IMessage.ERROR) {
				sb.append("[error] ");
			} else if (message.getKind() == IMessage.WARNING) {
				sb.append("[warning] ");
			}

			sb.append(text);
			if (null != context) {
				sb.append(LangUtil.EOL);
				sb.append(context);
			}

			String details = message.getDetails();
			if (details != null) {
				sb.append(LangUtil.EOL);
				sb.append('\t');
				sb.append(details);
			}
			Throwable thrown = message.getThrown();
			if (null != thrown) {
				sb.append(LangUtil.EOL);
				sb.append(Main.renderExceptionForUser(thrown));
			}

			if (message.getExtraSourceLocations().isEmpty()) {
				return sb.toString();
			} else {
				return MessageUtil.addExtraSourceLocations(message, sb.toString());
			}

		}

		@Override
		public boolean isIgnoring(IMessage.Kind kind) {
			return (null != getStreamFor(kind));
		}

		/**
		 * No-op
		 * 
		 * @see org.aspectj.bridge.IMessageHandler#isIgnoring(org.aspectj.bridge.IMessage.Kind)
		 * @param kind
		 */
		@Override
		public void dontIgnore(IMessage.Kind kind) {

		}

		/**
		 * @return System.err for FAIL, ABORT, ERROR, and WARNING, System.out for INFO if -verbose and WEAVEINFO if -showWeaveInfo.
		 */
		protected PrintStream getStreamFor(IMessage.Kind kind) {
			if (IMessage.WARNING.isSameOrLessThan(kind)) {
				return System.err;
			} else if (verbose && IMessage.INFO.equals(kind)) {
				return System.out;
			} else if (IMessage.WEAVEINFO.equals(kind)) {
				return System.out;
			} else {
				return null;
			}
		}

		/**
		 * No-op
		 * 
		 * @see org.aspectj.bridge.IMessageHandler#ignore(org.aspectj.bridge.IMessage.Kind)
		 * @param kind
		 */
		@Override
		public void ignore(Kind kind) {
		}
	}

	public static class LogModeMessagePrinter extends MessagePrinter {

		protected final PrintStream logStream;

		public LogModeMessagePrinter(boolean verbose, PrintStream logStream) {
			super(verbose);
			this.logStream = logStream;
		}

		@Override
		protected PrintStream getStreamFor(IMessage.Kind kind) {
			if (IMessage.WARNING.isSameOrLessThan(kind)) {
				return logStream;
			} else if (verbose && IMessage.INFO.equals(kind)) {
				return logStream;
			} else if (IMessage.WEAVEINFO.equals(kind)) {
				return logStream;
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
		private static String[][] OPTIONS = new String[][] { new String[] { INCREMENTAL_OPTION },
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

		/** true just after user types 'r' for rebuild */
		private boolean buildFresh;

		public CommandController() {
			delay = DEFAULT_DELAY;
		}

		/**
		 * @param args read and strip incremental args from this
		 * @param sink IMessageHandler for error messages
		 * @return String[] remainder of args
		 */
		public String[] init(String[] args, IMessageHandler sink) {
			running = true;
			// String[] unused;
			if (!LangUtil.isEmpty(args)) {
				String[][] options = LangUtil.copyStrings(OPTIONS);
				/* unused = */LangUtil.extractOptions(args, options);
				incremental = (null != options[0][0]);
				if (null != options[1][0]) {
					File file = new File(options[1][1]);
					if (!file.exists()) {
						MessageUtil.abort(sink, "tag file does not exist: " + file);
					} else {
						tagFile = file;
						fileModTime = tagFile.lastModified();
					}
				}
			}
			return args;
		}

		/**
		 * @return true if init(String[]) called but doRepeatCommand has not returned false
		 */
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

		/** @return true just after user typed 'r' */
		boolean buildFresh() {
			return buildFresh;
		}

		/** @return false if we should quit, true to do another command */
		boolean doRepeatCommand(ICommand command) {
			if (!running) {
				return false;
			}
			boolean result = false;
			if (quit) {
				result = false;
			} else if (incremental) {
				try {
					if (buildFresh) { // reset before input request
						buildFresh = false;
					}
					System.out.println(" press enter to recompile, r to rebuild, q to quit: ");
					System.out.flush();
					// boolean doMore = false;
					// seek for one q or a series of [\n\r]...
					do {
						int input = System.in.read();
						if ('q' == input) {
							break; // result = false;
						} else if ('r' == input) {
							buildFresh = true;
							result = true;
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
			// final Thread thread = Thread.currentThread();
			long targetTime = System.currentTimeMillis() + delay;
			// long curTime;
			while (targetTime > System.currentTimeMillis()) {
				if (quit) {
					return;
				}
				try {
					Thread.sleep(300);
				} // 1/3-second delta for quit check
				catch (InterruptedException e) {
				}
			}
		}
	}

}
