/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Xerox/PARC     initial implementation
 * ******************************************************************/

package org.aspectj.testing.harness.bridge;

import java.io.File;
import java.util.*;

import org.aspectj.bridge.AbortException;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessageHandler;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.util.FileUtil;
import org.aspectj.util.LangUtil;

/**
 * Check input and implement defaults.
 * This handles failure messaging and collecting temp directories
 * for later cleanup.
 * The default behavior is to send a fail message to the message
 * handler.  Clients may instead:
 * <li>Toggle abortOnException to throw AbortException on failure</li>
 * <li>push their own message handler to redirect messages
 *     (pop to remove if any pushed)</li>
 * <li>subclass this to reimplement <code>fail(String)</code>
 * <p>
 * A component given this to do validation should
 * not change the reporting scheme established by the caller,
 * so the caller may lock and unlock the error handling policy.
 * When the policy is locked, this silently ignores attempts
 * to toggle exceptions, or delete temporary files.
 * XXX callers cannot prevent others from pushing other error handlers.
 */
public class Validator {

	/** stack of handlers */
	private final Stack<IMessageHandler> handlers;

	/** list of File registered for deletion on demand */
	private final List<File> tempFiles; // deleteTempFiles requires ListIterator.remove()

	/** list of Sandboxes registered for cleanup on demand  */
	private final List<Sandbox> sandboxes;

	/** if true, throw AbortException on failure */
	boolean abortOnFailure;

	/** this object prevents any changes to error-handling policy */
	private Object locker;

	public Validator(IMessageHandler handler) {
		tempFiles = new ArrayList<>();
		sandboxes = new ArrayList<>();
		handlers = new Stack<>();
		pushHandler(handler);
	}

	/**
	 * Push IMessageHandler onto stack,
	 * so it will be used until the next push or pop
	 * @param handler not null
	 *
	 */
	public void pushHandler(IMessageHandler handler) {
		LangUtil.throwIaxIfNull(handler, "handler");
		handlers.push(handler);
	}

	/** @throws IllegalStateException if handler is not on top */
	public void popHandler(IMessageHandler handler) {
		LangUtil.throwIaxIfNull(handler, "handler");
		if (handler != handlers.peek()) {
			throw new IllegalStateException("not current handler");
		}
		handlers.pop();
	}

	/** @return true if this requestor now has locked the error handling policy */
	public boolean lock(Object requestor) {
		if (null == locker) {
			locker = requestor;
		}
		return (locker == requestor);
	}

	/** @return true if the error handling policy is now unlocked  */
	public boolean unlock(Object requestor) {
		if (requestor == locker) {
			locker = null;
		}
		return (locker == null);
	}

	public void setAbortOnFailure(boolean abortOnFailure) {
		if (null == locker) {
			if (this.abortOnFailure != abortOnFailure) {
				this.abortOnFailure = abortOnFailure;
			}
		}
	}

	/**
	 * May fail with any of the messages
	 * <li>{null check} array<li>
	 * <li>{null check} {message}[#}<li>
	 */
	public boolean nullcheck(Object[] ra, String message) {
		return ((nullcheck((Object) ra, message + " array"))
				&& nullcheck(Arrays.asList(ra), message));
	}

	/**
	 * Like nullcheck(Collection, message), except adding lower and upper bound
	 * @param atLeast fail if list size is smaller than this
	 * @param atMost  fail if list size is greater than this
	 */
	public boolean nullcheck(Collection list, int atLeast, int atMost, String message) {
		if (nullcheck(list, message)) {
			int size = list.size();
			if (size < atLeast) {
				fail(message + ": " + size + "<" + atLeast);
			} else if (size > atMost) {
				fail(message + ": " + size + ">" + atMost);
			} else {
				return true;
			}
		}
		return false;
	}

	/**
	 * May fail with any of the messages
	 * <li>{null check} list<li>
	 * <li>{null check} {message}[#}<li>
	 */
	public boolean nullcheck(Collection list, String message) {
		if (nullcheck((Object) list, message + " list")) {
			int i = 0;
			for (Object o : list) {
				if (!nullcheck(o, message + "[" + i++ + "]")) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * May fail with the message "null {message}"
	 * if o and the def{ault} are null
	 * @return o if not null or default otherwise
	 */
	public Object nulldefault(Object o, String message, Object def) {
		if (null == o) {
			o = def;
		}
		nullcheck(o, message);
		return o;
	}

	/** may fail with the message "null {message}" */
	public boolean nullcheck(Object o, String message) {
		if (null == o) {
			if (null == message) message = "object";
			fail("null " + message);
			return false;
		}
		return true;
	}

	/**
	 * Verify that all paths are readable relative to baseDir.
	 * may fail with the message "cannot read {file}"
	 */
	public boolean canRead(File baseDir, String[] paths, String message) {
		if (!canRead(baseDir, "baseDir - " + message)
				|| !nullcheck(paths, "paths - " + message)) {
			return false;
		}
		final String dirPath = baseDir.getPath();
		File[] files = FileUtil.getBaseDirFiles(baseDir, paths);
		for (File file : files) {
			if (!canRead(file, "{" + dirPath + "} " + file)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Verify that all paths are readable relative to baseDir.
	 * may fail with the message "cannot read {file}"
	 */
	public boolean canRead(File[] files, String message) {
		if (!nullcheck(files, message)) {
			return false;
		}
		for (File file : files) {
			if (!canRead(file, file.getPath())) {
				return false;
			}
		}
		return true;
	}

	/** may fail with the message "cannot read {file}" */
	public boolean canRead(File file, String message) {
		if (nullcheck(file, message)) {
			if (file.canRead()) {
				return true;
			} else {
				fail("cannot read " + file);
			}
		}
		return false;
	}

	/** may fail with the message "cannot write {file}" */
	public boolean canWrite(File file, String message) {
		if (nullcheck(file, message)) {
			if (file.canRead()) {
				return true;
			} else {
				fail("cannot write " + file);
			}
		}
		return false;
	}

	/** may fail with the message "not a directory {file}" */
	public boolean canReadDir(File file, String message) {
		if (canRead(file, message)) {
			if (file.isDirectory()) {
				return true;
			} else {
				fail("not a directory " + file);
			}
		}
		return false;
	}

	/** may fail with the message "not a directory {file}" */
	public boolean canWriteDir(File file, String message) {
		if (canWrite(file, message)) {
			if (file.isDirectory()) {
				return true;
			} else {
				fail("not a directory " + file);
			}
		}
		return false;
	}

	/**
	 * May fail with any of the messages
	 * <li>{null check} dir array<li>
	 * <li>"#: not a File {file}"<li>
	 * <li>{canRead} {message}[#}<li>
	 */
	public boolean canReadFiles(Object[] dirs, String message) {
		return ((nullcheck((Object) dirs, message + " dir array"))
				&& canReadFiles(Arrays.asList(dirs), message));
	}

	/**
	 * May fail with any of the messages
	 * <li>{null check} files<li>
	 * <li>"#: not a File {file}"<li>
	 * <li>{canRead} {message}[#}<li>
	 */
	public boolean canReadFiles(Collection dirs, String message) {
		if (nullcheck((Object) dirs, message + " files")) {
			int i = 0;
			for (Object o : dirs) {
				if (!(o instanceof File)) {
					fail(i + ": not a file " + o);
				}
				if (!canRead((File) o, message + "[" + i++ + "]")) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
	/**
	 * May fail with any of the messages
	 * <li>{null check} dir array<li>
	 * <li>"#: not a File {file}"<li>
	 * <li>{canReadDir} {message}[#}<li>
	 */
	public boolean canReadDirs(Object[] dirs, String message) {
		return ((nullcheck((Object) dirs, message + " dir array"))
				&& canReadDirs(Arrays.asList(dirs), message));
	}

	/**
	 * May fail with any of the messages
	 * <li>{null check} dirs<li>
	 * <li>"#: not a File {file}"<li>
	 * <li>{canReadDir} {message}[#}<li>
	 */
	public boolean canReadDirs(Collection dirs, String message) {
		if (nullcheck((Object) dirs, message + " dirs")) {
			int i = 0;
			for (Object o : dirs) {
				if (!(o instanceof File)) {
					fail(i + ": not a file " + o);
				}
				if (!canReadDir((File) o, message + "[" + i++ + "]")) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * May fail with any of the messages
	 * <li>{null check} dir array<li>
	 * <li>"#: not a File {file}"<li>
	 * <li>{canWrite} {message}[#}<li>
	 */
	public boolean canWriteFiles(Object[] dirs, String message) {
		return ((nullcheck((Object) dirs, message + " dir array"))
				&& canWriteFiles(Arrays.asList(dirs), message));
	}

	/**
	 * May fail with any of the messages
	 * <li>{null check} files<li>
	 * <li>"#: not a File {file}"<li>
	 * <li>{canWrite} {message}[#}<li>
	 */
	public boolean canWriteFiles(Collection dirs, String message) {
		if (nullcheck((Object) dirs, message + " files")) {
			int i = 0;
			for (Object o : dirs) {
				if (!(o instanceof File)) {
					fail(i + ": not a file " + o);
				}
				if (!canWrite((File) o, message + "[" + i++ + "]")) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * May fail with any of the messages
	 * <li>{null check} dir array<li>
	 * <li>"#: not a File {file}"<li>
	 * <li>{canWriteDir} {message}[#}<li>
	 */
	public boolean canWriteDirs(Object[] dirs, String message) {
		return ((nullcheck((Object) dirs, message + " dir array"))
				&& canWriteDirs(Arrays.asList(dirs), message));
	}

	/**
	 * May fail with any of the messages
	 * <li>{null check} dirs<li>
	 * <li>"#: not a File {file}"<li>
	 * <li>{canWriteDir} {message}[#}<li>
	 */
	public boolean canWriteDirs(Collection dirs, String message) {
		if (nullcheck((Object) dirs, message + " dirs")) {
			int i = 0;
			for (Object o : dirs) {
				if (!(o instanceof File)) {
					fail(i + ": not a file " + o);
				}
				if (!canWriteDir((File) o, message + "[" + i++ + "]")) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * Send an info message to any underlying handler
	 * @param message ignored if null
	 */
	public void info(String message) {
		if (null != message) {
			IMessageHandler handler = getHandler();
			MessageUtil.info(handler, message);
		}
	}

	/** Fail via message or AbortException */
	public void fail(String message) {
		fail(message, (Throwable) null);
	}

	/**
	 * Fail via message or AbortException.
	 * All failure messages go through here,
	 * so subclasses may override to control
	 * failure-handling.
	 */
	public void fail(String message, Throwable thrown) {
		if ((null == message) && (null == thrown)) {
			message = "<Validator:no message>";
		}
		IMessage m = MessageUtil.fail(message, thrown);
		if (abortOnFailure) {
			throw new AbortException(m);
		} else {
			IMessageHandler handler = getHandler();
			handler.handleMessage(m);
		}
	}

	/**
	 * Register a file temporary, i.e., to be
	 * deleted on completion.  The file need not
	 * exist (yet or ever) and may be a duplicate
	 * of existing files registered.
	 */
	public void registerTempFile(File file) {
		if (null != file) {
			tempFiles.add(file);
		}
	}

	/**
	 * Get a writable {possibly-empty} directory.
	 * If the input dir is null, then try to create a temporary
	 * directory using name.
	 * If the input dir is not null, this tries to
	 * create it if it does not exist.
	 * Then if name is not null,
	 * it tries to get a temporary directory
	 * under this using name.
	 * If name is null, "Validator" is used.
	 * If deleteContents is true, this will try to delete
	 * any existing contents.  If this is unable to delete
	 * the contents of the input directory, this may return
	 * a new, empty temporary directory.
	 * If register is true, then any directory returned is
	 * saved for later deletion using <code>deleteTempDirs()</code>,
	 * including the input directory.
	 * When this is unable to create a result, if failMessage
	 * is not null then this will fail; otherwise it returns false;
	 */
	public File getWritableDir(File dir, String name,
			boolean deleteContents, boolean register, String failMessage) {
		// check dir
		if (null == dir) {
			if (null == name) {
				name = "Validator";
			}
			dir = FileUtil.getTempDir(name);
		} else {
			if (!dir.exists()) {
				dir.mkdirs();
			}
		}
		// fail if necessary
		if ((null == dir) || (!dir.exists())) {
			if (null != failMessage) {
				fail(failMessage + ": unable to get parent " + dir);
			}
		} else {
			FileUtil.makeNewChildDir(dir, name);
			if (deleteContents) {
				FileUtil.deleteContents(dir);
			}
			if (register) {
				tempFiles.add(dir);
			}
		}
		return dir;
	}

	/**
	 * Delete any temp sandboxes, files or directories saved,
	 * optionally reporting failures in the normal way.
	 * This will be ignored unless the failure policy
	 * is unlocked.
	 */
	public void deleteTempFiles(boolean reportFailures) {
		if (null == locker) {
			for (ListIterator iter = tempFiles.listIterator(); iter.hasNext();) {
				if (deleteFile((File) iter.next(), reportFailures)) {
					iter.remove();
				}
			}
			for (ListIterator iter = sandboxes.listIterator(); iter.hasNext();) {
				Sandbox sandbox = (Sandbox) iter.next();
				// XXX assumes all dirs are in sandboxDir
				if (deleteFile(sandbox.sandboxDir, reportFailures)) {
					iter.remove();
				}
			}
		}
	}

	/**
	 * Manage temp files and directories of registered sandboxes.
	 * Note that a sandbox may register before it is initialized,
	 * so this must do nothing other than save the reference.
	 * @param sandbox the uninitialized Sandbox to track
	 */
	public void registerSandbox(Sandbox sandbox) {
		sandboxes.add(sandbox);
	}


	private boolean deleteFile(File file, boolean reportFailures) {
		if (null == file) {
			if (reportFailures) {
				fail("unable to delete null file");
			}
			return true; // null file - skip
		}
		FileUtil.deleteContents(file);
		if (file.exists()) {
			file.delete();
		}
		if (!file.exists()) {
			return true;
		} else if (reportFailures) {
			fail("unable to delete " + file);
		}
		return false;
	}

	/** @throws IllegalStateException if handler is null */
	private IMessageHandler getHandler() {
		IMessageHandler handler = handlers.peek();
		if (null == handler) {
			throw new IllegalStateException("no handler");
		}
		return handler;
	}

} // class Validator

