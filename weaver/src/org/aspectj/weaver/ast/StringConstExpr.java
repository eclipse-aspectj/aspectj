/*******************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/
package org.aspectj.weaver.ast;

import org.aspectj.weaver.ast.Expr;
import org.aspectj.weaver.ast.IExprVisitor;
import org.aspectj.weaver.ResolvedTypeX;

/**
 * Note: Used when aspectOf is not existing in the aspect class (no pre-processing of aspects)
 *
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class StringConstExpr extends Expr {
    private String m_stringConst;
    private ResolvedTypeX m_inAspect;
    public StringConstExpr(String stringConst, ResolvedTypeX inAspect) {
        super();
        m_stringConst = stringConst;
        m_inAspect = inAspect;//TODO useless
    }

    public void accept(IExprVisitor v) {
        v.visit(this);
    }

    public ResolvedTypeX getType() {
        throw new RuntimeException("not supported");
    }

    public String getStringConst() {
        return m_stringConst;
    }
}
