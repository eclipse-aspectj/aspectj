/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation,
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *     Xerox/PARC     initial implementation
 * ******************************************************************/

package org.aspectj.ajde.internal;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.aspectj.ajdt.ajc.ConfigParser;

/**
 * @author Mik Kersten
 */
public class LstBuildConfigFileParser extends ConfigParser {

	private final File topLevelConfigDirectory;
	private List<File> importedFiles = new ArrayList<>();
	private List<String> problemEntries = new ArrayList<>();

	// private String currFilePath;

	public LstBuildConfigFileParser(String currFilePath) {
		File canonicalConfigFile;
		try {
			canonicalConfigFile = new File(currFilePath).getCanonicalFile();
		} catch (IOException e) {
			canonicalConfigFile = new File(currFilePath).getAbsoluteFile();
		}
		this.topLevelConfigDirectory = canonicalConfigFile.getParentFile();
	}

	protected void showWarning(String message) {
		problemEntries.add(message);
	}

	protected void parseImportedConfigFile(String relativeFilePath) {
		File importedFile = makeFile(relativeFilePath);
		if (!isSafeImportedConfigFile(importedFile)) {
			showError("imported config file outside project root directory: " + relativeFilePath);
			return;
		}
		importedFiles.add(importedFile);
		super.files.add(importedFile);
		super.parseImportedConfigFile(relativeFilePath);
	}

	private boolean isSafeImportedConfigFile(File importedFile) {
		if (topLevelConfigDirectory == null) {
			return false;
		}
		try {
			File safeRoot = topLevelConfigDirectory.getCanonicalFile();
			File candidate = importedFile.getCanonicalFile();
			while (candidate != null) {
				if (safeRoot.equals(candidate)) {
					return true;
				}
				candidate = candidate.getParentFile();
			}
		} catch (IOException e) {
			// If canonicalization fails, fall back to rejecting the import.
		}
		return false;
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
