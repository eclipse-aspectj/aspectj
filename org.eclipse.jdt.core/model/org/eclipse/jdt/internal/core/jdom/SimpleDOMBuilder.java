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
package org.eclipse.jdt.internal.core.jdom;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.jdom.IDOMCompilationUnit;
import org.eclipse.jdt.internal.compiler.ISourceElementRequestor;
import org.eclipse.jdt.internal.compiler.SourceElementParser;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.core.util.CharArrayOps;

/**
 * A DOM builder that uses the SourceElementParser
 */
public class SimpleDOMBuilder extends AbstractDOMBuilder implements ISourceElementRequestor {

public void acceptImport(int declarationStart, int declarationEnd, char[] name, boolean onDemand) {
	int[] sourceRange = {declarationStart, declarationEnd};
	String importName = new String(name);
	/** name is set to contain the '*' */
	if (onDemand) {
		importName+=".*"; //$NON-NLS-1$
	}
	fNode= new DOMImport(fDocument, sourceRange, importName, onDemand);
	addChild(fNode);	
}
public void acceptPackage(int declarationStart, int declarationEnd, char[] name) {
	int[] sourceRange= new int[] {declarationStart, declarationEnd};
	fNode= new DOMPackage(fDocument, sourceRange, CharArrayOps.charToString(name));
	addChild(fNode);	
}
/**
 * @see IDOMFactory#createCompilationUnit(String, String)
 */
public IDOMCompilationUnit createCompilationUnit(String sourceCode, String name) {
	return createCompilationUnit(sourceCode.toCharArray(), name.toCharArray());
}
/**
 * @see IDOMFactory#createCompilationUnit(String, String)
 */
public IDOMCompilationUnit createCompilationUnit(ICompilationUnit compilationUnit) {
	initializeBuild(compilationUnit.getContents(), true, true);
	getParser().parseCompilationUnit(compilationUnit, false);
	return super.createCompilationUnit(compilationUnit);
}
/**
 * Creates a new DOMMethod and inizializes.
 *
 * @param declarationStart - a source position corresponding to the first character 
 *		of this constructor declaration
 * @param modifiers - the modifiers for this constructor converted to a flag
 * @param returnType - the name of the return type
 * @param name - the name of this constructor
 * @param nameStart - a source position corresponding to the first character of the name
 * @param nameEnd - a source position corresponding to the last character of the name
 * @param parameterTypes - a list of parameter type names
 * @param parameterNames - a list of the names of the parameters
 * @param exceptionTypes - a list of the exception types
 */
protected void enterAbstractMethod(int declarationStart, int modifiers,
	char[] returnType, char[] name, int nameStart, int nameEnd, char[][] parameterTypes,
	char[][] parameterNames, char[][] exceptionTypes, boolean isConstructor) {
		
	int[] sourceRange = {declarationStart, -1}; // will be fixed up on exit
	int[] nameRange = {nameStart, nameEnd};
	fNode = new DOMMethod(fDocument, sourceRange, CharArrayOps.charToString(name), nameRange, modifiers, 
		isConstructor, CharArrayOps.charToString(returnType),
		CharArrayOps.charcharToString(parameterTypes),
		CharArrayOps.charcharToString(parameterNames), 
		CharArrayOps.charcharToString(exceptionTypes));
	addChild(fNode);
	fStack.push(fNode);
}
/**
 */
public void enterClass(int declarationStart, int modifiers, char[] name, int nameStart, int nameEnd, char[] superclass, char[][] superinterfaces) {
	enterType(declarationStart, modifiers, name, nameStart, nameEnd, superclass,
		superinterfaces, true);
}
/**
 */
public void enterConstructor(int declarationStart, int modifiers, char[] name, int nameStart, int nameEnd, char[][] parameterTypes, char[][] parameterNames, char[][] exceptionTypes) {
	/* see 1FVIIQZ */
	String nameString = new String(fDocument, nameStart, nameEnd - nameStart);
	int openParenPosition = nameString.indexOf('(');
	if (openParenPosition > -1)
		nameEnd = nameStart + openParenPosition - 1;

	enterAbstractMethod(declarationStart, modifiers, 
		null, name, nameStart, nameEnd, parameterTypes,
		parameterNames, exceptionTypes,true);
}
/**
 */
public void enterField(int declarationStart, int modifiers, char[] type, char[] name, int nameStart, int nameEnd) {

	int[] sourceRange = {declarationStart, -1};
	int[] nameRange = {nameStart, nameEnd};
	boolean isSecondary= false;
	if (fNode instanceof DOMField) {
		isSecondary = declarationStart == fNode.fSourceRange[0];
	}
	fNode = new DOMField(fDocument, sourceRange, CharArrayOps.charToString(name), nameRange, 
		modifiers, CharArrayOps.charToString(type), isSecondary);
	addChild(fNode);
	fStack.push(fNode);
}
/**

 */
public void enterInitializer(int declarationSourceStart, int modifiers) {
	int[] sourceRange = {declarationSourceStart, -1};
	fNode = new DOMInitializer(fDocument, sourceRange, modifiers);
	addChild(fNode);
	fStack.push(fNode);
}
/**
 */
public void enterInterface(int declarationStart, int modifiers, char[] name, int nameStart, int nameEnd, char[][] superinterfaces) {
	enterType(declarationStart, modifiers, name, nameStart, nameEnd, null,
		superinterfaces, false);
}
/**
 */
public void enterMethod(int declarationStart, int modifiers, char[] returnType, char[] name, int nameStart, int nameEnd, char[][] parameterTypes, char[][] parameterNames, char[][] exceptionTypes) {
	enterAbstractMethod(declarationStart, modifiers, 
		returnType, name, nameStart, nameEnd, parameterTypes,
		parameterNames, exceptionTypes,false);
}
/**
 */
protected void enterType(int declarationStart, int modifiers, char[] name, 
	int nameStart, int nameEnd, char[] superclass, char[][] superinterfaces, boolean isClass) {
	if (fBuildingType) {
		int[] sourceRange = {declarationStart, -1}; // will be fixed in the exit
		int[] nameRange = new int[] {nameStart, nameEnd};
		fNode = new DOMType(fDocument, sourceRange, new String(name), nameRange,
			modifiers, CharArrayOps.charcharToString(superinterfaces), isClass);
		addChild(fNode);
		fStack.push(fNode);
	}
}
/**
 * Finishes the configuration of the class DOM object which
 * was created by a previous enterClass call.
 *
 * @see ISourceElementRequestor#exitClass(int)
 */
public void exitClass(int declarationEnd) {
	exitType(declarationEnd);
}
/**
 * Finishes the configuration of the method DOM object which
 * was created by a previous enterConstructor call.
 *
 * @see ISourceElementRequestor#exitConstructor(int)
 */
public void exitConstructor(int declarationEnd) {
	exitMember(declarationEnd);
}
/**
 */
public void exitField(int declarationEnd) {
	exitMember(declarationEnd);
}
/**
 */
public void exitInitializer(int declarationEnd) {
	exitMember(declarationEnd);
}
/**
 */
public void exitInterface(int declarationEnd) {
	exitType(declarationEnd);
}
/**
 * Finishes the configuration of the member.
 *
 * @param declarationEnd - a source position corresponding to the end of the method
 *		declaration.  This can include whitespace and comments following the closing bracket.
 */
protected void exitMember(int declarationEnd) {
	DOMMember m= (DOMMember) fStack.pop();
	m.setSourceRangeEnd(declarationEnd);
	fNode = m;
}
/**
 */
public void exitMethod(int declarationEnd) {
	exitMember(declarationEnd);
}
/**
 * @see AbstractDOMBuilder#exitType
 *
 * @param declarationEnd - a source position corresponding to the end of the class
 *		declaration.  This can include whitespace and comments following the closing bracket.
 */
protected void exitType(int declarationEnd) {
	exitType(declarationEnd, declarationEnd);
}
/**
 * Creates a new parser.
 */
protected SourceElementParser getParser() {
	return new SourceElementParser(this, new DefaultProblemFactory(), new CompilerOptions(JavaCore.getOptions()));
}
}
