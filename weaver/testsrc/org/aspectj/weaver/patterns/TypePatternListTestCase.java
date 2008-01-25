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
import java.util.Arrays;

import org.aspectj.weaver.bcel.*;
import org.aspectj.util.*;

import junit.framework.TestCase;
import org.aspectj.weaver.*;

/**
 * @author hugunin
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class TypePatternListTestCase extends TestCase {		
	/**
	 * Constructor for PatternTestCase.
	 * @param name
	 */
	public TypePatternListTestCase(String name) {
		super(name);
	}
	
	World world;
	
	//XXX when instanceof matching works add tests for that here
	
	
	public void testMatch() {
		world = new BcelWorld();
        
        checkStaticMatch("()", new String[] {}, FuzzyBoolean.YES);
        checkStaticMatch("()", new String[] {"java.lang.Object"}, FuzzyBoolean.NO);
       
        checkStaticMatch("(java.lang.Object)", new String[] {"java.lang.Object"}, FuzzyBoolean.YES);

        checkStaticMatch("(java.lang.String)", new String[] {"java.lang.Object"}, FuzzyBoolean.NO);

        checkStaticMatch("(java.lang.Object)", new String[] {"java.lang.String"}, FuzzyBoolean.NO);

        checkStaticMatch("()", new String[] {"java.lang.Object"}, FuzzyBoolean.NO);
        
        checkStaticMatch("(..)", new String[] {}, FuzzyBoolean.YES);
        checkStaticMatch("(..)", new String[] {"int", "char"}, FuzzyBoolean.YES);


        checkStaticMatch("(int,..,int)", new String[] {"int", "int"}, FuzzyBoolean.YES);

        checkStaticMatch("(int,..)", new String[] {}, FuzzyBoolean.NO);
        checkStaticMatch("(int,..)", new String[] {"int"}, FuzzyBoolean.YES);
         
        checkStaticMatch("(..,int,..)", new String[] {"int"}, FuzzyBoolean.YES);

        // these checks are taken from new/ExpandedDotPattern.java
        stupidCheck("( ..,  ..,  ..)", new boolean[] { true,  true,  true, true,  true });
        stupidCheck("( ..,  .., int)", new boolean[] { false, true,  true,  true,  true });
        stupidCheck("( .., int,  ..)", new boolean[] { false, true,  true,  true,  true });
        stupidCheck("( .., int, int)", new boolean[] { false, false, true,  true,  true });
        stupidCheck("(int,  ..,  ..)", new boolean[] { false, true,  true,  true,  true });
        stupidCheck("(int,  .., int)", new boolean[] { false, false, true,  true,  true });
        stupidCheck("(int, int,  ..)", new boolean[] { false, false, true,  true,  true });
        stupidCheck("(int, int, int)", new boolean[] { false, false, false, true,  false });
        
        stupidCheck("( ..,  ..,  ..,  ..)", new boolean[] { true,  true,  true,  true,  true });
        stupidCheck("( ..,  ..,  .., int)", new boolean[] { false, true,  true,  true,  true });
        stupidCheck("( ..,  .., int,  ..)", new boolean[] { false, true,  true,  true,  true });
        stupidCheck("( ..,  .., int, int)", new boolean[] { false, false, true,  true,  true });
        stupidCheck("( .., int,  ..,  ..)", new boolean[] { false, true,  true,  true,  true });
        stupidCheck("( .., int,  .., int)", new boolean[] { false, false, true,  true,  true });
        stupidCheck("( .., int, int,  ..)", new boolean[] { false, false, true,  true,  true });
        stupidCheck("( .., int, int, int)", new boolean[] { false, false, false, true,  true });
               
        stupidCheck("(int,  ..,  ..,  ..)", new boolean[] { false, true,  true,  true,  true });
        stupidCheck("(int,  ..,  .., int)", new boolean[] { false, false, true,  true,  true });
        stupidCheck("(int,  .., int,  ..)", new boolean[] { false, false, true,  true,  true });
        stupidCheck("(int,  .., int, int)", new boolean[] { false, false, false, true,  true });
        stupidCheck("(int, int,  ..,  ..)", new boolean[] { false, false, true,  true,  true });
        stupidCheck("(int, int,  .., int)", new boolean[] { false, false, false, true,  true });
        stupidCheck("(int, int, int,  ..)", new boolean[] { false, false, false, true,  true });
        stupidCheck("(int, int, int, int)", new boolean[] { false, false, false, false, true });	
	}

	private TypePatternList makeArgumentsPattern(String pattern) {
		return new PatternParser(pattern).parseArgumentsPattern(false);
	}

	private void checkStaticMatch(String pattern, String[] names, 
                            FuzzyBoolean shouldMatchStatically) {
        // We're only doing TypePattern.STATIC matching here because my intent was
        // to test the wildcarding, and we don't do DYNAMIC matching on wildcarded things.                        
                                
		TypePatternList p = makeArgumentsPattern(pattern);
        ResolvedType[] types = new ResolvedType[names.length];
        for (int i = 0; i < names.length; i++) {
            types[i] = world.resolve(names[i]);
        }
        
        p.resolveBindings(makeTestScope(), Bindings.NONE, false, false);
		//System.out.println("type: " + type);
		FuzzyBoolean result = p.matches(types, TypePattern.STATIC);
		String msg = "matches statically " + pattern + " to " + Arrays.asList(types);
        assertEquals(msg, shouldMatchStatically, result);       
	}
	
	private TestScope makeTestScope() {
		TestScope scope = new TestScope(CollectionUtil.NO_STRINGS, CollectionUtil.NO_STRINGS, world);
		return scope;
	}
	    
    public void stupidCheck(String pattern, boolean[] matches) {
        TypePatternList p = makeArgumentsPattern(pattern);
        p.resolveBindings(makeTestScope(), Bindings.NONE, false, false);
        
        int len = matches.length;
        
        for (int j = 0; j < len; j++) {
            
            ResolvedType[] types = new ResolvedType[j];
            for (int i = 0; i < j; i++) {
                types[i] = world.resolve("int");
            }
       
            FuzzyBoolean result = p.matches(types, TypePattern.STATIC);
            String msg = "matches statically " + pattern + " to " + Arrays.asList(types);
            assertEquals(msg, FuzzyBoolean.fromBoolean(matches[j]), result);   
        }
    }     
    
	public void testSerialization() throws IOException {
		String[] patterns = new String[] {
            "( ..,  ..,  .., int)", 
            "( ..,  .., int,  ..)", 
            "( ..,  .., int, int)", 
            "( .., int,  ..,  ..)", 
            "( .., int,  .., int)", 
            "( .., int, int,  ..)", 
            "( .., int, int, int)", 
            
            "(int,  ..,  ..,  ..)", 
            "(int,  ..,  .., int)", 
            "(int,  .., int,  ..)", 
            "(int,  .., int, int)", 
            "(int, int,  ..,  ..)", 
            "(int, int,  .., int)", 
            "(int, int, int,  ..)", 
            "(int, int, int, int)"
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
		TypePatternList p = makeArgumentsPattern(string);
		ByteArrayOutputStream bo = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(bo);
		p.write(out);
		out.close();
		
		ByteArrayInputStream bi = new ByteArrayInputStream(bo.toByteArray());
		VersionedDataInputStream in = new VersionedDataInputStream(bi);
		TypePatternList newP = TypePatternList.read(in, null);
		
		assertEquals("write/read", p, newP);	
	}
	
}
