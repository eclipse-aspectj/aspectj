/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.weaver.bcel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.bcel.Constants;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.Type;
import org.aspectj.weaver.AdviceKind;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.TypeX;

/**.
 */
public class ArgsWeaveTestCase extends WeaveTestCase {
	{
		regenerate = false;
	}

	public ArgsWeaveTestCase(String name) {
		super(name);
	}
    

    public void testAfterReturningArgs() throws IOException {
        weaveTest("HelloWorld", "ArgsAfterReturningHelloWorld", makeArgsMunger("afterReturning"));
    }  


    public void testFancyAfterReturningArgs() throws IOException {
        weaveTest("FancyHelloWorld", "ArgsAfterReturningFancyHelloWorld", makeArgsMunger("afterReturning"));
    }

    public void testThrowing() throws IOException {
        weaveTest("HelloWorld", "ArgsAfterThrowingHelloWorld", makeArgsMunger("afterThrowing"));
    }  

    public void testLots() throws IOException {
        List l = new ArrayList();
        
        
        BcelAdvice p1 = 
            makeArgsMunger("before");

        BcelAdvice p2 = 
            makeArgsMunger("afterThrowing");
        
        BcelAdvice p3 = 
            makeArgsMunger("afterReturning");

        l.add(p1);        
        l.add(p2);
        l.add(p3);


        weaveTest("HelloWorld", "ArgsBeforeAfterHelloWorld", addLexicalOrder(l));        
    }    

	private InstructionList getArgsAdviceTag(BcelShadow shadow, String where) {
		String methodName =
			"ajc_" + where + "_" + shadow.getKind().toLegalJavaIdentifier();
		InstructionFactory fact = shadow.getFactory();
		InstructionList il = new InstructionList();
        

        il.append(
            BcelRenderer.renderExpr(
                fact, 
                new BcelWorld(), 
                shadow.getArgVar(0),
                Type.OBJECT));
        
        il.append(
            fact.createInvoke(
                "Aspect", 
                methodName, 
                Type.VOID, 
                new Type[] { Type.OBJECT }, 
                Constants.INVOKESTATIC));
                
		return il;
	}
    
    private BcelAdvice makeArgsMunger(final String kindx) {
        return new BcelAdvice(AdviceKind.stringToKind(kindx), makePointcutNoZeroArg(),
        			Member.method(TypeX.forName("Aspect"), 0, "foo", "()V"), 0, -1, -1, null,
        			world.resolve(TypeX.forName("Aspect"))) {
            public void specializeOn(Shadow shadow) {
                super.specializeOn(shadow);
                shadow.getArgVar(0);
            }
            public InstructionList getAdviceInstructions(BcelShadow shadow, BcelVar extraVar, InstructionHandle fk) {
                return getArgsAdviceTag(shadow, kindx);
            }
        };    	
    } 

}
