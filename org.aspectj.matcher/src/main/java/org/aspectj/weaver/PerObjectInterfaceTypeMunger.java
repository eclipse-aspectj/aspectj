/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     PARC                 initial implementation
 *     Alexandre Vasseur    rearchitected for #75442 finer grained matching
 * ******************************************************************/
package org.aspectj.weaver;

import java.io.IOException;

import org.aspectj.weaver.patterns.PerFromSuper;
import org.aspectj.weaver.patterns.PerObject;
import org.aspectj.weaver.patterns.PerThisOrTargetPointcutVisitor;
import org.aspectj.weaver.patterns.Pointcut;
import org.aspectj.weaver.patterns.TypePattern;

public class PerObjectInterfaceTypeMunger extends ResolvedTypeMunger {

	private final UnresolvedType interfaceType;
	private final Pointcut testPointcut;
	private TypePattern lazyTestTypePattern;

	public boolean equals(Object other) {
		if (other == null || !(other instanceof PerObjectInterfaceTypeMunger)) {
			return false;
		}
		PerObjectInterfaceTypeMunger o = (PerObjectInterfaceTypeMunger) other;
		return ((testPointcut == null) ? (o.testPointcut == null) : testPointcut.equals(o.testPointcut))
				&& ((lazyTestTypePattern == null) ? (o.lazyTestTypePattern == null) : lazyTestTypePattern
						.equals(o.lazyTestTypePattern));
	}

	private volatile int hashCode = 0;

	public int hashCode() {
		if (hashCode == 0) {
			int result = 17;
			result = 37 * result + ((testPointcut == null) ? 0 : testPointcut.hashCode());
			result = 37 * result + ((lazyTestTypePattern == null) ? 0 : lazyTestTypePattern.hashCode());
			hashCode = result;
		}
		return hashCode;
	}

	public PerObjectInterfaceTypeMunger(UnresolvedType aspectType, Pointcut testPointcut) {
		super(PerObjectInterface, null);
		this.testPointcut = testPointcut;
		this.interfaceType = AjcMemberMaker.perObjectInterfaceType(aspectType);
	}

	private TypePattern getTestTypePattern(ResolvedType aspectType) {
		if (lazyTestTypePattern == null) {
			final boolean isPerThis;
			if (aspectType.getPerClause() instanceof PerFromSuper) {
				PerFromSuper ps = (PerFromSuper) aspectType.getPerClause();
				isPerThis = ((PerObject) ps.lookupConcretePerClause(aspectType)).isThis();
			} else {
				isPerThis = ((PerObject) aspectType.getPerClause()).isThis();
			}
			PerThisOrTargetPointcutVisitor v = new PerThisOrTargetPointcutVisitor(!isPerThis, aspectType);
			lazyTestTypePattern = v.getPerTypePointcut(testPointcut);
			// reset hashCode so that its recalculated with the new lazyTestTypePattern
			hashCode = 0;
		}
		return lazyTestTypePattern;
	}

	public void write(CompressingDataOutputStream s) throws IOException {
		throw new RuntimeException("shouldn't be serialized");
	}

	public UnresolvedType getInterfaceType() {
		return interfaceType;
	}

	public Pointcut getTestPointcut() {
		return testPointcut;
	}

	public boolean matches(ResolvedType matchType, ResolvedType aspectType) {
		if (matchType.isInterface()) {
			return false;
		}
		return getTestTypePattern(aspectType).matchesStatically(matchType);
	}

	public boolean isLateMunger() {
		return true;
	}
}
