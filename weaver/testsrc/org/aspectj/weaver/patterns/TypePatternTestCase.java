/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.weaver.patterns;

import java.io.*;

import org.aspectj.weaver.bcel.*;
import org.aspectj.bridge.AbortException;
import org.aspectj.util.FuzzyBoolean;

import junit.framework.TestCase;
import org.aspectj.weaver.*;

public class TypePatternTestCase extends TestCase {		
	/**
	 * Constructor for PatternTestCase.
	 * @param name
	 */
	public TypePatternTestCase(String name) {
		super(name);
	}
	
	World world;
	
	public void testStaticMatch() {
		world = new BcelWorld();
		
		checkMatch("java.lang.Object", "java.lang.Object", true);
		checkMatch("java.lang.Object+", "java.lang.Object", true);
		checkMatch("java.lang.Object+", "java.lang.String", true);
		checkMatch("java.lang.String+", "java.lang.Object", false);
		checkMatch("java.lang.Integer", "java.lang.String", false);

		checkMatch("java.lang.Integer", "int", false);

		checkMatch("java.lang.Number+", "java.lang.Integer", true);
		
		
		checkMatch("java..*", "java.lang.Integer", true);
		checkMatch("java..*", "java.lang.reflect.Modifier", true);
		checkMatch("java..*", "int", false);
		checkMatch("java..*", "javax.swing.Action", false);
		checkMatch("java..*+", "javax.swing.Action", true);
		
		checkMatch("*.*.Object", "java.lang.Object", true);
		checkMatch("*.Object", "java.lang.Object", false);
        checkMatch("*..*", "java.lang.Object", true);
        checkMatch("*..*", "int", false);
        checkMatch("java..Modifier", "java.lang.reflect.Modifier", true);
        checkMatch("java.lang.reflect.Mod..ifier", "java.lang.reflect.Modifier", false);
        
        checkMatch("java..reflect..Modifier", "java.lang.reflect.Modifier", true);
        checkMatch("java..lang..Modifier", "java.lang.reflect.Modifier", true);        
        checkMatch("java..*..Modifier", "java.lang.reflect.Modifier", true);
        checkMatch("java..*..*..Modifier", "java.lang.reflect.Modifier", true);
        checkMatch("java..*..*..*..Modifier", "java.lang.reflect.Modifier", false);        
        //checkMatch("java..reflect..Modifier", "java.lang.reflect.Modxifier", false);
        checkMatch("ja*va..Modifier", "java.lang.reflect.Modifier", true);
        checkMatch("java..*..Mod*ifier", "java.lang.reflect.Modifier", true);        
	
	}
	
	
	// three levels:
	//  0. defined in current compilation unit, or imported by name
	//  1. defined in current package/type/whatever
	//  2. defined in package imported by *
	/**
	 * We've decided not to test this here, but rather in any compilers
	 */
	public void testImportResolve() {
		world = new BcelWorld();
//		checkIllegalImportResolution("List", new String[] { "java.util", "java.awt", }, 
//			ZERO_STRINGS);


	}
	
	
	// Assumption for bcweaver:  Already resolved type patterns with no *s or ..'s into exact type
	// patterns.  Exact type patterns don't have import lists.  non-exact-type pattens don't
	// care about precedence, so the current package can be included with all the other packages, 
	// and we don't care about compilation units, and we don't care about ordering.

	// only giving this wild-type patterns	
	public void testImportMatch() {
		world = new BcelWorld();

		checkImportMatch("*List", new String[] { "java.awt.", }, ZERO_STRINGS, "java.awt.List", true);
		checkImportMatch("*List", new String[] { "java.awt.", }, ZERO_STRINGS, "java.awt.List", true);
		checkImportMatch("*List", new String[] { "java.awt.", }, ZERO_STRINGS, "java.util.List", false);
		checkImportMatch("*List", new String[] { "java.util.", }, ZERO_STRINGS, "java.awt.List", false);
		checkImportMatch("*List", new String[] { "java.util.", }, ZERO_STRINGS, "java.util.List", true);

		checkImportMatch("*List", ZERO_STRINGS, new String[] { "java.awt.List", }, "java.awt.List", true);

		checkImportMatch("awt.*List", ZERO_STRINGS, new String[] { "java.awt.List", }, "java.awt.List", false);
		checkImportMatch("*Foo", ZERO_STRINGS, new String[] { "java.awt.List", }, "java.awt.List", false);

		checkImportMatch("*List", new String[] { "java.util.", "java.awt.", }, 
			ZERO_STRINGS, "java.util.List", true);
		checkImportMatch("*List", new String[] { "java.util.", "java.awt.", }, 
			ZERO_STRINGS, "java.awt.List", true);

		checkImportMatch("*..List", new String[] { "java.util." }, ZERO_STRINGS, "java.util.List", true);
		checkImportMatch("*..List", new String[] { "java.util." }, ZERO_STRINGS, "java.awt.List", true);
		
	}
	
	public void testImportMatchWithInners() {
		world = new BcelWorld();
		checkImportMatch("*Entry", new String[] { "java.util.", "java.util.Map$"}, ZERO_STRINGS,
						"java.util.Map$Entry", true);
	
		checkImportMatch("java.util.Map.*Entry", ZERO_STRINGS, ZERO_STRINGS,
						"java.util.Map$Entry", true);

		checkImportMatch("*Entry", new String[] { "java.util.", }, ZERO_STRINGS,
						"java.util.Map$Entry", false);
						
		checkImportMatch("*.Entry", new String[] { "java.util.", }, ZERO_STRINGS,
						"java.util.Map$Entry", true);
						
		checkImportMatch("Map.*", new String[] { "java.util.", }, ZERO_STRINGS,
						"java.util.Map$Entry", true);
						
		checkImportMatch("Map.*", ZERO_STRINGS, new String[] { "java.util.Map" },
						"java.util.Map$Entry", true);
	}

	private void checkImportMatch(
		String wildPattern,
		String[] importedPackages,
		String[] importedNames,
		String matchName,
		boolean shouldMatch) 
	{
		WildTypePattern p = makeResolvedWildTypePattern(wildPattern, importedPackages, importedNames);
		checkPatternMatch(p, matchName, shouldMatch);	
	}


	private WildTypePattern makeResolvedWildTypePattern(
		String wildPattern,
		String[] importedPackages,
		String[] importedNames) 
	{
		WildTypePattern unresolved = (WildTypePattern) new PatternParser(wildPattern).parseTypePattern();
		
		WildTypePattern resolved = resolve(unresolved, importedPackages, importedNames);
		return resolved;
		
	}

	private WildTypePattern resolve(
		WildTypePattern unresolved,
		String[] importedPrefixes,
		String[] importedNames) 
	{
		
		TestScope scope = makeTestScope();
		scope.setImportedPrefixes(importedPrefixes);
		scope.setImportedNames(importedNames);
		return (WildTypePattern) unresolved.resolveBindings(scope, Bindings.NONE, false, false);
	}



	public static final String[] ZERO_STRINGS = new String[0];

  	public void testInstanceofMatch() {
		world = new BcelWorld();
		
		checkInstanceofMatch("java.lang.Object", "java.lang.Object", FuzzyBoolean.YES);

		checkIllegalInstanceofMatch("java.lang.Object+", "java.lang.Object");
		checkIllegalInstanceofMatch("java.lang.Object+", "java.lang.String");
		checkIllegalInstanceofMatch("java.lang.String+", "java.lang.Object");
		checkIllegalInstanceofMatch("java.lang.*", "java.lang.Object");
		checkInstanceofMatch("java.lang.Integer", "java.lang.String", FuzzyBoolean.NO);

		checkInstanceofMatch("java.lang.Number", "java.lang.Integer", FuzzyBoolean.YES);
		checkInstanceofMatch("java.lang.Integer", "java.lang.Number", FuzzyBoolean.MAYBE);
				
		
		checkIllegalInstanceofMatch("java..Integer", "java.lang.Integer");

		
		checkInstanceofMatch("*", "java.lang.Integer", FuzzyBoolean.YES);
		
	
	}
  	
  	public void testArrayMatch() {
  		world = new BcelWorld();
  		checkMatch("*[][]","java.lang.Object",false);
  		checkMatch("*[]","java.lang.Object[]",true);
  		checkMatch("*[][]","java.lang.Object[][]",true);
  		checkMatch("java.lang.Object[]","java.lang.Object",false);
  		checkMatch("java.lang.Object[]","java.lang.Object[]",true);
  		checkMatch("java.lang.Object[][]","java.lang.Object[][]",true);
  		checkMatch("java.lang.String[]","java.lang.Object",false);
  		checkMatch("java.lang.String[]","java.lang.Object[]",false);
  		checkMatch("java.lang.String[][]","java.lang.Object[][]",false);
  		checkMatch("java.lang.Object+[]","java.lang.String[]",true);
  	}

	private void checkIllegalInstanceofMatch(String pattern, String name) {
		try {
			TypePattern p = makeTypePattern(pattern);
			ResolvedType type = world.resolve(name);
			/*FuzzyBoolean result = */p.matchesInstanceof(type);
		} catch (AbortException e) {
			return;
		}
		assertTrue("matching " + pattern + " with " + name + " should fail", false);
	}
  
  	private void checkInstanceofMatch(String pattern, String name, FuzzyBoolean shouldMatch) {
		TypePattern p = makeTypePattern(pattern);
		ResolvedType type = world.resolve(name);
		
		p = p.resolveBindings(makeTestScope(), null, false, false);
		
		
		//System.out.println("type: " + p);
		FuzzyBoolean result = p.matchesInstanceof(type);
		String msg = "matches " + pattern + " to " + type;
		assertEquals(msg, shouldMatch, result);
	}

	private TestScope makeTestScope() {
		TestScope scope = new TestScope(ZERO_STRINGS, ZERO_STRINGS, world);
		return scope;
	}
  
	private TypePattern makeTypePattern(String pattern) {
		return new PatternParser(pattern).parseSingleTypePattern();
	}

	private void checkMatch(String pattern, String name, boolean shouldMatch) {
		TypePattern p = makeTypePattern(pattern);
		p = p.resolveBindings(makeTestScope(), null, false, false);
		checkPatternMatch(p, name, shouldMatch);
	}

	private void checkPatternMatch(
		TypePattern p,
		String name,
		boolean shouldMatch) 
	{
		ResolvedType type = world.resolve(name);
		//System.out.println("type: " + type);
		boolean result = p.matchesStatically(type);
		String msg = "matches " + p + " to " + type + " expected ";
		if (shouldMatch) {
			assertTrue(msg + shouldMatch, result);
		} else {
			assertTrue(msg + shouldMatch, !result);
		}
	}
	
	public void testSerialization() throws IOException {
		String[] patterns = new String[] {
			"java.lang.Object", "java.lang.Object+", "java.lang.Integer",
			"int", "java..*", "java..util..*", "*.*.Object", "*",
		};
		
		for (int i=0, len=patterns.length; i < len; i++) {
			checkSerialization(patterns[i]);
		}
	}

	/**
	 * Method checkSerialization.
	 * @param string
	 */
	private void checkSerialization(String string) throws IOException {
		TypePattern p = makeTypePattern(string);
		ByteArrayOutputStream bo = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(bo);
		p.write(out);
		out.close();
		
		ByteArrayInputStream bi = new ByteArrayInputStream(bo.toByteArray());
		VersionedDataInputStream in = new VersionedDataInputStream(bi);
		TypePattern newP = TypePattern.read(in, null);
		
		assertEquals("write/read", p, newP);	
	}
	
}
