/********************************************************************
 * Copyright (c) 2007 Contributors. All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: IBM Corporation - initial API and implementation 
 * 				 Helen Hawkins   - initial version (bug 148190)
 *******************************************************************/
package org.aspectj.systemtest.incremental.tools;

import java.util.ArrayList;
import java.util.List;

import org.aspectj.ajde.core.IBuildMessageHandler;
import org.aspectj.bridge.AbortException;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessage.Kind;

/**
 * IMessageHandler which by default ignores INFO and WEAVEINFO messages. Records the warning, weaving, compiler errors and error
 * messages and provides methods to get them.
 */
public class MultiProjTestMessageHandler implements IBuildMessageHandler {

	private final boolean VERBOSE = false;

	private boolean receivedNonIncrementalBuildMessage = false;
	private boolean receivedBatchBuildMessage = false;

	private List<IMessage> errorMessages;
	private List<IMessage> warningMessages;
	private List<IMessage> weavingMessages;
	private List<String> compilerErrors;
	private List<Kind> ignoring;

	public MultiProjTestMessageHandler() {
		ignoring = new ArrayList<>();
		errorMessages = new ArrayList<>();
		warningMessages = new ArrayList<>();
		weavingMessages = new ArrayList<>();
		compilerErrors = new ArrayList<>();
		ignore(IMessage.INFO);
		ignore(IMessage.WEAVEINFO);
	}

	public boolean handleMessage(IMessage message) throws AbortException {
		IMessage.Kind kind = message.getKind();
		if (isIgnoring(kind)) {
			return true;
		}
		if (kind.equals(IMessage.ABORT) || message.getThrown() != null) {
			log("> AjCompiler error: " + message.getMessage() + ", " + message.getThrown() + ")"); //$NON-NLS-1$
			message.getThrown().printStackTrace();
			compilerErrors.add(message + ", (" + message.getThrown() + ")");
			if (VERBOSE && (message.getThrown() != null)) {
				message.getThrown().printStackTrace();
			}
			return true;
		}
		if (message.getKind() == IMessage.ERROR) {
			errorMessages.add(message);
		}
		if (message.getKind() == IMessage.WARNING) {
			warningMessages.add(message);
		}
		if (message.getKind() == IMessage.WEAVEINFO) {
			weavingMessages.add(message);
		}
		log("IMessageHandler.handleMessage(" + message + ")");
		return true;
	}

	public void dontIgnore(Kind kind) {
		if (null != kind) {
			ignoring.remove(kind);
		}
	}

	public boolean isIgnoring(Kind kind) {
		return ((null != kind) && (ignoring.contains(kind)));
	}

	public void ignore(Kind kind) {
		if ((null != kind) && (!ignoring.contains(kind))) {
			ignoring.add(kind);
		}
	}

	public boolean hasWarning() {
		return !warningMessages.isEmpty();
	}

	public boolean hasErrorMessages() {
		return !errorMessages.isEmpty();
	}

	public boolean hasCompilerErrorMessages() {
		return !compilerErrors.isEmpty();
	}

	public List<IMessage> getErrorMessages() {
		return errorMessages;
	}

	public List<IMessage> getWarningMessages() {
		return warningMessages;
	}

	public List<IMessage> getWeavingMessages() {
		return weavingMessages;
	}

	public List<String> getCompilerErrors() {
		return compilerErrors;
	}

	public void log(String s) {
		if (VERBOSE) {
			System.out.println(s);
		}
	}

	public void reset() {
		receivedNonIncrementalBuildMessage = false;
		receivedBatchBuildMessage = false;
		errorMessages.clear();
		warningMessages.clear();
		weavingMessages.clear();
		compilerErrors.clear();
	}
}
