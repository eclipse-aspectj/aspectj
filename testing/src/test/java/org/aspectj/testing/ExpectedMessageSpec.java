/* *******************************************************************
 * Copyright (c) 2004 IBM Corporation
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *     Adrian Colyer,
 * ******************************************************************/
package org.aspectj.testing;

import org.aspectj.tools.ajc.AjcTestCase;

/**
 * @author colyer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ExpectedMessageSpec {

	private String kind = "error";
	private int line = -1;
	private int aspectLine = -1;
	private String text;
	private String file;
	private String aspectFile;
	private String details;

	public AjcTestCase.Message toMessage() {
		return new AjcTestCase.Message(line, file, text, null);
	}

	public AjcTestCase.Message toWeaveMessage() {
		return new AjcTestCase.Message(line, file, aspectLine, aspectFile, text, null);
	}

	/**
	 * @return Returns the details.
	 */
	public String getDetails() {
		return details;
	}
	/**
	 * @param details The details to set.
	 */
	public void setDetails(String details) {
		this.details = details;
	}
	/**
	 * @return Returns the file.
	 */
	public String getFile() {
		return file;
	}
	/**
	 * @param file The file to set.
	 */
	public void setFile(String file) {
		this.file = file;
	}
	/**
	 * @return Returns the aspect file.
	 */
	public String getAspectFile() {
		return aspectFile;
	}
	/**
	 * @param aspectFile The aspect file to set.
	 */
	public void setAspectFile(String aspectFile) {
		this.aspectFile = aspectFile;
	}
	/**
	 * @return Returns the kind.
	 */
	public String getKind() {
		return kind;
	}
	/**
	 * @param kind The kind to set.
	 */
	public void setKind(String kind) {
		this.kind = kind;
	}
	/**
	 * @return Returns the line.
	 */
	public int getLine() {
		return line;
	}
	/**
	 * @param line The line to set.
	 */
	public void setLine(int line) {
		this.line = line;
	}
	/**
	 * @return Returns the asperct line.
	 */
	public int getAspectLine() {
		return aspectLine;
	}
	/**
	 * @param aspectLine The aspect line to set.
	 */
	public void setAspectLine(int aspectLine) {
		this.aspectLine = aspectLine;
	}
	/**
	 * @return Returns the text.
	 */
	public String getText() {
		return text;
	}
	/**
	 * @param text The text to set.
	 */
	public void setText(String text) {
		this.text = text;
	}
}
