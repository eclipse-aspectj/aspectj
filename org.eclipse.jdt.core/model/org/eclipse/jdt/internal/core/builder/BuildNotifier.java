/*******************************************************************************
 * Copyright (c) 2000, 2001, 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Palo Alto Research Center, Incorporated - AspectJ adaptation
 ******************************************************************************/
package org.eclipse.jdt.internal.core.builder;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;
import org.eclipse.jdt.internal.core.Util;

// AspectJ - increased member visibilities
public class BuildNotifier {

protected IProgressMonitor monitor;
protected int rootPathLength;
protected boolean cancelling;
protected float percentComplete;
protected float progressPerCompilationUnit;
protected int newErrorCount;
protected int fixedErrorCount;
protected int newWarningCount;
protected int fixedWarningCount;
protected int workDone;
protected int totalWork;
protected String previousSubtask;

public BuildNotifier(IProgressMonitor monitor, IProject project) {
	this.monitor = monitor;
	try {
		IPath location = project.getDescription().getLocation();
		if (location == null)
			location = project.getParent().getLocation(); // default workspace location
		else if (project.getName().equalsIgnoreCase(location.lastSegment()))
			location = location.removeLastSegments(1); // want to show project name if possible
		this.rootPathLength = location.addTrailingSeparator().toString().length();
	} catch(CoreException e) {
		this.rootPathLength = 0;
	}
	this.cancelling = false;
	this.newErrorCount = 0;
	this.fixedErrorCount = 0;
	this.newWarningCount = 0;
	this.fixedWarningCount = 0;
	this.workDone = 0;
	this.totalWork = 1000000;
}

/**
 * Notification before a compile that a unit is about to be compiled.
 */
public void aboutToCompile(ICompilationUnit unit) {
	String message = new String(unit.getFileName());
	message = message.replace('\\', '/');
	int end = message.lastIndexOf('/');
	message = Util.bind("build.compiling", //$NON-NLS-1$
		message.substring(rootPathLength, end <= rootPathLength ? message.length() : end));
	subTask(message);
}

public void begin() {
	if (monitor != null)
		monitor.beginTask("", totalWork); //$NON-NLS-1$
	this.previousSubtask = null;
}

/**
 * Check whether the build has been canceled.
 */
public void checkCancel() {
	if (monitor != null && monitor.isCanceled())
		throw new OperationCanceledException();
}

/**
 * Check whether the build has been canceled.
 * Must use this call instead of checkCancel() when within the compiler.
 */
public void checkCancelWithinCompiler() {
	if (monitor != null && monitor.isCanceled() && !cancelling) {
		// Once the compiler has been canceled, don't check again.
		setCancelling(true);
		// Only AbortCompilation can stop the compiler cleanly.
		// We check cancelation again following the call to compile.
		throw new AbortCompilation(true, null); 
	}
}

/**
 * Notification while within a compile that a unit has finished being compiled.
 */
public void compiled(ICompilationUnit unit) {
	String message = new String(unit.getFileName());
	message = message.replace('\\', '/');
	int end = message.lastIndexOf('/');
	message = Util.bind("build.compiling", //$NON-NLS-1$
		message.substring(rootPathLength, end <= rootPathLength ? message.length() : end));
	subTask(message);
	updateProgressDelta(progressPerCompilationUnit);
	checkCancelWithinCompiler();
}

public void done() {
	updateProgress(1.0f);
	subTask(Util.bind("build.done")); //$NON-NLS-1$
	if (monitor != null)
		monitor.done();
	this.previousSubtask = null;
}

/**
 * Returns a string describing the problems.
 */
protected String problemsMessage() {
	int numNew = newErrorCount + newWarningCount;
	int numFixed = fixedErrorCount + fixedWarningCount;
	if (numNew == 0 && numFixed == 0) return ""; //$NON-NLS-1$
	if (numFixed == 0)
		return '(' + (numNew == 1
			? Util.bind("build.oneProblemFound", String.valueOf(numNew)) //$NON-NLS-1$
			: Util.bind("build.problemsFound", String.valueOf(numNew))) + ')'; //$NON-NLS-1$
	if (numNew == 0)
		return '(' + (numFixed == 1
			? Util.bind("build.oneProblemFixed", String.valueOf(numFixed)) //$NON-NLS-1$
			: Util.bind("build.problemsFixed", String.valueOf(numFixed))) + ')'; //$NON-NLS-1$
	return
		'(' + (numFixed == 1
			? Util.bind("build.oneProblemFixed", String.valueOf(numFixed)) //$NON-NLS-1$
			: Util.bind("build.problemsFixed", String.valueOf(numFixed))) //$NON-NLS-1$
		+ ", " //$NON-NLS-1$
		+ (numNew == 1
			? Util.bind("build.oneProblemFound", String.valueOf(numNew)) //$NON-NLS-1$
			: Util.bind("build.problemsFound", String.valueOf(numNew))) + ')'; //$NON-NLS-1$
}

/**
 * Sets the cancelling flag, which indicates we are in the middle
 * of being cancelled.  Certain places (those callable indirectly from the compiler)
 * should not check cancel again while this is true, to avoid OperationCanceledException
 * being thrown at an inopportune time.
 */
public void setCancelling(boolean cancelling) {
	this.cancelling = cancelling;
}

/**
 * Sets the amount of progress to report for compiling each compilation unit.
 */
public void setProgressPerCompilationUnit(float progress) {
	this.progressPerCompilationUnit = progress;
}

public void subTask(String message) {
	String pm = problemsMessage();
	String msg = pm.length() == 0 ? message : pm + " " + message; //$NON-NLS-1$

	if (msg.equals(this.previousSubtask)) return; // avoid refreshing with same one
	//if (JavaBuilder.DEBUG) System.out.println(msg);
	if (monitor != null)
		monitor.subTask(msg);

	this.previousSubtask = msg;
}

public void updateProblemCounts(IProblem[] newProblems) {
	for (int i = 0, newSize = newProblems.length; i < newSize; ++i)
		if (newProblems[i].isError()) newErrorCount++; else newWarningCount++;
}

/**
 * Update the problem counts from one compilation result given the old and new problems,
 * either of which may be null.
 */
protected void updateProblemCounts(IMarker[] oldProblems, IProblem[] newProblems) {
	if (newProblems != null) {
		next : for (int i = 0, newSize = newProblems.length; i < newSize; ++i) {
			IProblem newProblem = newProblems[i];
			boolean isError = newProblem.isError();
			String message = newProblem.getMessage();

			if (oldProblems != null) {
				for (int j = 0, oldSize = oldProblems.length; j < oldSize; ++j) {
					IMarker pb = oldProblems[j];
					if (pb == null) continue; // already matched up with a new problem
					boolean wasError = IMarker.SEVERITY_ERROR
						== pb.getAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
					if (isError == wasError && message.equals(pb.getAttribute(IMarker.MESSAGE, ""))) { //$NON-NLS-1$
						oldProblems[j] = null;
						continue next;
					}
				}
			}
			if (isError) newErrorCount++; else newWarningCount++;
		}
	}
	if (oldProblems != null) {
		next : for (int i = 0, oldSize = oldProblems.length; i < oldSize; ++i) {
			IMarker oldProblem = oldProblems[i];
			if (oldProblem == null) continue next; // already matched up with a new problem
			boolean wasError = IMarker.SEVERITY_ERROR
				== oldProblem.getAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
			String message = oldProblem.getAttribute(IMarker.MESSAGE, ""); //$NON-NLS-1$

			if (newProblems != null) {
				for (int j = 0, newSize = newProblems.length; j < newSize; ++j) {
					IProblem pb = newProblems[j];
					if (wasError == pb.isError() && message.equals(pb.getMessage()))
						continue next;
				}
			}
			if (wasError) fixedErrorCount++; else fixedWarningCount++;
		}
	}
}

public void updateProgress(float percentComplete) {
	if (percentComplete > this.percentComplete) {
		this.percentComplete = Math.min(percentComplete, 1.0f);
		int work = Math.round(this.percentComplete * this.totalWork);
		if (work > this.workDone) {
			if (monitor != null)
				monitor.worked(work - this.workDone);
			//if (JavaBuilder.DEBUG)
				//System.out.println(java.text.NumberFormat.getPercentInstance().format(this.percentComplete));
			this.workDone = work;
		}
	}
}

public void updateProgressDelta(float percentWorked) {
	updateProgress(percentComplete + percentWorked);
}

public boolean anyErrors() {
	return newErrorCount > 0;
}
}