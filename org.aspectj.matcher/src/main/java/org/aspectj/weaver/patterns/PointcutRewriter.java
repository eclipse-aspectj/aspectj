/* *******************************************************************
 * Copyright (c) 2004 IBM Corporation.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * ******************************************************************/
package org.aspectj.weaver.patterns;

import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.patterns.Pointcut.MatchesNothingPointcut;

/**
 * Performs term rewriting for pointcut expressions.
 * 
 * @author colyer
 * @author clement
 */
public class PointcutRewriter {

	private static final boolean WATCH_PROGRESS = false;

	/**
	 * Set forcerewrite if you want to override the checking for something already in DNF (useful for some testing) Repeated
	 * processing of something already in DNF is expensive (it ends up being done for every pointcut on every incremental compile) -
	 * so let's not do it if we don't have to. See pr113257
	 */
	public Pointcut rewrite(Pointcut pc, boolean forceRewrite) {
		Pointcut result = pc;// checkPC(result);
		if (forceRewrite || !isDNF(pc)) {
			if (WATCH_PROGRESS) {
				System.out.println("Initial pointcut is        ==> " + format(pc));
			}
			result = distributeNot(result);// checkPC(result);
			if (WATCH_PROGRESS) {
				System.out.println("Distributing NOT gives     ==> " + format(result));
			}
			result = pullUpDisjunctions(result);// checkPC(result);
			if (WATCH_PROGRESS) {
				System.out.println("Pull up disjunctions gives ==> " + format(result));
			}
		} else {
			if (WATCH_PROGRESS) {
				System.out.println("Not distributing NOTs or pulling up disjunctions, already DNF ==> " + format(pc));
			}
		}
		result = simplifyAnds(result); // checkPC(result);
		if (WATCH_PROGRESS) {
			System.out.println("Simplifying ANDs gives     ==> " + format(result));
		}
		result = removeNothings(result); // checkPC(result);
		if (WATCH_PROGRESS) {
			System.out.println("Removing nothings gives    ==> " + format(result));
		}
		result = sortOrs(result); // checkPC(result);
		if (WATCH_PROGRESS) {
			System.out.println("Sorting ORs gives          ==> " + format(result));
		}
		return result;
	}

	// /**
	// * Checks pointcuts - used for debugging.
	// * - this variant checks if the context has been lost, since
	// * that can indicate an NPE will happen later reporting a message (pr162657).
	// * Not finished, but helped locate the problem ;)
	// */
	// private void checkPC(Pointcut pc) {
	// if (isNot(pc)) {
	// NotPointcut npc = (NotPointcut)pc;
	// checkPC(npc.getNegatedPointcut());
	// if (npc.getSourceContext()==null) {
	// System.out.println("Lost context for "+npc);
	// throw new RuntimeException("Lost context");
	// }
	// } else if (isOr(pc)) {
	// OrPointcut opc = (OrPointcut)pc;
	// checkPC(opc.getLeft());
	// checkPC(opc.getRight());
	// if (opc.getSourceContext()==null) {
	// System.out.println("Lost context for "+opc);
	// throw new RuntimeException("Lost context");
	// }
	// } else if (isAnd(pc)) {
	// AndPointcut apc = (AndPointcut)pc;
	// checkPC(apc.getLeft());
	// checkPC(apc.getRight());
	// if (apc.getSourceContext()==null) {
	// System.out.println("Lost context for "+apc);
	// throw new RuntimeException("Lost context");
	// }
	// } else {
	// if (pc.getSourceContext()==null) {
	// System.out.println("Lost context for "+pc);
	// throw new RuntimeException("Lost context");
	// }
	// }
	// }

	public Pointcut rewrite(Pointcut pc) {
		return rewrite(pc, false);
	}

	/**
	 * Check if a pointcut is in DNF - if it is then it should be lots of 'ORs' up the top with 'ANDs' beneath them.
	 */
	private boolean isDNF(Pointcut pc) {
		return isDNFHelper(pc, true);
	}

	/**
	 * Helper function for determining DNFness. Records when we have crossed the point of allowing ORs.
	 */
	private boolean isDNFHelper(Pointcut pc, boolean canStillHaveOrs) {
		if (isAnd(pc)) {
			AndPointcut ap = (AndPointcut) pc;
			return isDNFHelper(ap.getLeft(), false) && isDNFHelper(ap.getRight(), false);
		} else if (isOr(pc)) {
			if (!canStillHaveOrs) {
				return false;
			}
			OrPointcut op = (OrPointcut) pc;
			return isDNFHelper(op.getLeft(), true) && isDNFHelper(op.getRight(), true);
		} else if (isNot(pc)) {
			return isDNFHelper(((NotPointcut) pc).getNegatedPointcut(), canStillHaveOrs);
		} else {
			return true;
		}
	}

	/**
	 * Allows formatting of the output pointcut for debugging...
	 */
	public static String format(Pointcut p) {
		String s = p.toString();
		// Regex param needs '(' and '*' changing to '.'
		// s = s.replaceAll("persingleton.pkg1.monitoring.ErrorMonitoring.","M");
		// s = s.replaceAll("args.BindingTypePattern.java.lang.Throwable, 0.","Z");
		// s = s.replaceAll("within.pkg1.monitoring.DoMonitorErrors+.","X");
		// s=s.replaceAll("within.pkg1.monitoring....","Y");
		// s=s.replaceAll("if.true.","N");
		return s;
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
				return ((NotPointcut) notBody).getNegatedPointcut();
			} else if (isAnd(notBody)) {
				// !(X && Y) => !X || !Y
				AndPointcut apc = (AndPointcut) notBody;
				Pointcut newLeft = distributeNot(new NotPointcut(apc.getLeft(), npc.getStart()));
				Pointcut newRight = distributeNot(new NotPointcut(apc.getRight(), npc.getStart()));
				return new OrPointcut(newLeft, newRight);
			} else if (isOr(notBody)) {
				// !(X || Y) => !X && !Y
				OrPointcut opc = (OrPointcut) notBody;
				Pointcut newLeft = distributeNot(new NotPointcut(opc.getLeft(), npc.getStart()));
				Pointcut newRight = distributeNot(new NotPointcut(opc.getRight(), npc.getStart()));
				return new AndPointcut(newLeft, newRight);
			} else {
				return new NotPointcut(notBody, npc.getStart());
			}
		} else if (isAnd(pc)) {
			AndPointcut apc = (AndPointcut) pc;
			Pointcut left = distributeNot(apc.getLeft());
			Pointcut right = distributeNot(apc.getRight());
			return new AndPointcut(left, right);
		} else if (isOr(pc)) {
			OrPointcut opc = (OrPointcut) pc;
			Pointcut left = distributeNot(opc.getLeft());
			Pointcut right = distributeNot(opc.getRight());
			return new OrPointcut(left, right);
		} else {
			return pc;
		}
	}

	// A && (B || C) => (A && B) || (A && C)
	// (A || B) && C => (A && C) || (B && C)
	private Pointcut pullUpDisjunctions(Pointcut pc) {
		if (isNot(pc)) {
			NotPointcut npc = (NotPointcut) pc;
			return new NotPointcut(pullUpDisjunctions(npc.getNegatedPointcut()));
		} else if (isAnd(pc)) {
			AndPointcut apc = (AndPointcut) pc;
			// dive into left and right here...
			Pointcut left = pullUpDisjunctions(apc.getLeft());
			Pointcut right = pullUpDisjunctions(apc.getRight());
			if (isOr(left) && !isOr(right)) {
				// (A || B) && C => (A && C) || (B && C)
				Pointcut leftLeft = ((OrPointcut) left).getLeft();
				Pointcut leftRight = ((OrPointcut) left).getRight();
				return pullUpDisjunctions(new OrPointcut(new AndPointcut(leftLeft, right), new AndPointcut(leftRight, right)));
			} else if (isOr(right) && !isOr(left)) {
				// A && (B || C) => (A && B) || (A && C)
				Pointcut rightLeft = ((OrPointcut) right).getLeft();
				Pointcut rightRight = ((OrPointcut) right).getRight();
				return pullUpDisjunctions(new OrPointcut(new AndPointcut(left, rightLeft), new AndPointcut(left, rightRight)));
			} else if (isOr(right) && isOr(left)) {
				// (A || B) && (C || D) => (A && C) || (A && D) || (B && C) || (B && D)
				Pointcut A = pullUpDisjunctions(((OrPointcut) left).getLeft());
				Pointcut B = pullUpDisjunctions(((OrPointcut) left).getRight());
				Pointcut C = pullUpDisjunctions(((OrPointcut) right).getLeft());
				Pointcut D = pullUpDisjunctions(((OrPointcut) right).getRight());
				Pointcut newLeft = new OrPointcut(new AndPointcut(A, C), new AndPointcut(A, D));
				Pointcut newRight = new OrPointcut(new AndPointcut(B, C), new AndPointcut(B, D));
				return pullUpDisjunctions(new OrPointcut(newLeft, newRight));
			} else {
				return new AndPointcut(left, right);
			}
		} else if (isOr(pc)) {
			OrPointcut opc = (OrPointcut) pc;
			return new OrPointcut(pullUpDisjunctions(opc.getLeft()), pullUpDisjunctions(opc.getRight()));
		} else {
			return pc;
		}
	}

	/**
	 * Returns a NOTted form of the pointcut p - we cope with already NOTted pointcuts.
	 */
	public Pointcut not(Pointcut p) {
		if (isNot(p)) {
			return ((NotPointcut) p).getNegatedPointcut();
		}
		return new NotPointcut(p);
	}

	/**
	 * Passed an array of pointcuts, returns an AND tree with them in.
	 */
	public Pointcut createAndsFor(Pointcut[] ps) {
		if (ps.length == 1) {
			return ps[0]; // dumb case
		}
		if (ps.length == 2) { // recursion exit case
			return new AndPointcut(ps[0], ps[1]);
		}
		// otherwise ...
		Pointcut[] subset = new Pointcut[ps.length - 1];
		for (int i = 1; i < ps.length; i++) {
			subset[i - 1] = ps[i];
		}
		return new AndPointcut(ps[0], createAndsFor(subset));
	}

	// NOT: execution(* TP.*(..)) => within(TP) && execution(* *(..))
	// since this breaks when the pattern matches an interface
	// NOT: withincode(* TP.*(..)) => within(TP) && withincode(* *(..))
	// since this is not correct when an aspect makes an ITD
	// private Pointcut splitOutWithins(Pointcut pc) {
	// if (isExecution(pc)) {
	// KindedPointcut kpc = (KindedPointcut) pc;
	// SignaturePattern sp = kpc.signature;
	// TypePattern within = sp.getDeclaringType();
	// if (isAnyType(within)) return pc;
	// SignaturePattern simplified = removeDeclaringTypePattern(sp);
	// return new AndPointcut(new WithinPointcut(within),
	// new KindedPointcut(kpc.kind,simplified));
	// } else if (isNot(pc)) {
	// return new NotPointcut(splitOutWithins(((NotPointcut)pc).getNegatedPointcut()));
	// } else if (isAnd(pc)) {
	// AndPointcut apc = (AndPointcut) pc;
	// return new AndPointcut(splitOutWithins(apc.getLeft()),
	// splitOutWithins(apc.getRight()));
	// } else if (isOr(pc)) {
	// OrPointcut opc = (OrPointcut) pc;
	// return new OrPointcut(splitOutWithins(opc.getLeft()),
	// splitOutWithins(opc.getRight()));
	// } else {
	// return pc;
	// }
	// }

	// private SignaturePattern removeDeclaringTypePattern(SignaturePattern sp) {
	// return new SignaturePattern(
	// sp.getKind(),
	// sp.getModifiers(),
	// sp.getReturnType(),
	// TypePattern.ANY,
	// sp.getName(),
	// sp.getParameterTypes(),
	// sp.getThrowsPattern(),
	// sp.getAnnotationPattern()
	// );
	// }

	// this finds the root of each && tree and then aggregates all of the branches
	// into a sorted set:
	// - duplicates are removed
	// - A && !A is replaced by a matchesNothingPointcut
	// - the kind(s) matched by the set are evaluated
	// - elements are sorted by evaluation complexity
	// - the result is written out with the least expensive branch leftmost
	private Pointcut simplifyAnds(Pointcut pc) {
		if (isNot(pc)) {
			NotPointcut npc = (NotPointcut) pc;
			Pointcut notBody = npc.getNegatedPointcut();
			if (isNot(notBody)) {
				// !!X => X
				return simplifyAnds(((NotPointcut) notBody).getNegatedPointcut());
			} else {
				return new NotPointcut(simplifyAnds(npc.getNegatedPointcut()));
			}
		} else if (isOr(pc)) {
			OrPointcut opc = (OrPointcut) pc;
			return new OrPointcut(simplifyAnds(opc.getLeft()), simplifyAnds(opc.getRight()));
		} else if (isAnd(pc)) {
			return simplifyAnd((AndPointcut) pc);
		} else {
			return pc;
		}
	}

	private Pointcut simplifyAnd(AndPointcut apc) {
		SortedSet<Pointcut> nodes = new TreeSet<Pointcut>(new PointcutEvaluationExpenseComparator());
		collectAndNodes(apc, nodes);
		// look for A and !A, or IfFalse
		for (Iterator<Pointcut> iter = nodes.iterator(); iter.hasNext();) {
			Pointcut element = iter.next();
			if (element instanceof NotPointcut) {
				Pointcut body = ((NotPointcut) element).getNegatedPointcut();
				if (nodes.contains(body)) {
					return Pointcut.makeMatchesNothing(body.state);
				}
			}
			if (element instanceof IfPointcut) {
				if (((IfPointcut) element).alwaysFalse()) {
					return Pointcut.makeMatchesNothing(element.state);
				}
			}
			// If it can't match anything, the whole AND can't match anything
			if (element.couldMatchKinds() == Shadow.NO_SHADOW_KINDS_BITS) {
				return element;
			}
		}
		if (apc.couldMatchKinds() == Shadow.NO_SHADOW_KINDS_BITS) {
			return Pointcut.makeMatchesNothing(apc.state);
		}
		// write out with cheapest on left
		Iterator<Pointcut> iter = nodes.iterator();
		Pointcut result = iter.next();
		while (iter.hasNext()) {
			Pointcut right = iter.next();
			result = new AndPointcut(result, right);
		}
		return result;
	}

	private Pointcut sortOrs(Pointcut pc) {
		SortedSet<Pointcut> nodes = new TreeSet<Pointcut>(new PointcutEvaluationExpenseComparator());
		collectOrNodes(pc, nodes);
		// write out with cheapest on left
		Iterator<Pointcut> iter = nodes.iterator();
		Pointcut result = iter.next();
		while (iter.hasNext()) {
			Pointcut right = iter.next();
			result = new OrPointcut(result, right);
		}
		return result;
	}

	/**
	 * Removes MATCHES_NOTHING pointcuts
	 */
	private Pointcut removeNothings(Pointcut pc) {
		if (isAnd(pc)) {
			AndPointcut apc = (AndPointcut) pc;
			Pointcut right = removeNothings(apc.getRight());
			Pointcut left = removeNothings(apc.getLeft());
			if (left instanceof MatchesNothingPointcut || right instanceof MatchesNothingPointcut) {
				return new MatchesNothingPointcut();
			}
			return new AndPointcut(left, right);
		} else if (isOr(pc)) {
			OrPointcut opc = (OrPointcut) pc;
			Pointcut right = removeNothings(opc.getRight());
			Pointcut left = removeNothings(opc.getLeft());
			if (left instanceof MatchesNothingPointcut && !(right instanceof MatchesNothingPointcut)) {
				return right;
			} else if (right instanceof MatchesNothingPointcut && !(left instanceof MatchesNothingPointcut)) {
				return left;
			} else if (!(left instanceof MatchesNothingPointcut) && !(right instanceof MatchesNothingPointcut)) {
				return new OrPointcut(left, right);
			} else if (left instanceof MatchesNothingPointcut && right instanceof MatchesNothingPointcut) {
				return new MatchesNothingPointcut();
			}
		}
		return pc;
	}

	private void collectAndNodes(AndPointcut apc, Set<Pointcut> nodesSoFar) {
		Pointcut left = apc.getLeft();
		Pointcut right = apc.getRight();
		if (isAnd(left)) {
			collectAndNodes((AndPointcut) left, nodesSoFar);
		} else {
			nodesSoFar.add(left);
		}
		if (isAnd(right)) {
			collectAndNodes((AndPointcut) right, nodesSoFar);
		} else {
			nodesSoFar.add(right);
		}
	}

	private void collectOrNodes(Pointcut pc, Set<Pointcut> nodesSoFar) {
		if (isOr(pc)) {
			OrPointcut opc = (OrPointcut) pc;
			collectOrNodes(opc.getLeft(), nodesSoFar);
			collectOrNodes(opc.getRight(), nodesSoFar);
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

}