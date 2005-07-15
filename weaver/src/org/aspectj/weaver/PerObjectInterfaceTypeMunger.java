/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Common Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     PARC                 initial implementation
 *     Alexandre Vasseur    rearchitected for #75442 finer grained matching
 * ******************************************************************/
package org.aspectj.weaver;

import org.aspectj.weaver.patterns.PerFromSuper;
import org.aspectj.weaver.patterns.PerObject;
import org.aspectj.weaver.patterns.PerThisOrTargetPointcutVisitor;
import org.aspectj.weaver.patterns.Pointcut;
import org.aspectj.weaver.patterns.TypePattern;

import java.io.DataOutputStream;
import java.io.IOException;

public class PerObjectInterfaceTypeMunger extends ResolvedTypeMunger {

    private final UnresolvedType interfaceType;
    private final Pointcut testPointcut;
    private TypePattern lazyTestTypePattern;

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
        }
        return lazyTestTypePattern;
    }

    public void write(DataOutputStream s) throws IOException {
        throw new RuntimeException("shouldn't be serialized");
    }

    public UnresolvedType getInterfaceType() {
        return interfaceType;
    }

    public Pointcut getTestPointcut() {
        return testPointcut;
    }

    public boolean matches(ResolvedType matchType, ResolvedType aspectType) {
        if (matchType.isInterface()) return false;
        return getTestTypePattern(aspectType).matchesStatically(matchType);
    }
}
