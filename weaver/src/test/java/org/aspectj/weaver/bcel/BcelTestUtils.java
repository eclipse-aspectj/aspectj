/* *******************************************************************
 * Copyright (c) 2008 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Andy Clement     initial implementation 
 * ******************************************************************/
package org.aspectj.weaver.bcel;

import org.aspectj.weaver.Advice;
import org.aspectj.weaver.AdviceKind;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.TestUtils;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.World;
import org.aspectj.weaver.patterns.FormalBinding;
import org.aspectj.weaver.patterns.Pointcut;
import org.aspectj.weaver.patterns.SimpleScope;

public class BcelTestUtils {
	/**
	 * Moved from BcelWorld to here
	 * 
	 * Parse a string into advice.
	 * 
	 * <blockquote>
	 * 
	 * <pre>
	 * Kind ( Id , ... ) : Pointcut -&gt; MethodSignature
	 * </pre>
	 * 
	 * </blockquote>
	 */
	public static Advice shadowMunger(World w, String str, int extraFlag) {
		str = str.trim();
		int start = 0;
		int i = str.indexOf('(');
		AdviceKind kind = AdviceKind.stringToKind(str.substring(start, i));
		start = ++i;
		i = str.indexOf(')', i);
		String[] ids = TestUtils.parseIds(str.substring(start, i).trim());
		// start = ++i;

		i = str.indexOf(':', i);
		start = ++i;
		i = str.indexOf("->", i);
		Pointcut pointcut = Pointcut.fromString(str.substring(start, i).trim());
		Member m = TestUtils.methodFromString(str.substring(i + 2, str.length()).trim());

		// now, we resolve
		UnresolvedType[] types = m.getParameterTypes();
		FormalBinding[] bindings = new FormalBinding[ids.length];
		for (int j = 0, len = ids.length; j < len; j++) {
			bindings[j] = new FormalBinding(types[j], ids[j], j, 0, 0);
		}

		Pointcut p = pointcut.resolve(new SimpleScope(w, bindings));

		return new BcelAdvice(kind, p, m, extraFlag, 0, 0, null, null);
	}
}
