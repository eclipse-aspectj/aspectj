/* *******************************************************************
 * Copyright (c) 2002-2006 Contributors
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *     PARC     initial implementation
 *     AndyClement extracted as self contained type from Lint type (4-Aug-06)
 * ******************************************************************/
package org.aspectj.weaver;

import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.bridge.Message;

public class LintMessage extends Message {

	// private Lint.Kind lintKind;
	private String lintKind;

	public LintMessage(String message, IMessage.Kind messageKind, ISourceLocation location, ISourceLocation[] extraLocations,
			Lint.Kind lintKind) {
		super(message, "", messageKind, location, null, extraLocations);
		this.lintKind = lintKind.getName();
	}

	public LintMessage(String message, String extraDetails, org.aspectj.weaver.Lint.Kind kind2, Kind kind,
			ISourceLocation sourceLocation, Throwable object, ISourceLocation[] seeAlsoLocations, boolean declared, int id,
			int sourceStart, int sourceEnd) {
		super(message, extraDetails, kind, sourceLocation, object, seeAlsoLocations, declared, id, sourceStart, sourceEnd);
		this.lintKind = kind2.getName();
	}

	/**
	 * @return Returns the Lint kind of this message
	 */
	public String getLintKind() {
		return lintKind;
	}

}
