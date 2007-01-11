/********************************************************************
 * Copyright (c) 2006 Contributors. All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: IBM Corporation - initial API and implementation 
 * 				 Helen Hawkins   - initial version
 *******************************************************************/
package org.aspectj.asm.internal;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import org.aspectj.asm.AsmManager;
import org.aspectj.asm.IElementHandleProvider;
import org.aspectj.asm.IProgramElement;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.util.CharOperation;
import org.aspectj.util.NameConvertor;

/**
 * Creates JDT-like handles, for example
 * 
 * method with string argument:  <tjp{Demo.java[Demo~main~\[QString;
 * method with generic argument: <pkg{MyClass.java[MyClass~myMethod~QList\<QString;>;
 * an aspect:					 <pkg*A1.aj}A1
 * advice with Integer arg:      <pkg*A8.aj}A8&afterReturning&QInteger;
 * method call:	                 <pkg*A10.aj[C~m1?method-call(void pkg.C.m2())
 *
 */
public class JDTLikeHandleProvider implements IElementHandleProvider {
 	
	// Need to keep our own count of the number of initializers
	// because this information cannot be gained from the ipe.
	private int initializerCounter = 0;
	
	private char[] empty = new char[]{};
	private char[] countDelim = new char[]{HandleProviderDelimiter.COUNT.getDelimiter()};
	
	private String backslash = "\\";
	private String emptyString = "";
	
	public String createHandleIdentifier(IProgramElement ipe) {

		// AjBuildManager.setupModel --> top of the tree is either
		// <root> or the .lst file
		if (ipe == null || 
				(ipe.getKind().equals(IProgramElement.Kind.FILE_JAVA) 
						&& ipe.getName().equals("<root>"))) {
			return "";
		} else if (ipe.getHandleIdentifier(false) != null) {
			// have already created the handle for this ipe
			// therefore just return it
			return ipe.getHandleIdentifier();
		} else if (ipe.getKind().equals(IProgramElement.Kind.FILE_LST)) {
			String configFile = AsmManager.getDefault().getHierarchy().getConfigFile();
			int start = configFile.lastIndexOf(File.separator);
			int end = configFile.lastIndexOf(".lst");
			if (end != -1) {
				configFile = configFile.substring(start+1,end);
			} else {
				configFile = configFile.substring(start+1);
			}
			ipe.setHandleIdentifier(configFile);
			return configFile;
		}
		IProgramElement parent = ipe.getParent();
		if (parent != null &&
				parent.getKind().equals(IProgramElement.Kind.IMPORT_REFERENCE)) {
			// want to miss out '#import declaration' in the handle
			parent = ipe.getParent().getParent();
		}
		
		StringBuffer handle = new StringBuffer();
		// add the handle for the parent
		handle.append(createHandleIdentifier(parent));
		// add the correct delimiter for this ipe
		handle.append(HandleProviderDelimiter.getDelimiter(ipe));
		// add the name and any parameters unless we're an initializer
		// (initializer's names are '...')
		if (!ipe.getKind().equals(IProgramElement.Kind.INITIALIZER)) {
			handle.append(ipe.getName() + getParameters(ipe));
		}
		// add the count, for example '!2' if its the second ipe of its
		// kind in the aspect
		handle.append(getCount(ipe));

		ipe.setHandleIdentifier(handle.toString());
		return handle.toString();
	}	

	private String getParameters(IProgramElement ipe) {
		if (ipe.getParameterSignatures() == null || ipe.getParameterSignatures().isEmpty()) return "";
		StringBuffer sb = new StringBuffer();
		List parameterTypes = ipe.getParameterSignatures();
		for (Iterator iter = parameterTypes.iterator(); iter.hasNext();) {
			char[] element = (char[]) iter.next();
			sb.append(HandleProviderDelimiter.getDelimiter(ipe));
			if (element[0] == HandleProviderDelimiter.TYPE.getDelimiter()) {
				// its an array
				sb.append(HandleProviderDelimiter.ESCAPE.getDelimiter());
				sb.append(HandleProviderDelimiter.TYPE.getDelimiter());
				sb.append(NameConvertor.getTypeName(
						CharOperation.subarray(element,1,element.length)));
			} else if (element[0] == NameConvertor.PARAMETERIZED) {
				// its a parameterized type
				sb.append(NameConvertor.createShortName(element));
			} else {
				sb.append(NameConvertor.getTypeName(element));
			}
		}
		return sb.toString();
	}
		
	private char[] getCount(IProgramElement ipe) {
		char[] byteCodeName = ipe.getBytecodeName().toCharArray();
		if (ipe.getKind().isDeclare()) {
			int index = CharOperation.lastIndexOf('_',byteCodeName);
			if (index != -1) {
				return convertCount(CharOperation.subarray(byteCodeName,
						index+1,byteCodeName.length));
			}
		} else if (ipe.getKind().equals(IProgramElement.Kind.ADVICE)) {
			int lastDollar = CharOperation.lastIndexOf('$',byteCodeName);
			if (lastDollar != -1) {
				char[] upToDollar = CharOperation.subarray(byteCodeName,0,lastDollar);
				int secondToLastDollar = CharOperation.lastIndexOf('$',upToDollar);
				if (secondToLastDollar != -1) {
					return convertCount(CharOperation.subarray(upToDollar,
							secondToLastDollar+1,upToDollar.length));
				}
			}		
		} else if (ipe.getKind().equals(IProgramElement.Kind.INITIALIZER)) {	
			return String.valueOf(++initializerCounter).toCharArray();
		} else if (ipe.getKind().equals(IProgramElement.Kind.CODE)) {
			int index = CharOperation.lastIndexOf('!',byteCodeName);
			if (index != -1) {
				return convertCount(CharOperation.subarray(byteCodeName,
						index+1,byteCodeName.length));
			}
		}
		return empty;
	}
	
	/**
	 * Only returns the count if it's not equal to 1
	 */
	private char[] convertCount(char[] c) {
		if ((c.length == 1 && c[0] != ' ' && c[0] != '1') || c.length > 1) {
			return CharOperation.concat(countDelim,c);
		}
		return empty;
	}
	
    public String getFileForHandle(String handle) {
    	IProgramElement node = AsmManager.getDefault().getHierarchy().getElement(handle);
    	if (node != null) {
        	return AsmManager.getDefault().getCanonicalFilePath(node.getSourceLocation().getSourceFile());			
		} else if (handle.charAt(0) == HandleProviderDelimiter.ASPECT_CU.getDelimiter() 
				|| handle.charAt(0) == HandleProviderDelimiter.COMPILATIONUNIT.getDelimiter()) {
			// it's something like *MyAspect.aj or {MyClass.java. In other words
			// it's a file node that's been created with no children and no parent
			return backslash + handle.substring(1);
		} 
    	return emptyString;
    }

    public int getLineNumberForHandle(String handle) {
    	IProgramElement node = AsmManager.getDefault().getHierarchy().getElement(handle);
    	if (node != null) {
    		return node.getSourceLocation().getLine();
		} else if (handle.charAt(0) == HandleProviderDelimiter.ASPECT_CU.getDelimiter() 
				|| handle.charAt(0) == HandleProviderDelimiter.COMPILATIONUNIT.getDelimiter()) {
			// it's something like *MyAspect.aj or {MyClass.java. In other words
			// it's a file node that's been created with no children and no parent
			return 1;
		} 
    	return -1;
    }

	public int getOffSetForHandle(String handle) {
    	IProgramElement node = AsmManager.getDefault().getHierarchy().getElement(handle);
    	if (node != null) {
    		return node.getSourceLocation().getOffset();
		} else if (handle.charAt(0) == HandleProviderDelimiter.ASPECT_CU.getDelimiter() 
				|| handle.charAt(0) == HandleProviderDelimiter.COMPILATIONUNIT.getDelimiter()) {
			// it's something like *MyAspect.aj or {MyClass.java. In other words
			// it's a file node that's been created with no children and no parent
			return 0;
		} 		
    	return -1;
	}

	public String createHandleIdentifier(ISourceLocation location) {
		IProgramElement node = AsmManager.getDefault().getHierarchy().findElementForSourceLine(location);
		if (node != null) {
			return createHandleIdentifier(node);
		}
		return null;
	}

	public String createHandleIdentifier(File sourceFile, int line, int column, int offset) {
		IProgramElement node = AsmManager.getDefault().getHierarchy().findElementForOffSet(sourceFile.getAbsolutePath(),line,offset);
		if (node != null) {
			return createHandleIdentifier(node);
		}
		return null;
	}
	
	public boolean dependsOnLocation() {
		// handles are independent of soureLocations therefore return false
		return false;
	}

	public void initialize() {
		// reset the initializer count. This ensures we return the
		// same handle as JDT for initializers.
		initializerCounter = 0;
	}
}
