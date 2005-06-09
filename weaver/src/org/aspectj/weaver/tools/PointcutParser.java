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

import org.aspectj.weaver.IHasPosition;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.internal.tools.PointcutExpressionImpl;
import org.aspectj.weaver.patterns.AndPointcut;
import org.aspectj.weaver.patterns.CflowPointcut;
import org.aspectj.weaver.patterns.KindedPointcut;
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
        primitives.add(PointcutPrimitive.ADVICE_EXECUTION);
        primitives.add(PointcutPrimitive.ARGS);
        primitives.add(PointcutPrimitive.CALL);
        primitives.add(PointcutPrimitive.EXECUTION);
        primitives.add(PointcutPrimitive.GET);
        primitives.add(PointcutPrimitive.HANDLER);
        primitives.add(PointcutPrimitive.INITIALIZATION);
        primitives.add(PointcutPrimitive.PRE_INITIALIZATION);
        primitives.add(PointcutPrimitive.SET);
        primitives.add(PointcutPrimitive.STATIC_INITIALIZATION);
        primitives.add(PointcutPrimitive.TARGET);
        primitives.add(PointcutPrimitive.THIS);
        primitives.add(PointcutPrimitive.WITHIN);
        primitives.add(PointcutPrimitive.WITHIN_CODE);
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
            PointcutPrimitive element = (PointcutPrimitive) iter.next();
            if ((element == PointcutPrimitive.IF) ||
                (element == PointcutPrimitive.CFLOW) ||
                (element == PointcutPrimitive.CFLOW_BELOW)) {
                throw new UnsupportedOperationException("Cannot handle if, cflow, and cflowbelow primitives"); 
            }
        }
    }
    

    /**
     * Parse the given pointcut expression.
     * @throws UnsupportedPointcutPrimitiveException if the parser encounters a 
     * primitive pointcut expression of a kind not supported by this PointcutParser.
     * @throws IllegalArgumentException if the expression is not a well-formed 
     * pointcut expression
     */
    public PointcutExpression parsePointcutExpression(String expression)
    throws UnsupportedPointcutPrimitiveException, IllegalArgumentException {
        PointcutExpressionImpl pcExpr = null;
        try {
            Pointcut pc = new PatternParser(expression).parsePointcut();
            validateAgainstSupportedPrimitives(pc,expression);
            pc.resolve();
            pcExpr = new PointcutExpressionImpl(pc,expression);
        } catch (ParserException pEx) {
            throw new IllegalArgumentException(buildUserMessageFromParserException(expression,pEx));
        }
        return pcExpr;
    }
    
    /* for testing */
    Set getSupportedPrimitives() {
    	return supportedPrimitives;
    }
    
    private void validateAgainstSupportedPrimitives(Pointcut pc, String expression) {
        switch(pc.getPointcutKind()) {
        	case Pointcut.AND:
        	   validateAgainstSupportedPrimitives(((AndPointcut)pc).getLeft(),expression);
        	   validateAgainstSupportedPrimitives(((AndPointcut)pc).getRight(),expression);
        	   break;
        	case Pointcut.ARGS:
        	    if (!supportedPrimitives.contains(PointcutPrimitive.ARGS))
        	        throw new UnsupportedPointcutPrimitiveException(expression, PointcutPrimitive.ARGS);
        	    break;
        	case Pointcut.CFLOW:
					CflowPointcut cfp = (CflowPointcut) pc;
					if (cfp.isCflowBelow()) {
	        	        throw new UnsupportedPointcutPrimitiveException(expression, PointcutPrimitive.CFLOW_BELOW);												
					} else {
	        	        throw new UnsupportedPointcutPrimitiveException(expression, PointcutPrimitive.CFLOW);						
					}
        	case Pointcut.HANDLER:
        	    if (!supportedPrimitives.contains(PointcutPrimitive.HANDLER))
        	        throw new UnsupportedPointcutPrimitiveException(expression, PointcutPrimitive.HANDLER);
        	    break;
        	case Pointcut.IF:
        	case Pointcut.IF_FALSE:
        	case Pointcut.IF_TRUE:
        	    throw new UnsupportedPointcutPrimitiveException(expression, PointcutPrimitive.IF);       	    
        	case Pointcut.KINDED:
        		validateKindedPointcut(((KindedPointcut)pc),expression);
        	    break;
        	case Pointcut.NOT:
        	    validateAgainstSupportedPrimitives(((NotPointcut)pc).getNegatedPointcut(),expression);
        	    break;
        	case Pointcut.OR:
         	    validateAgainstSupportedPrimitives(((OrPointcut)pc).getLeft(),expression);
        		validateAgainstSupportedPrimitives(((OrPointcut)pc).getRight(),expression);
        	    break;
        	case Pointcut.REFERENCE:
        	    throw new UnsupportedPointcutPrimitiveException(expression, PointcutPrimitive.REFERENCE);
        	case Pointcut.THIS_OR_TARGET:
        	    boolean isThis = ((ThisOrTargetPointcut)pc).isThis();
        		if (isThis && !supportedPrimitives.contains(PointcutPrimitive.THIS)) {
        		    throw new UnsupportedPointcutPrimitiveException(expression, PointcutPrimitive.THIS);
        		} else if (!supportedPrimitives.contains(PointcutPrimitive.TARGET)) {
        		    throw new UnsupportedPointcutPrimitiveException(expression, PointcutPrimitive.TARGET);
        		}
        	    break;
        	case Pointcut.WITHIN:
        	    if (!supportedPrimitives.contains(PointcutPrimitive.WITHIN))
        	        throw new UnsupportedPointcutPrimitiveException(expression, PointcutPrimitive.WITHIN);
        	    break;
        	case Pointcut.WITHINCODE:
        	    if (!supportedPrimitives.contains(PointcutPrimitive.WITHIN_CODE))
        	        throw new UnsupportedPointcutPrimitiveException(expression, PointcutPrimitive.WITHIN_CODE);
        	    break;
        	case Pointcut.NONE:  // deliberate fall-through
        	default:
        	    throw new IllegalArgumentException("Unknown pointcut kind: " + pc.getPointcutKind());
        }
    }
    
    private void validateKindedPointcut(KindedPointcut pc, String expression) {
    	Shadow.Kind kind = pc.getKind();
    	if ((kind == Shadow.MethodCall) || (kind == Shadow.ConstructorCall)) {
    		if (!supportedPrimitives.contains(PointcutPrimitive.CALL))
    			throw new UnsupportedPointcutPrimitiveException(expression, PointcutPrimitive.CALL);
    	} else if ((kind == Shadow.MethodExecution) || (kind == Shadow.ConstructorExecution)) {
    		if (!supportedPrimitives.contains(PointcutPrimitive.EXECUTION))
    			throw new UnsupportedPointcutPrimitiveException(expression, PointcutPrimitive.EXECUTION);    		
    	} else if (kind == Shadow.AdviceExecution) {
    		if (!supportedPrimitives.contains(PointcutPrimitive.ADVICE_EXECUTION))
    			throw new UnsupportedPointcutPrimitiveException(expression, PointcutPrimitive.ADVICE_EXECUTION);
    	} else if (kind == Shadow.FieldGet) {
    		if (!supportedPrimitives.contains(PointcutPrimitive.GET))
    			throw new UnsupportedPointcutPrimitiveException(expression, PointcutPrimitive.GET);
    	} else if (kind == Shadow.FieldSet) {
    		if (!supportedPrimitives.contains(PointcutPrimitive.SET))
    			throw new UnsupportedPointcutPrimitiveException(expression, PointcutPrimitive.SET);		    		
    	} else if (kind == Shadow.Initialization) {
    		if (!supportedPrimitives.contains(PointcutPrimitive.INITIALIZATION))
    			throw new UnsupportedPointcutPrimitiveException(expression, PointcutPrimitive.INITIALIZATION);    		    		    		
    	} else if (kind == Shadow.PreInitialization) {
    		if (!supportedPrimitives.contains(PointcutPrimitive.PRE_INITIALIZATION))
    			throw new UnsupportedPointcutPrimitiveException(expression, PointcutPrimitive.PRE_INITIALIZATION);    		    		    		    		
    	} else if (kind == Shadow.StaticInitialization) {
    		if (!supportedPrimitives.contains(PointcutPrimitive.STATIC_INITIALIZATION))
    			throw new UnsupportedPointcutPrimitiveException(expression, PointcutPrimitive.STATIC_INITIALIZATION);    		    		    		    		    		
    	}
    }
	
	private String buildUserMessageFromParserException(String pc, ParserException ex) {
		StringBuffer msg = new StringBuffer();
		msg.append("Pointcut is not well-formed: expecting '");
		msg.append(ex.getMessage());
		msg.append("'");
		IHasPosition location = ex.getLocation();
		msg.append(" at character position ");
		msg.append(location.getStart());
		msg.append("\n");
		msg.append(pc);
		msg.append("\n");
		for (int i = 0; i < location.getStart(); i++) {
			msg.append(" ");
		}
		for (int j=location.getStart(); j <= location.getEnd(); j++) {
			msg.append("^");
		}
		msg.append("\n");
		return msg.toString();
	}
}
