/*******************************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * initial implementation              Alexandre Vasseur
 *******************************************************************************/
package org.aspectj.weaver.ast;

import org.aspectj.weaver.ast.Expr;
import org.aspectj.weaver.ast.IExprVisitor;
import org.aspectj.weaver.ResolvedType;

/**
 * Represents a String constant instruction.
 * <p/>
 * Used when aspectOf is not existing in the aspect class (no pre-processing of aspects)
 *
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class StringConstExpr extends Expr {

    private String m_stringConst;

    public StringConstExpr(String stringConst) {
        super();
        m_stringConst = stringConst;
    }

    public void accept(IExprVisitor v) {
        v.visit(this);
    }

    public ResolvedType getType() {
        throw new RuntimeException("not supported");
    }

    public String getStringConst() {
        return m_stringConst;
    }
}
