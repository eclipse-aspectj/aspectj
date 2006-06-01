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


package org.aspectj.weaver.bcel;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.*;

import org.aspectj.weaver.*;

public class AroundWeaveTestCase extends WeaveTestCase {
	{
		regenerate = false;
	}

	public AroundWeaveTestCase(String name) {
		super(name);
	}
	
	public void testAround() throws IOException {
		aroundTest("Around", true);
	}
	
	public void testAroundAll() throws IOException {
		aroundTest("AroundAll", false);
	}
	
    public void testAroundAndOthers() throws IOException {
    	aroundTestAndOthers("AroundAndOthers", true);
    }

    public void testAroundAllAndOthers() throws IOException {
    	aroundTestAndOthers("AroundAllAndOthers", false);
    }


    private BcelAdvice makeAroundMunger(final boolean matchOnlyPrintln) {
        BcelWorld world = super.world;
        final Member sig =
            MemberImpl.method(
                UnresolvedType.forName("Aspect"),
                Modifier.STATIC,
                "ajc_around",
                "(Lorg/aspectj/runtime/internal/AroundClosure;)Ljava/lang/Object;");
        
        return new BcelAdvice(
        	AdviceKind.stringToKind("around"), 
        	matchOnlyPrintln ? makePointcutPrintln() : makePointcutAll(),
	        sig, 0, -1, -1, null, UnresolvedType.forName("Aspect").resolve(world))
	    {
            public void specializeOn(Shadow s) {
            	super.specializeOn(s);
                ((BcelShadow) s).initializeForAroundClosure();
            }
        };    
    }  

	private void aroundTest(String outName, final boolean matchOnlyPrintln) throws IOException {
		weaveTest(getStandardTargets(), outName, makeAroundMunger(matchOnlyPrintln));
	}  

    private void aroundTestAndOthers(String outName, final boolean matchOnlyPrintln)
            throws IOException 
    {
                
        List l = new ArrayList();

		// the afterReturning was taken out to avoid circular advice dependency        

        l.addAll(makeAdviceAll("before", matchOnlyPrintln));
        //l.addAll(makeAdviceAll("afterReturning", matchOnlyPrintln));

        l.add(makeAroundMunger(matchOnlyPrintln));

        l.addAll(makeAdviceAll("before", matchOnlyPrintln));
        //l.addAll(makeAdviceAll("afterReturning", matchOnlyPrintln));

        l.add(makeAroundMunger(matchOnlyPrintln));
        weaveTest(getStandardTargets(), outName, addLexicalOrder(l));
    } 

}
