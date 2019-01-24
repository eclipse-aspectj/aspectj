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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.aspectj.weaver.CompressingDataOutputStream;
import org.aspectj.weaver.VersionedDataInputStream;
import org.aspectj.weaver.World;
import org.aspectj.weaver.reflect.ReflectionWorld;

/**
 * @author hugunin
 * 
 */
public class AndOrNotTestCase extends PatternsTestCase {

	public World getWorld() {
		return new ReflectionWorld(this.getClass().getClassLoader());
	}

	public void testMatchBooleanOperatorPointcutMatching() throws IOException {

		Pointcut foo = makePointcut("this(Foo)");
		Pointcut bar = makePointcut("this(Bar)");
		Pointcut c = makePointcut("this(C)");

		checkEquals("this(Foo) && this(Bar)", new AndPointcut(foo, bar));
		checkEquals("this(Foo) && this(Bar) && this(C)", new AndPointcut(foo, new AndPointcut(bar, c)));

		checkEquals("this(Foo) || this(Bar)", new OrPointcut(foo, bar));
		checkEquals("this(Foo) || this(Bar) || this(C)", new OrPointcut(foo, new OrPointcut(bar, c)));

		checkEquals("this(Foo) && this(Bar) || this(C)", new OrPointcut(new AndPointcut(foo, bar), c));
		checkEquals("this(Foo) || this(Bar) && this(C)", new OrPointcut(foo, new AndPointcut(bar, c)));
		checkEquals("(this(Foo) || this(Bar)) && this(C)", new AndPointcut(new OrPointcut(foo, bar), c));
		checkEquals("this(Foo) || (this(Bar) && this(C))", new OrPointcut(foo, new AndPointcut(bar, c)));

		checkEquals("!this(Foo)", new NotPointcut(foo));
		checkEquals("!this(Foo) && this(Bar)", new AndPointcut(new NotPointcut(foo), bar));
		checkEquals("!(this(Foo) && this(Bar)) || this(C)", new OrPointcut(new NotPointcut(new AndPointcut(foo, bar)), c));
		checkEquals("!!this(Foo)", new NotPointcut(new NotPointcut(foo)));
	}

	private static class Foo {
	}

	private static class Bar {
	}

	private static class C {
	}

	static {
		new Foo();
		new Bar();
		new C(); // just to touch them and so eclipse thinks they are used
	}

	private Pointcut makePointcut(String pattern) {
		return new PatternParser(pattern).parsePointcut();
	}

	private void checkEquals(String pattern, Pointcut p) throws IOException {
		assertEquals(pattern, p, makePointcut(pattern));
		checkSerialization(pattern);
	}

	private void checkSerialization(String string) throws IOException {
		Pointcut p = makePointcut(string);
		ByteArrayOutputStream bo = new ByteArrayOutputStream();
		ConstantPoolSimulator cps = new ConstantPoolSimulator();
		CompressingDataOutputStream out = new CompressingDataOutputStream(bo, cps);
		p.write(out);
		out.close();

		ByteArrayInputStream bi = new ByteArrayInputStream(bo.toByteArray());
		VersionedDataInputStream in = new VersionedDataInputStream(bi, cps);
		Pointcut newP = Pointcut.read(in, null);

		assertEquals("write/read", p, newP);
	}

}
