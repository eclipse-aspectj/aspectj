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
package org.eclipse.jdt.internal.core.search.indexing;

import java.io.IOException;
import java.util.Locale;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.compiler.SourceElementParser;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.core.index.IDocument;
import org.eclipse.jdt.internal.core.jdom.CompilationUnit;

/**
 * A SourceIndexer indexes java files using a java parser. The following items are indexed:
 * Declarations of:
 * - Classes<br>
 * - Interfaces; <br>
 * - Methods;<br>
 * - Fields;<br>
 * References to:
 * - Methods (with number of arguments); <br>
 * - Fields;<br>
 * - Types;<br>
 * - Constructors.
 */
public class SourceIndexer extends AbstractIndexer {
	
	public static final String[] FILE_TYPES= new String[] {"java"}; //$NON-NLS-1$
	protected DefaultProblemFactory problemFactory= new DefaultProblemFactory(Locale.getDefault());
	
/**
 * Returns the file types the <code>IIndexer</code> handles.
 */

public String[] getFileTypes(){
	return FILE_TYPES;
}
protected void indexFile(IDocument document) throws IOException {

	// Add the name of the file to the index
	output.addDocument(document);

	// Create a new Parser
	SourceIndexerRequestor requestor = new SourceIndexerRequestor(this, document);
	SourceElementParser parser = new SourceElementParser(requestor, problemFactory, new CompilerOptions(JavaCore.getOptions()), true); // index local declarations

	// Launch the parser
	char[] source = null;
	char[] name = null;
	try {
		source = document.getCharContent();
		name = document.getName().toCharArray();
	} catch(Exception e){
	}
	if (source == null || name == null) return; // could not retrieve document info (e.g. resource was discarded)
	CompilationUnit compilationUnit = new CompilationUnit(source, name);
	try {
		parser.parseCompilationUnit(compilationUnit, true);
	} catch (Exception e) {
		e.printStackTrace();
	}
}
/**
 * Sets the document types the <code>IIndexer</code> handles.
 */

public void setFileTypes(String[] fileTypes){}
}
