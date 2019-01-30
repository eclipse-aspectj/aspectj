/* *******************************************************************
 * Copyright (c) 2003 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Mik Kersten     initial implementation 
 * ******************************************************************/

package org.aspectj.tools.ajdoc;

import java.io.File;

/**
 * A long way to go until full coverage, but this is the place to add more.
 * 
 * @author Mik Kersten
 */
public class PatternsTestCase extends AjdocTestCase {
	
	public void testSimpleExample() {
		  
//		System.err.println(new File("testdata.figures-demo").exists());
//		File file1 = new File("testdata/patterns/allPatterns.lst");
		File outdir = new File("testdata/patterns/doc");
		File srcdir = new File("../../docs/sandbox/ubc-design-patterns/src");
		
		String[] args = { 
//			"-XajdocDebug", 
                "-classpath",
                AjdocTests.ASPECTJRT_PATH.getPath(),
			"-d", 
			outdir.getAbsolutePath(),
			"-sourcepath", 
			srcdir.getAbsolutePath(),
			"ca.ubc.cs.spl.aspectPatterns.patternLibrary",
			"ca.ubc.cs.spl.aspectPatterns.examples.abstractFactory.java",
			"ca.ubc.cs.spl.aspectPatterns.examples.abstractFactory.aspectj",
			"ca.ubc.cs.spl.aspectPatterns.examples.builder.java",
			"ca.ubc.cs.spl.aspectPatterns.examples.builder.aspectj",
			"ca.ubc.cs.spl.aspectPatterns.examples.factoryMethod.java",
			"ca.ubc.cs.spl.aspectPatterns.examples.factoryMethod.aspectj",
			"ca.ubc.cs.spl.aspectPatterns.examples.prototype.java",
			"ca.ubc.cs.spl.aspectPatterns.examples.prototype.aspectj",
			"ca.ubc.cs.spl.aspectPatterns.examples.singleton.java",
			"ca.ubc.cs.spl.aspectPatterns.examples.singleton.aspectj",
			"ca.ubc.cs.spl.aspectPatterns.examples.adapter.java",
			"ca.ubc.cs.spl.aspectPatterns.examples.adapter.aspectj",
			"ca.ubc.cs.spl.aspectPatterns.examples.bridge.java",
			"ca.ubc.cs.spl.aspectPatterns.examples.bridge.aspectj",
			"ca.ubc.cs.spl.aspectPatterns.examples.composite.java",
			"ca.ubc.cs.spl.aspectPatterns.examples.composite.aspectj",
			"ca.ubc.cs.spl.aspectPatterns.examples.decorator.java",
			"ca.ubc.cs.spl.aspectPatterns.examples.decorator.aspectj",
			"ca.ubc.cs.spl.aspectPatterns.examples.facade.java",
			"ca.ubc.cs.spl.aspectPatterns.examples.facade.aspectj",
			"ca.ubc.cs.spl.aspectPatterns.examples.flyweight.java",
			"ca.ubc.cs.spl.aspectPatterns.examples.flyweight.aspectj",
			"ca.ubc.cs.spl.aspectPatterns.examples.proxy.java",
			"ca.ubc.cs.spl.aspectPatterns.examples.proxy.aspectj",
			"ca.ubc.cs.spl.aspectPatterns.examples.chainOfResponsibility.java",
			"ca.ubc.cs.spl.aspectPatterns.examples.chainOfResponsibility.aspectj",
			"ca.ubc.cs.spl.aspectPatterns.examples.command.java",
			"ca.ubc.cs.spl.aspectPatterns.examples.command.aspectj",
			"ca.ubc.cs.spl.aspectPatterns.examples.interpreter.java",
			"ca.ubc.cs.spl.aspectPatterns.examples.interpreter.aspectj",
			"ca.ubc.cs.spl.aspectPatterns.examples.iterator.java",
			"ca.ubc.cs.spl.aspectPatterns.examples.iterator.aspectj",
			"ca.ubc.cs.spl.aspectPatterns.examples.mediator.java",
			"ca.ubc.cs.spl.aspectPatterns.examples.mediator.aspectj",
			"ca.ubc.cs.spl.aspectPatterns.examples.memento.java",
			"ca.ubc.cs.spl.aspectPatterns.examples.memento.aspectj",
			"ca.ubc.cs.spl.aspectPatterns.examples.observer.java",
			"ca.ubc.cs.spl.aspectPatterns.examples.observer.aspectj",
			"ca.ubc.cs.spl.aspectPatterns.examples.state.java",
			"ca.ubc.cs.spl.aspectPatterns.examples.state.aspectj",
			"ca.ubc.cs.spl.aspectPatterns.examples.strategy.java",
			"ca.ubc.cs.spl.aspectPatterns.examples.strategy.aspectj",
			"ca.ubc.cs.spl.aspectPatterns.examples.templateMethod.java",
			"ca.ubc.cs.spl.aspectPatterns.examples.templateMethod.aspectj",
			"ca.ubc.cs.spl.aspectPatterns.examples.visitor.java",
			"ca.ubc.cs.spl.aspectPatterns.examples.visitor.aspectj"
		};
		
		org.aspectj.tools.ajdoc.Main.main(args);
	}
	
}
