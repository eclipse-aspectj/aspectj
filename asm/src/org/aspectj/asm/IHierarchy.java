/* *******************************************************************
 * Copyright (c) 2003,2010 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Mik Kersten     initial implementation 
 *     Andy Clement
 * ******************************************************************/
package org.aspectj.asm;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.aspectj.asm.internal.ProgramElement;
import org.aspectj.bridge.ISourceLocation;

/**
 * @author Mik Kersten
 * @author Andy Clement
 */
public interface IHierarchy extends Serializable {

	public static final IProgramElement NO_STRUCTURE = new ProgramElement(null, "<build to view structure>",
			IProgramElement.Kind.ERROR, null);

	public IProgramElement getElement(String handle);

	public IProgramElement getRoot();

	public void setRoot(IProgramElement root);

	public void addToFileMap(String canonicalFilePath, IProgramElement compilationUnitProgramElement);

	public boolean removeFromFileMap(String canonicalFilePath);

	public void setFileMap(HashMap<String, IProgramElement> fileMap);

	public Object findInFileMap(Object key);

	public Set<Map.Entry<String, IProgramElement>> getFileMapEntrySet();

	public boolean isValid();

	public IProgramElement findElementForHandle(String handle);

	public IProgramElement findElementForHandleOrCreate(String handle, boolean create);

	/**
	 * Returns the first match
	 * 
	 * @param parent
	 * @param kind not null
	 * @return null if not found
	 */
	public IProgramElement findElementForSignature(IProgramElement parent, IProgramElement.Kind kind, String signature);

	/**
	 * Returns the first match
	 * 
	 * @param parent
	 * @param kind not null
	 * @return null if not found
	 */
	public IProgramElement findElementForLabel(IProgramElement parent, IProgramElement.Kind kind, String label);

	/**
	 * @param packageName if null default package is searched
	 * @param className can't be null
	 */
	public IProgramElement findElementForType(String packageName, String typeName);

	/**
	 * @param sourceFilePath modified to '/' delimited path for consistency
	 * @return a new structure node for the file if it was not found in the model
	 */
	public IProgramElement findElementForSourceFile(String sourceFile);

	/**
	 * TODO: discriminate columns
	 */
	public IProgramElement findElementForSourceLine(ISourceLocation location);

	/**
	 * Never returns null
	 * 
	 * @param sourceFilePath canonicalized path for consistency
	 * @param lineNumber if 0 or 1 the corresponding file node will be returned
	 * @return a new structure node for the file if it was not found in the model
	 */
	public IProgramElement findElementForSourceLine(String sourceFilePath, int lineNumber);

	public IProgramElement findElementForOffSet(String sourceFilePath, int lineNumber, int offSet);

	public String getConfigFile();

	public void setConfigFile(String configFile);

	public void flushTypeMap();

	public void flushHandleMap();

	public void updateHandleMap(Set<String> deletedFiles);

	/**
	 * For a specified node, check if any of the children more accurately represent the specified line.
	 * 
	 * @param node where to start looking
	 * @param lineno the line number
	 * @return any closer match below 'node' or null if nothing is a more accurate match
	 */
	public IProgramElement findCloserMatchForLineNumber(IProgramElement node, int lineno);

	/**
	 * Discover the node representing a particular source file.
	 * 
	 * @param node where in the model to start looking (usually the root on the initial call)
	 * @param sourcefilePath the source file being searched for
	 * @return the node representing that source file or null if it cannot be found
	 */
	public IProgramElement findNodeForSourceFile(IProgramElement node, String sourcefilePath);
}