/*******************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/
package org.aspectj.weaver.ast;

import org.aspectj.weaver.ResolvedTypeX;

/**
 * Represents a cast expression.
 * <p/>
 * Used when aspectOf is not existing in the aspect class (no pre-processing of aspects) ie when
 * Object Aspects.aspectOf(..) API is used.
 *
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class CastExpr extends Expr {

    private String m_castToTypeName;

    private CallExpr m_expr;

    public CastExpr(CallExpr expr, String castToSignature) {
        super();
        m_expr = expr;
        m_castToTypeName = castToSignature;
    }

    public void accept(IExprVisitor v) {
        v.visit(m_expr);
        v.visit(this);
    }

    public String getTypeName() {
        return m_castToTypeName;
    }

    public ResolvedTypeX getType() {
        throw new RuntimeException("not supported");
    }
}
