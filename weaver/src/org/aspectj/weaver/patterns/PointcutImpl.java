/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.aspectj.weaver.patterns;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.JoinPoint.StaticPart;
import org.aspectj.lang.reflect.Pointcut;
import org.aspectj.util.FuzzyBoolean;

/**
 * Implementation of the reflective pointcut interface in org.aspectj.lang.reflect
 * (not to be confused with the Pointcut class in this package, which itself should 
 * be considered part of the weaver implementation).
 */
public class PointcutImpl implements Pointcut {

    private String pointcutExpression;
    private org.aspectj.weaver.patterns.Pointcut pointcut;
    
    public PointcutImpl(String pointcutExpression) {
        this.pointcutExpression = pointcutExpression;
        try {
           pointcut = new PatternParser(pointcutExpression).parsePointcut(); 
        } catch (ParserException pEx) {
            throw new IllegalArgumentException(pEx.getMessage());
        }
    }
    
    /* (non-Javadoc)
     * @see org.aspectj.lang.reflect.Pointcut#matches(org.aspectj.lang.JoinPoint, org.aspectj.lang.JoinPoint.StaticPart)
     */
    public boolean matches(JoinPoint jp, StaticPart enclosingJoinPoint) {
        return pointcut.match(jp,enclosingJoinPoint) == FuzzyBoolean.YES;
    }

    /* (non-Javadoc)
     * @see org.aspectj.lang.reflect.Pointcut#getPointcutExpression()
     */
    public String getPointcutExpression() {
        return pointcutExpression;
    }

}
