/********************************************************************
 * Copyright (c) 2007 Contributors. All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://eclipse.org/legal/epl-v10.html
 *
 * Contributors: IBM Corporation - initial API and implementation
 * 				 Helen Hawkins   - initial version
 *******************************************************************/
package org.aspectj.ajde.core;

import java.util.ArrayList;
import java.util.List;

import org.aspectj.bridge.AbortException;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessage.Kind;

/**
 * Test implementation of IBuildMessageHandler. By default it ignores INFO and WEAVEINFO messages. Stores all messages it's not
 * ignoring in an ArrayList and ERRORS and ABORTS also in a separate ArrayList enabling users to query whether anything went wrong
 * with the build.
 */
public class TestMessageHandler implements IBuildMessageHandler {

	private List<Kind> ignoring;
	private List<TestMessage> messages;
	private List<TestMessage> errors;

	public TestMessageHandler() {
		ignoring = new ArrayList<>();
		messages = new ArrayList<>();
		errors = new ArrayList<>();
		ignore(IMessage.INFO);
		ignore(IMessage.WEAVEINFO);
	}

	public boolean handleMessage(IMessage message) throws AbortException {
		IMessage.Kind kind = message.getKind();
		if (isIgnoring(kind)) {
			return true;
		}
		TestMessage t = new TestMessage(message);
		messages.add(t);
		if (kind.equals(IMessage.ABORT) || message.getThrown() != null) {
			System.err.println("> AjCompiler error: " + message.getMessage()); //$NON-NLS-1$
			message.getThrown().printStackTrace();
			errors.add(t);
		} else if (kind.equals(IMessage.ERROR)) {
			errors.add(t);
		}
		if (AjdeCoreModuleTests.verbose) {
			System.out.println("> " + message); //$NON-NLS-1$
		}
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

	public List<TestMessage> getMessages() {
		return messages;
	}

	public List<TestMessage> getErrors() {
		return errors;
	}

	public static class TestMessage {
		IMessage message;

		public TestMessage(IMessage m) {
			message = m;
		}

		public IMessage getContainedMessage() {
			return message;
		}

		public String toString() {
			String loc = "<no location>";
			if (null != message.getSourceLocation()) {
				loc = message.getSourceLocation().getSourceFile() + ":" + message.getSourceLocation().getLine();
			}
			return "TestMessage [" + message.getMessage() + ", " + loc + ", " + message.getKind() + "]";
		}
	}
}
