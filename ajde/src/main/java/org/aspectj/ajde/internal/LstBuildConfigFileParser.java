/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/

package org.aspectj.ajde.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.aspectj.ajdt.ajc.ConfigParser;

/**
 * @author Mik Kersten
 */
public class LstBuildConfigFileParser extends ConfigParser {

	private List<File> importedFiles = new ArrayList<>();
	private List<String> problemEntries = new ArrayList<>();

	// private String currFilePath;

	public LstBuildConfigFileParser(String currFilePath) {
		// this.currFilePath = currFilePath;
	}

	protected void showWarning(String message) {
		problemEntries.add(message);
	}

	protected void parseImportedConfigFile(String relativeFilePath) {
		importedFiles.add(makeFile(relativeFilePath));
		super.files.add(new File(relativeFilePath));
		super.parseImportedConfigFile(relativeFilePath);
	}

	protected void showError(String message) {
		problemEntries.add(message);
	}

	public List<File> getImportedFiles() {
		return importedFiles;
	}

	public List<String> getProblemEntries() {
		return problemEntries;
	}
}
