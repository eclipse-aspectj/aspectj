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
package org.aspectj.weaver.tools;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.aspectj.weaver.internal.tools.PointcutExpressionImpl;
import org.aspectj.weaver.patterns.AndPointcut;
import org.aspectj.weaver.patterns.NotPointcut;
import org.aspectj.weaver.patterns.OrPointcut;
import org.aspectj.weaver.patterns.ParserException;
import org.aspectj.weaver.patterns.PatternParser;
import org.aspectj.weaver.patterns.Pointcut;
import org.aspectj.weaver.patterns.ThisOrTargetPointcut;

/**
 * A PointcutParser can be used to build PointcutExpressions for a 
 * user-defined subset of AspectJ's pointcut language
 */
public class PointcutParser {
    
    private Set supportedPrimitives; 
    
    /**
     * @return a Set containing every PointcutPrimitive except
     * if, cflow, and cflowbelow (useful for passing to 
     * PointcutParser constructor).
     */
    public static Set getAllSupportedPointcutPrimitives() {
        Set primitives = new HashSet();
        primitives.add(PointcutPrimitives.ADVICE_EXECUTION);
        primitives.add(PointcutPrimitives.ARGS);
        primitives.add(PointcutPrimitives.CALL);
        primitives.add(PointcutPrimitives.EXECUTION);
        primitives.add(PointcutPrimitives.GET);
        primitives.add(PointcutPrimitives.HANDLER);
        primitives.add(PointcutPrimitives.INITIALIZATION);
        primitives.add(PointcutPrimitives.PRE_INITIALIZATION);
        primitives.add(PointcutPrimitives.SET);
        primitives.add(PointcutPrimitives.STATIC_INITIALIZATION);
        primitives.add(PointcutPrimitives.TARGET);
        primitives.add(PointcutPrimitives.THIS);
        primitives.add(PointcutPrimitives.WITHIN);
        primitives.add(PointcutPrimitives.WITHIN_CODE);
        return primitives;
    }
    
    /**
     * Create a pointcut parser that can parse the full AspectJ pointcut
     * language with the following exceptions:
     * <ul>
     * <li>The <code>if, cflow, and cflowbelow</code> pointcut designators are not supported
     * <li>Pointcut expressions must be self-contained :- they cannot contain references
     * to other named pointcuts
     * <li>The pointcut expression must be anonymous with no formals allowed.
     * </ul>
     */
    public PointcutParser() {
        supportedPrimitives = getAllSupportedPointcutPrimitives();
    }
    
    /**
     * Create a pointcut parser that can parse pointcut expressions built
     * from a user-defined subset of AspectJ's supported pointcut primitives. 
     * The following restrictions apply:
     * <ul>
     * <li>The <code>if, cflow, and cflowbelow</code> pointcut designators are not supported
     * <li>Pointcut expressions must be self-contained :- they cannot contain references
     * to other named pointcuts
     * <li>The pointcut expression must be anonymous with no formals allowed.
     * </ul>
     * @param supportedPointcutKinds a set of PointcutPrimitives this parser
     * should support
     * @throws UnsupportedOperationException if the set contains if, cflow, or
     * cflow below
     */
    public PointcutParser(Set/*<PointcutPrimitives>*/ supportedPointcutKinds) {
        supportedPrimitives = supportedPointcutKinds;
        for (Iterator iter = supportedPointcutKinds.iterator(); iter.hasNext();) {
            PointcutPrimitives element = (PointcutPrimitives) iter.next();
            if ((element == PointcutPrimitives.IF) ||
                (element == PointcutPrimitives.CFLOW) ||
                (element == PointcutPrimitives.CFLOW_BELOW)) {
                throw new UnsupportedOperationException("Cannot handle if, cflow, and cflowbelow primitives"); 
            }
        }
    }
    

    /**
     * Parse the given pointcut expression.
     * @throws UnsupportedOperationException if the parser encounters a 
     * primitive pointcut expression of a kind not supported by this PointcutParser.
     * @throws IllegalArgumentException if the expression is not a well-formed 
     * pointcut expression
     */
    public PointcutExpression parsePointcutExpression(String expression)
    throws UnsupportedOperationException, IllegalArgumentException {
        PointcutExpressionImpl pcExpr = null;
        try {
            Pointcut pc = new PatternParser(expression).parsePointcut();
            validateAgainstSupportedPrimitives(pc);
            pcExpr = new PointcutExpressionImpl(pc);
        } catch (ParserException pEx) {
            throw new IllegalArgumentException(pEx.getMessage());
        }
        return pcExpr;
    }
    
    private void validateAgainstSupportedPrimitives(Pointcut pc) {
        switch(pc.getPointcutKind()) {
        	case Pointcut.AND:
        	   validateAgainstSupportedPrimitives(((AndPointcut)pc).getLeft());
        	   validateAgainstSupportedPrimitives(((AndPointcut)pc).getRight());
        	   break;
        	case Pointcut.ARGS:
        	    if (!supportedPrimitives.contains(PointcutPrimitives.ARGS))
        	        throw new UnsupportedOperationException("args is not supported by this parser");
        	    break;
        	case Pointcut.CFLOW:
        	        throw new UnsupportedOperationException("cflow and cflowbelow are not supported by this parser");
        	case Pointcut.HANDLER:
        	    if (!supportedPrimitives.contains(PointcutPrimitives.HANDLER))
        	        throw new UnsupportedOperationException("handler is not supported by this parser");
        	    break;
        	case Pointcut.IF:
        	case Pointcut.IF_FALSE:
        	case Pointcut.IF_TRUE:
        	    throw new UnsupportedOperationException("if is not supported by this parser");        	    
        	case Pointcut.KINDED:
        	    break;
        	case Pointcut.NOT:
        	    validateAgainstSupportedPrimitives(((NotPointcut)pc).getNegatedPointcut());
        	    break;
        	case Pointcut.OR:
         	    validateAgainstSupportedPrimitives(((OrPointcut)pc).getLeft());
        		validateAgainstSupportedPrimitives(((OrPointcut)pc).getRight());
        	    break;
        	case Pointcut.REFERENCE:
        	    throw new UnsupportedOperationException("reference pointcuts are not supported by this parser");
        	case Pointcut.THIS_OR_TARGET:
        	    boolean isThis = ((ThisOrTargetPointcut)pc).isThis();
        		if (isThis && !supportedPrimitives.contains(PointcutPrimitives.THIS)) {
        		    throw new UnsupportedOperationException("this is not supported by this parser");
        		} else if (!supportedPrimitives.contains(PointcutPrimitives.TARGET)) {
        		    throw new UnsupportedOperationException("target is not supported by this parser");
        		}
        	    break;
        	case Pointcut.WITHIN:
        	    if (!supportedPrimitives.contains(PointcutPrimitives.WITHIN))
        	        throw new UnsupportedOperationException("within is not supported by this parser");
        	    break;
        	case Pointcut.WITHINCODE:
        	    if (!supportedPrimitives.contains(PointcutPrimitives.WITHIN_CODE))
        	        throw new UnsupportedOperationException("withincode is not supported by this parser");
        	    break;
        	case Pointcut.NONE:  // deliberate fall-through
        	default:
        	    throw new UnsupportedOperationException("Unknown pointcut kind: " + pc.getPointcutKind());
        }
    }
}
