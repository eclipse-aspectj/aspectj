/*******************************************************************************
 * Copyright (c) 2000, 2001, 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.internal.compiler.problem;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.IProblemFactory;
import org.eclipse.jdt.internal.compiler.util.CharOperation;

public class DefaultProblemFactory implements IProblemFactory {


	public String[] messageTemplates;
	private Locale locale;
	private static String[] DEFAULT_LOCALE_TEMPLATES;
	private final static char[] DOUBLE_QUOTES = "''".toCharArray(); //$NON-NLS-1$
	private final static char[] SINGLE_QUOTE = "'".toCharArray(); //$NON-NLS-1$

/**
 * @param loc the locale used to get the right message
 */
public DefaultProblemFactory(Locale loc) {
	this.locale = loc;
	if (Locale.getDefault().equals(loc)){
		if (DEFAULT_LOCALE_TEMPLATES == null){
			DEFAULT_LOCALE_TEMPLATES = loadMessageTemplates(loc);
		}
		this.messageTemplates = DEFAULT_LOCALE_TEMPLATES;
	} else {
		this.messageTemplates = loadMessageTemplates(loc);
	}
}
/**
 * Answer a new IProblem created according to the parameters value
 * <ul>
 * <li>originatingFileName the name of the file name from which the problem is originated
 * <li>problemId the problem id
 * <li>arguments the arguments needed to set the error message
 * <li>severity the severity of the problem
 * <li>startPosition the starting position of the problem
 * <li>endPosition the end position of the problem
 * <li>lineNumber the line on which the problem occured
 * </ul>
 * @param originatingFileName char[]
 * @param problemId int
 * @param arguments String[]
 * @param severity int
 * @param startPosition int
 * @param endPosition int
 * @param lineNumber int
 * @return org.eclipse.jdt.internal.compiler.IProblem
 */
public IProblem createProblem(
	char[] originatingFileName, 
	int problemId, 
	String[] arguments, 
	int severity, 
	int startPosition, 
	int endPosition, 
	int lineNumber) {

	return new DefaultProblem(
		originatingFileName, 
		this.getLocalizedMessage(problemId, arguments),
		problemId, 
		arguments, 
		severity, 
		startPosition, 
		endPosition, 
		lineNumber); 
}
/**
 * Answer the locale used to retrieve the error messages
 * @return java.util.Locale
 */
public Locale getLocale() {
	return locale;
}
public final String getLocalizedMessage(int id, String[] problemArguments) {
	StringBuffer output = new StringBuffer(80);
	String message = 
		messageTemplates[(id & IProblem.IgnoreCategoriesMask)]; 
	if (message == null) {
		return "Unable to retrieve the error message for problem id: " //$NON-NLS-1$
			+ id
			+ ". Check compiler resources.";  //$NON-NLS-1$
	}

	// for compatibility with MessageFormat which eliminates double quotes in original message
	char[] messageWithNoDoubleQuotes =
		CharOperation.replace(message.toCharArray(), DOUBLE_QUOTES, SINGLE_QUOTE);
	message = new String(messageWithNoDoubleQuotes);

	int length = message.length();
	int start = -1, end = length;
	while (true) {
		if ((end = message.indexOf('{', start)) > -1) {
			output.append(message.substring(start + 1, end));
			if ((start = message.indexOf('}', end)) > -1) {
				try {
					output.append(
						problemArguments[Integer.parseInt(message.substring(end + 1, start))]); 
				} catch (NumberFormatException nfe) {
					output.append(message.substring(end + 1, start + 1));
				} catch (ArrayIndexOutOfBoundsException e) {
					return "Corrupted compiler resources for problem id: " //$NON-NLS-1$
						+ (id & IProblem.IgnoreCategoriesMask)
						+ ". Check compiler resources.";  //$NON-NLS-1$
				}
			} else {
				output.append(message.substring(end, length));
				break;
			}
		} else {
			output.append(message.substring(start + 1, length));
			break;
		}
	}
	return output.toString();
}
/**
 * @param problem org.eclipse.jdt.internal.compiler.IProblem
 * @return String
 */
public final String localizedMessage(IProblem problem) {
	return getLocalizedMessage(problem.getID(), problem.getArguments());
}

/**
 * This method initializes the MessageTemplates class variable according
 * to the current Locale.
 */
public static String[] loadMessageTemplates(Locale loc) {
	ResourceBundle bundle = ResourceBundle.getBundle("org.eclipse.jdt.internal.compiler.problem.messages", loc); //$NON-NLS-1$
	String[] templates = new String[500];
	for (int i = 0, max = templates.length; i < max; i++) {
		try {
			templates[i] = bundle.getString(String.valueOf(i));
		} catch (MissingResourceException e) {
			// available ID
		}
	}
	return templates;
}

public DefaultProblemFactory() {
	this(Locale.getDefault());
}
}
