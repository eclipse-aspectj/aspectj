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
package org.eclipse.jdt.internal.core.index;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.internal.core.index.impl.FileDocument;
import org.eclipse.jdt.internal.core.index.impl.IFileDocument;

public class DocumentFactory {

	public static IDocument newDocument(File file) {
		return new FileDocument(file);
	}
	public static IDocument newDocument(IFile file) {
		return new IFileDocument(file);
	}
}
