/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.aspectj.ajdt.internal.core.builder;

import java.util.HashMap;
import java.util.Map;

import org.aspectj.org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.TestCase;

/**
 * @author Adrian Colyer
 * @uahtor Andy Clement
 */
public class AjCompilerOptionsTest extends TestCase {

	private AjCompilerOptions options;
	
	protected void setUp() throws Exception {
		super.setUp();
		options = new AjCompilerOptions();
	}
	
	public void testDefaultValues() {
		assertFalse(options.terminateAfterCompilation);
		assertFalse(options.xSerializableAspects);
		assertFalse(options.xLazyThisJoinPoint);
		assertFalse(options.xNoInline);
		assertFalse(options.xNotReweavable);
		assertFalse(options.generateModel);
		assertFalse(options.generateJavaDocsInModel);
		assertFalse(options.generateEmacsSymFiles);
		assertFalse(options.noAtAspectJProcessing);
		
		Map<String,String> map = options.getMap();
		assertEquals(CompilerOptions.WARNING,map.get(AjCompilerOptions.OPTION_ReportInvalidAbsoluteTypeName));
		assertEquals(CompilerOptions.IGNORE,map.get(AjCompilerOptions.OPTION_ReportInvalidWildcardTypeName));
		assertEquals(CompilerOptions.WARNING,map.get(AjCompilerOptions.OPTION_ReportUnresolvableMember));
		assertEquals(CompilerOptions.WARNING,map.get(AjCompilerOptions.OPTION_ReportTypeNotExposedToWeaver));
		assertEquals(CompilerOptions.IGNORE,map.get(AjCompilerOptions.OPTION_ReportShadowNotInStructure));
		assertEquals(CompilerOptions.WARNING,map.get(AjCompilerOptions.OPTION_ReportUnmatchedSuperTypeInCall));
		assertEquals(CompilerOptions.WARNING,map.get(AjCompilerOptions.OPTION_ReportCannotImplementLazyTJP));
		assertEquals(CompilerOptions.IGNORE,map.get(AjCompilerOptions.OPTION_ReportNeedSerialVersionUIDField));
		assertEquals(CompilerOptions.IGNORE,map.get(AjCompilerOptions.OPTION_ReportIncompatibleSerialVersion));
	}
	
	public void testDirectSet() {
		options.terminateAfterCompilation = true;
		options.xSerializableAspects = true;
		options.xLazyThisJoinPoint = true;
		options.xNoInline = true;
		options.xNotReweavable = true;
		options.generateModel = true;
		options.generateJavaDocsInModel = true;
		options.generateEmacsSymFiles = true;
		options.noAtAspectJProcessing = true;

		Map<String,String> map = options.getMap();
		assertEquals(CompilerOptions.ENABLED,map.get(AjCompilerOptions.OPTION_TerminateAfterCompilation));
		assertEquals(CompilerOptions.ENABLED,map.get(AjCompilerOptions.OPTION_XSerializableAspects));
		assertEquals(CompilerOptions.ENABLED,map.get(AjCompilerOptions.OPTION_XLazyThisJoinPoint));
		assertEquals(CompilerOptions.ENABLED,map.get(AjCompilerOptions.OPTION_XNoInline));
		assertEquals(CompilerOptions.ENABLED,map.get(AjCompilerOptions.OPTION_XNotReweavable));
		assertEquals(CompilerOptions.ENABLED,map.get(AjCompilerOptions.OPTION_GenerateModel));
		assertEquals(CompilerOptions.ENABLED,map.get(AjCompilerOptions.OPTION_GenerateJavaDocsInModel));
		assertEquals(CompilerOptions.ENABLED,map.get(AjCompilerOptions.OPTION_Emacssym));
		assertEquals(CompilerOptions.ENABLED,map.get(AjCompilerOptions.OPTION_XDevNoAtAspectJProcessing));
	}

	
	public void testMapSet() {
		Map<String,String> map = new HashMap<>();
		map.put(AjCompilerOptions.OPTION_ReportInvalidAbsoluteTypeName,CompilerOptions.ERROR);
		map.put(AjCompilerOptions.OPTION_ReportInvalidWildcardTypeName,CompilerOptions.ERROR);
		map.put(AjCompilerOptions.OPTION_ReportUnresolvableMember,CompilerOptions.IGNORE);
		map.put(AjCompilerOptions.OPTION_ReportTypeNotExposedToWeaver,CompilerOptions.ERROR);
		map.put(AjCompilerOptions.OPTION_ReportShadowNotInStructure,CompilerOptions.WARNING);
		map.put(AjCompilerOptions.OPTION_ReportUnmatchedSuperTypeInCall,CompilerOptions.ERROR);
		map.put(AjCompilerOptions.OPTION_ReportCannotImplementLazyTJP,CompilerOptions.ERROR);
		map.put(AjCompilerOptions.OPTION_ReportNeedSerialVersionUIDField,CompilerOptions.WARNING);
		map.put(AjCompilerOptions.OPTION_ReportIncompatibleSerialVersion,CompilerOptions.ERROR);
		
		map.put(AjCompilerOptions.OPTION_TerminateAfterCompilation,CompilerOptions.ENABLED);
		map.put(AjCompilerOptions.OPTION_XSerializableAspects,CompilerOptions.ENABLED);
		map.put(AjCompilerOptions.OPTION_XLazyThisJoinPoint,CompilerOptions.ENABLED);
		map.put(AjCompilerOptions.OPTION_XNoInline,CompilerOptions.ENABLED);
		map.put(AjCompilerOptions.OPTION_XNotReweavable,CompilerOptions.ENABLED);
		map.put(AjCompilerOptions.OPTION_GenerateModel,CompilerOptions.ENABLED);
		map.put(AjCompilerOptions.OPTION_GenerateJavaDocsInModel,CompilerOptions.ENABLED);
		map.put(AjCompilerOptions.OPTION_Emacssym,CompilerOptions.ENABLED);
		map.put(AjCompilerOptions.OPTION_XDevNoAtAspectJProcessing,CompilerOptions.ENABLED);
		
		options.set(map);
		
		assertTrue(options.terminateAfterCompilation);
		assertTrue(options.xSerializableAspects);
		assertTrue(options.xLazyThisJoinPoint);
		assertTrue(options.xNoInline);
		assertTrue(options.xNotReweavable);
		assertTrue(options.generateModel);
		assertTrue(options.generateJavaDocsInModel);
		assertTrue(options.generateEmacsSymFiles);
		assertTrue(options.noAtAspectJProcessing);
		
		Map<String,String> newMap = options.getMap();
		assertEquals(CompilerOptions.ERROR,newMap.get(AjCompilerOptions.OPTION_ReportInvalidAbsoluteTypeName));
		assertEquals(CompilerOptions.ERROR,newMap.get(AjCompilerOptions.OPTION_ReportInvalidWildcardTypeName));
		assertEquals(CompilerOptions.IGNORE,newMap.get(AjCompilerOptions.OPTION_ReportUnresolvableMember));
		assertEquals(CompilerOptions.ERROR,newMap.get(AjCompilerOptions.OPTION_ReportTypeNotExposedToWeaver));
		assertEquals(CompilerOptions.WARNING,newMap.get(AjCompilerOptions.OPTION_ReportShadowNotInStructure));
		assertEquals(CompilerOptions.ERROR,newMap.get(AjCompilerOptions.OPTION_ReportUnmatchedSuperTypeInCall));
		assertEquals(CompilerOptions.ERROR,newMap.get(AjCompilerOptions.OPTION_ReportCannotImplementLazyTJP));
		assertEquals(CompilerOptions.WARNING,newMap.get(AjCompilerOptions.OPTION_ReportNeedSerialVersionUIDField));
		assertEquals(CompilerOptions.ERROR,newMap.get(AjCompilerOptions.OPTION_ReportIncompatibleSerialVersion));
	}
	
	public void testToString() {
		String s = options.toString();
		assertTrue("Should have info on AspectJ options",s.indexOf("AspectJ Specific Options:") > 0);
	}

}
