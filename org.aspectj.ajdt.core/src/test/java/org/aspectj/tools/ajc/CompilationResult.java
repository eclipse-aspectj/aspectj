/* *******************************************************************
 * Copyright (c) 2004 IBM Corporation
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Adrian Colyer,
 * ******************************************************************/
package org.aspectj.tools.ajc;

import java.util.Collections;
import java.util.List;

import org.aspectj.bridge.IMessage;

/**
 * Utility class that makes the results of a compiler run available.
 * <p>
 * Instances of this class are returned by the Ajc.compile() and
 * doIncrementalCompile() methods (and the AjcTestCase.ajc() wrapper).
 * </p>
 * <p>
 * This class provides a useful toString() method that is very helpful when
 * debugging or creating messages for assert statements.
 * </p>
 * <p>Note that the stdOut and stdErr captured from the compiler run do
 * not contain any rendered messages - these are in the messages lists
 * instead. Therefore for many compiler runs, they will be empty.
 * </p>
 */
public class CompilationResult {

	private String[] args;
	private String stdOut;
	private String stdErr;
	private List<IMessage> infoMessages;
	private List<IMessage>  errorMessages;
	private List<IMessage>  warningMessages;
	private List<IMessage>  failMessages;
	private List<IMessage> weaveMessages;

	/**
	 * Build a compilation result - called by the Ajc.compile and
	 * Ajc.doIncrementalCompile methods. Should be no need for you
	 * to construct an instance yourself.
	 */
	protected CompilationResult(
			String[] args,
			String stdOut,
			String stdErr,
			List<IMessage> infoMessages,
			List<IMessage> errorMessages,
			List<IMessage> warningMessages,
			List<IMessage> failMessages,
			List<IMessage> weaveMessages) {
		this.args = args;
		this.stdOut = stdOut;
		this.stdErr = stdErr;
		this.infoMessages = (infoMessages == null) ? Collections.<IMessage>emptyList() : infoMessages;
		this.errorMessages = (errorMessages == null) ? Collections.<IMessage>emptyList() : errorMessages;
		this.warningMessages = (warningMessages == null) ? Collections.<IMessage>emptyList() : warningMessages;
		this.failMessages = (failMessages == null) ? Collections.<IMessage>emptyList() : failMessages;
		this.weaveMessages = (weaveMessages == null) ? Collections.<IMessage>emptyList() : weaveMessages;
	}

	/**
	 * The arguments that were passed to the compiler.
	 */
	public String[] getArgs() { return args; }
	/**
	 * The standard output written by the compiler, excluding any messages.
	 */
	public String getStandardOutput() { return stdOut; }
	/**
	 * The standard error written by the compiler, excluding any messages.
	 */
	public String getStandardError() { return stdErr; }

	/**
	 * True if the compiler issued any messages of any kind.
	 */
	public boolean hasMessages() { return (hasInfoMessages() || hasErrorMessages() || hasWarningMessages() || hasFailMessages() || hasWeaveMessages()); }
	/**
	 * True if the compiler issued one or more informational messages.
	 */
	public boolean hasInfoMessages() { return !infoMessages.isEmpty(); }
	/**
	 * True if the compiler issued one or more error messages.
	 */
	public boolean hasErrorMessages() { return !errorMessages.isEmpty(); }
	/**
	 * True if the compiler issued one or more warning messages.
	 */
	public boolean hasWarningMessages() { return !warningMessages.isEmpty(); }
	/**
	 * True if the compiler issued one or more fail or abort messages.
	 */
	public boolean hasFailMessages() { return !failMessages.isEmpty(); }
	/**
	 * True if the compiler issued one or more weave info messages.
	 */
	public boolean hasWeaveMessages() { return !weaveMessages.isEmpty(); }

	/**
	 * The informational messages produced by the compiler. The list
	 * entries are the <code>IMessage</code> objects created during the
	 * compile - so that you can programmatically test source locations
	 * etc. etc.. It may often be easier to use the <code>assertMessages</code>
	 * helper methods defined in the AjcTestCase class to test for messages
	 * though.
	 * @see org.aspectj.tools.ajc.AjcTestCase
	 */
	public List<IMessage> getInfoMessages() { return infoMessages; }
	/**
	 * The error messages produced by the compiler. The list
	 * entries are the <code>IMessage</code> objects created during the
	 * compile - so that you can programmatically test source locations
	 * etc. etc.. It may often be easier to use the <code>assertMessages</code>
	 * helper methods defined in the AjcTestCase class to test for messages
	 * though.
	 * @see org.aspectj.tools.ajc.AjcTestCase
	 */
	public List<IMessage> getErrorMessages() { return errorMessages; }
	/**
	 * The warning messages produced by the compiler. The list
	 * entries are the <code>IMessage</code> objects created during the
	 * compile - so that you can programmatically test source locations
	 * etc. etc.. It may often be easier to use the <code>assertMessages</code>
	 * helper methods defined in the AjcTestCase class to test for messages
	 * though.
	 * @see org.aspectj.tools.ajc.AjcTestCase
	 */
	public List<IMessage> getWarningMessages() { return warningMessages; }
	/**
	 * The fail or abort messages produced by the compiler. The list
	 * entries are the <code>IMessage</code> objects created during the
	 * compile - so that you can programmatically test source locations
	 * etc. etc.. It may often be easier to use the <code>assertMessages</code>
	 * helper methods defined in the AjcTestCase class to test for messages
	 * though.
	 * @see org.aspectj.tools.ajc.AjcTestCase
	 */
	public List<IMessage>  getFailMessages() { return failMessages; }

	public List<IMessage> getWeaveMessages() { return weaveMessages; }

	/**
	 * Returns string containing message count summary, list of messages
	 * by type, and the actual ajc compilation command that was issued.
	 */
	@Override
	public String toString() {
		StringBuffer buff = new StringBuffer();
		buff.append("AspectJ Compilation Result:\n");
		int totalMessages = infoMessages.size() + warningMessages.size() + errorMessages.size() + failMessages.size() + weaveMessages.size();
		buff.append(totalMessages);
		buff.append(" messages");
		if (totalMessages > 0) {
			buff.append(" (");
			buff.append(infoMessages.size());
			buff.append(" info, ");
			buff.append(warningMessages.size());
			buff.append(" warning, ");
			buff.append(errorMessages.size());
			buff.append(" error, ");
			buff.append(failMessages.size());
			buff.append(" fail, )");
			buff.append(weaveMessages.size());
			buff.append(" weaveInfo");
		}
		buff.append("\n");
		int msgNo = 1;
		for (IMessage failMessage : failMessages) {
			buff.append("[fail ");
			buff.append(msgNo++);
			buff.append("] ");
			buff.append(failMessage.toString());
			buff.append("\n");
		}
		msgNo = 1;
		for (IMessage errorMessage : errorMessages) {
			buff.append("[error ");
			buff.append(msgNo++);
			buff.append("] ");
			buff.append(errorMessage.toString());
			buff.append("\n");
		}
		msgNo = 1;
		for (IMessage warningMessage : warningMessages) {
			buff.append("[warning ");
			buff.append(msgNo++);
			buff.append("] ");
			buff.append(warningMessage.toString());
			buff.append("\n");
		}
		msgNo = 1;
		for (IMessage infoMessage : infoMessages) {
			buff.append("[info ");
			buff.append(msgNo++);
			buff.append("] ");
			buff.append(infoMessage.toString());
			buff.append("\n");
		}
		msgNo = 1;
		for (IMessage weaveMessage : weaveMessages) {
			buff.append("[weaveInfo ");
			buff.append(msgNo++);
			buff.append("] ");
			buff.append(weaveMessage.toString());
			buff.append("\n");
		}
		buff.append("\ncommand was: 'ajc");
		for (String arg : args) {
			buff.append(' ');
			buff.append(arg);
		}
		buff.append("'\n");
		return buff.toString();
	}
}
