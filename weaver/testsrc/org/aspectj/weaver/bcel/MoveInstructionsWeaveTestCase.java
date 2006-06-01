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

import java.io.*;

import org.aspectj.apache.bcel.generic.InstructionFactory;
import org.aspectj.weaver.*;

public class MoveInstructionsWeaveTestCase extends WeaveTestCase {
	{
		regenerate = false;
	}

	public MoveInstructionsWeaveTestCase(String name) {
		super(name);
	}
    public void testHello() throws IOException {
        BcelAdvice p = new BcelAdvice(null, makePointcutAll(), null, 0, -1, -1, null, null) {
            public void specializeOn(Shadow s) {
            	super.specializeOn(s);
                ((BcelShadow) s).initializeForAroundClosure();
            }
			public void implementOn(Shadow s) {
				BcelShadow shadow = (BcelShadow) s;
				LazyMethodGen newMethod =
					shadow.extractMethod(
						shadow.getSignature().getExtractableName() + "_extracted",
						0,
						this);
				shadow.getRange().append(shadow.makeCallToCallback(newMethod));

				if (!shadow.isFallsThrough()) {
					shadow.getRange().append(
						InstructionFactory.createReturn(newMethod.getReturnType()));
				}
			}
        };

        weaveTest("HelloWorld", "ExtractedHelloWorld", p);
    }  
   
    static int counter = 0;
	public void testFancyHello() throws IOException {
		BcelAdvice p = new BcelAdvice(null, makePointcutAll(), null, 0, -1, -1, null, null) {
            public void specializeOn(Shadow s) {
            	super.specializeOn(s);
                ((BcelShadow) s).initializeForAroundClosure();
            }
            public void implementOn(Shadow s) {
                BcelShadow shadow = (BcelShadow) s;
                LazyMethodGen newMethod = shadow.extractMethod(shadow.getSignature().getExtractableName() + "_extracted" + counter++, 0, this);
                shadow.getRange().append(shadow.makeCallToCallback(newMethod));

                if (! shadow.isFallsThrough()) {
                    shadow.getRange().append(InstructionFactory.createReturn(newMethod.getReturnType()));
                }
            }
  		};

		weaveTest("FancyHelloWorld", "ExtractedFancyHelloWorld", p);
	}
}
