/* *******************************************************************
 * Copyright (c) 2004 IBM Corporation.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * ******************************************************************/
package org.aspectj.weaver.patterns;

import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.aspectj.weaver.Shadow;

/**
 * @author colyer
 * 
 * Performs term rewriting for pointcut expressions.
 * 
 */
public class PointcutRewriter {

	private static final boolean WATCH_PROGRESS = false;
	
	public Pointcut rewrite(Pointcut pc) {
		if (WATCH_PROGRESS) System.out.println(pc);
		Pointcut result = distributeNot(pc);
		if (WATCH_PROGRESS) System.out.println("==> " + result);
		result = pullUpDisjunctions(result);
		if (WATCH_PROGRESS) System.out.println("==> " + result);
		result = simplifyAnds(result);
		if (WATCH_PROGRESS) System.out.println("==> " + result);
		result = sortOrs(result);
		if (WATCH_PROGRESS) System.out.println("==> " + result);
		return result;
	}
	
	
	// !!X => X
	// !(X && Y) => !X || !Y
	// !(X || Y) => !X && !Y
	private Pointcut distributeNot(Pointcut pc) {
		if (isNot(pc)) {
			NotPointcut npc = (NotPointcut) pc;
			Pointcut notBody = distributeNot(npc.getNegatedPointcut());
			if (isNot(notBody)) {
				// !!X => X
				return ((NotPointcut)notBody).getNegatedPointcut();
			} else if (isAnd(notBody)) {
				// !(X && Y) => !X || !Y
				AndPointcut apc = (AndPointcut) notBody;
				Pointcut newLeft = distributeNot(new NotPointcut(apc.getLeft())); 
				Pointcut newRight = distributeNot(new NotPointcut(apc.getRight()));
				return new OrPointcut(newLeft,newRight);
			} else if (isOr(notBody)) {
				// !(X || Y) => !X && !Y
				OrPointcut opc = (OrPointcut) notBody;
				Pointcut newLeft = distributeNot(new NotPointcut(opc.getLeft())); 
				Pointcut newRight = distributeNot(new NotPointcut(opc.getRight()));
				return new AndPointcut(newLeft,newRight);				
			} else {
				return new NotPointcut(notBody);
			}
		} else if (isAnd(pc)) {
			AndPointcut apc = (AndPointcut) pc;
			Pointcut left = distributeNot(apc.getLeft());
			Pointcut right = distributeNot(apc.getRight());
			return new AndPointcut(left,right);
		} else if (isOr(pc)) {
			OrPointcut opc = (OrPointcut) pc;
			Pointcut left = distributeNot(opc.getLeft());
			Pointcut right = distributeNot(opc.getRight());
			return new OrPointcut(left,right);
		} else {
			return pc;
		}
	}
	
	// A && (B || C) => (A && B) || (A && C)
	// (A || B) && C => (A && C) || (B && C)
	private Pointcut pullUpDisjunctions(Pointcut pc) {
		if (isNot(pc)) {
			NotPointcut npc = (NotPointcut)pc;
			return new NotPointcut(pullUpDisjunctions(npc.getNegatedPointcut()));
		} else if (isAnd(pc)) {
			AndPointcut apc = (AndPointcut) pc;
			// dive into left and right here...
			Pointcut left = pullUpDisjunctions(apc.getLeft());
			Pointcut right = pullUpDisjunctions(apc.getRight());
			if (isOr(left) && !isOr(right)) {
				// (A || B) && C => (A && C) || (B && C)
				Pointcut leftLeft = ((OrPointcut)left).getLeft();
				Pointcut leftRight = ((OrPointcut)left).getRight();
				return new OrPointcut(
							new AndPointcut(leftLeft,right),
							new AndPointcut(leftRight,right));
			} else if (isOr(right) && !isOr(left)) {
				// A && (B || C) => (A && B) || (A && C)
				Pointcut rightLeft = ((OrPointcut)right).getLeft();
				Pointcut rightRight = ((OrPointcut)right).getRight();
				return new OrPointcut(
							new AndPointcut(left,rightLeft),
							new AndPointcut(left,rightRight));
				
			} else {
				return new AndPointcut(left,right);
			}
		} else if (isOr(pc)){
			OrPointcut opc = (OrPointcut) pc;
			return new OrPointcut(pullUpDisjunctions(opc.getLeft()),
					              pullUpDisjunctions(opc.getRight()));
		} else {
			return pc;
		}
	}
		
	// NOT: execution(* TP.*(..)) => within(TP) && execution(* *(..))
	//      since this breaks when the pattern matches an interface
	// NOT: withincode(* TP.*(..)) => within(TP) && withincode(* *(..))
	//      since this is not correct when an aspect makes an ITD
//	private Pointcut splitOutWithins(Pointcut pc) {
//		if (isExecution(pc)) {
//			KindedPointcut kpc = (KindedPointcut) pc;
//			SignaturePattern sp = kpc.signature;
//			TypePattern within = sp.getDeclaringType();
//			if (isAnyType(within)) return pc;
//			SignaturePattern simplified = removeDeclaringTypePattern(sp);
//			return new AndPointcut(new WithinPointcut(within),
//					               new KindedPointcut(kpc.kind,simplified));
//		} else if (isNot(pc)) {
//			return new NotPointcut(splitOutWithins(((NotPointcut)pc).getNegatedPointcut()));
//		} else if (isAnd(pc)) {
//			AndPointcut apc = (AndPointcut) pc;
//			return new AndPointcut(splitOutWithins(apc.getLeft()),
//					               splitOutWithins(apc.getRight()));
//		} else if (isOr(pc)) {
//			OrPointcut opc = (OrPointcut) pc;
//			return new OrPointcut(splitOutWithins(opc.getLeft()),
//							      splitOutWithins(opc.getRight()));
//		} else {
//			return pc;
//		}
//	}
	
	private SignaturePattern removeDeclaringTypePattern(SignaturePattern sp) {
		return new SignaturePattern(
		  sp.getKind(),
		  sp.getModifiers(),
		  sp.getReturnType(),
		  TypePattern.ANY,
		  sp.getName(),
		  sp.getParameterTypes(),
		  sp.getThrowsPattern(),
		  sp.getAnnotationPattern()
		);
	}

	// this finds the root of each && tree and then aggregates all of the branches
	// into a sorted set:
	//   - duplicates are removed
	//   - A && !A is replaced by a matchesNothingPointcut
	//   - the kind(s) matched by the set are evaluated
	//   - elements are sorted by evaluation complexity
	//   - the result is written out with the least expensive branch leftmost
	private Pointcut simplifyAnds(Pointcut pc) {
		if (isNot(pc)) {
			NotPointcut npc = (NotPointcut) pc;
			return new NotPointcut(simplifyAnds(npc.getNegatedPointcut()));
		} else if (isOr(pc)) {
			OrPointcut opc = (OrPointcut) pc;
			return new OrPointcut(simplifyAnds(opc.getLeft()),simplifyAnds(opc.getRight()));
		} else if (isAnd(pc)) {
			return simplifyAnd((AndPointcut)pc);
		} else {
			return pc;
		}
	}
	
	private Pointcut simplifyAnd(AndPointcut apc) {
		SortedSet nodes = new TreeSet(new PointcutEvaluationExpenseComparator());
		collectAndNodes(apc,nodes);
		// look for A and !A, or IfFalse
		for (Iterator iter = nodes.iterator(); iter.hasNext();) {
			Pointcut element = (Pointcut) iter.next();
			if (element instanceof NotPointcut) {
				Pointcut body = ((NotPointcut)element).getNegatedPointcut();
				if (nodes.contains(body)) return Pointcut.makeMatchesNothing(body.state);
			}
			if (element instanceof IfPointcut) {
				if (((IfPointcut)element).alwaysFalse()) return Pointcut.makeMatchesNothing(element.state);
			}
			if (element.toString().equals("")) return element;  // matches nothing...
		}
		if (apc.couldMatchKinds().isEmpty()) return Pointcut.makeMatchesNothing(apc.state);
		// write out with cheapest on left
		Iterator iter = nodes.iterator();
		Pointcut result = (Pointcut) iter.next();
		while(iter.hasNext()) {
			Pointcut right = (Pointcut) iter.next();
			result = new AndPointcut(result,right);
		}
		return result;
	}
	
	private Pointcut sortOrs(Pointcut pc) {
		SortedSet nodes = new TreeSet(new PointcutEvaluationExpenseComparator());
		collectOrNodes(pc,nodes);		
		// write out with cheapest on left
		Iterator iter = nodes.iterator();
		Pointcut result = (Pointcut) iter.next();
		while(iter.hasNext()) {
			Pointcut right = (Pointcut) iter.next();
			result = new OrPointcut(result,right);
		}
		return result;
	}
	
	private void collectAndNodes(AndPointcut apc,Set nodesSoFar) {
		Pointcut left = apc.getLeft();
		Pointcut right = apc.getRight();
		if (isAnd(left)) {
			collectAndNodes((AndPointcut)left,nodesSoFar);
		} else {
			nodesSoFar.add(left);
		}
		if (isAnd(right)) {
			collectAndNodes((AndPointcut)right,nodesSoFar);
		} else {
			nodesSoFar.add(right);
		}		
	}
	
	private void collectOrNodes(Pointcut pc, Set nodesSoFar) {
		if (isOr(pc)) {
			OrPointcut opc = (OrPointcut) pc;
			collectOrNodes(opc.getLeft(),nodesSoFar);
			collectOrNodes(opc.getRight(),nodesSoFar);
		} else {
			nodesSoFar.add(pc);
		}
	}
	
	private boolean isNot(Pointcut pc) {
		return (pc instanceof NotPointcut);
	}
	
	private boolean isAnd(Pointcut pc) {
		return (pc instanceof AndPointcut);
	}
	
	private boolean isOr(Pointcut pc) {
		return (pc instanceof OrPointcut);
	}
	
	private boolean isExecution(Pointcut pc) {
		if (pc instanceof KindedPointcut) {
			KindedPointcut kp = (KindedPointcut) pc;
			if (kp.kind == Shadow.MethodExecution) return true;
			if (kp.kind == Shadow.ConstructorExecution) return true;
		}
		return false;
	}
	
	private boolean isWithinCode(Pointcut pc) {
		return (pc instanceof WithincodePointcut); 
	}
	
	private boolean isAnyType(TypePattern tp) {
		if (tp == TypePattern.ANY) return true;
		if (tp.toString().equals("*")) return true;
		return false;
	}
}