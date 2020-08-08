/* *******************************************************************
 * Copyright (c) 2007-2008 Contributors
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alexandre Vasseur    
 * ******************************************************************/
package org.aspectj.weaver.patterns;

import org.aspectj.weaver.Member;

/**
 * A sample toString like visitor that helps understanding the AST tree structure organization
 * 
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class DumpPointcutVisitor implements PatternNodeVisitor {

	private StringBuffer sb = new StringBuffer();

	public String get() {
		return sb.toString();
	}

	private void append(Object o) {
		sb.append(o.toString());
	}

	private void append(char c) {
		sb.append(c);
	}

	/**
	 * This method helps maintaining the API and raises warning when PatternNode subclasses do not implement the visitor pattern
	 * 
	 * @param node
	 * @param data
	 * @return
	 */
	public Object visit(PatternNode node, Object data) {
		System.err.println("Should implement: " + node.getClass());
		return null;
	}

	public Object visit(AnyTypePattern node, Object data) {
		append('*');
		return null;
	}

	public Object visit(NoTypePattern node, Object data) {
		append(node.toString());// TODO no idea when this one is used
		return null;
	}

	public Object visit(EllipsisTypePattern node, Object data) {
		append(node.toString());
		return null;
	}

	public Object visit(AnyWithAnnotationTypePattern node, Object data) {
		if (node.getAnnotationPattern() != AnnotationTypePattern.ANY) {
			append('(');
		}
		node.annotationPattern.accept(this, data);
		append(" *");
		if (node.getAnnotationPattern() != AnnotationTypePattern.ANY) {
			append(')');
		}
		return null;
	}

	public Object visit(AnyAnnotationTypePattern node, Object data) {
		// @ANY : ignore
		append('*');
		return null;
	}

	public Object visit(EllipsisAnnotationTypePattern node, Object data) {
		append("..");
		return null;
	}

	public Object visit(AndAnnotationTypePattern node, Object data) {
		node.getLeft().accept(this, data);
		append(' ');
		node.getRight().accept(this, data);
		return null;
	}

	public Object visit(AndPointcut node, Object data) {
		append('(');
		node.getLeft().accept(this, data);
		append(" && ");
		node.getRight().accept(this, data);
		append(')');
		return null;
	}

	public Object visit(AndTypePattern node, Object data) {
		append('(');
		node.getLeft().accept(this, data);
		append(" && ");
		node.getRight().accept(this, data);
		append(')');
		return null;
	}

	public Object visit(AnnotationPatternList node, Object data) {
		AnnotationTypePattern[] annotations = node.getAnnotationPatterns();
		for (int i = 0; i < annotations.length; i++) {
			if (i > 0) {
				append(", ");// Note: list is ",", and is " " separated for annotations
			}
			annotations[i].accept(this, data);
		}
		return null;
	}

	public Object visit(AnnotationPointcut node, Object data) {
		append("@annotation(");
		node.getAnnotationTypePattern().accept(this, data);
		append(')');
		return null;
	}

	public Object visit(ArgsAnnotationPointcut node, Object data) {
		append("@args(");
		node.getArguments().accept(this, data);
		append(')');
		return null;
	}

	public Object visit(ArgsPointcut node, Object data) {
		append("args(");
		node.getArguments().accept(this, data);
		append(')');
		return null;
	}

	public Object visit(BindingAnnotationTypePattern node, Object data) {
		append(node);
		return null;
	}

	public Object visit(BindingTypePattern node, Object data) {
		append(node);
		return null;
	}

	public Object visit(CflowPointcut node, Object data) {
		append(node.isCflowBelow() ? "cflowbelow(" : "cflow(");
		node.getEntry().accept(this, data);
		append(')');
		return null;
	}

	public Object visit(ExactAnnotationTypePattern node, Object data) {
		// append('@'); // since @annotation(@someAnno) cannot be parsed anymore
		append(node.getAnnotationType().getName());
		return null;
	}

	public Object visit(ExactTypePattern node, Object data) {
		if (node.getAnnotationPattern() != AnnotationTypePattern.ANY) {
			append('(');
			node.getAnnotationPattern().accept(this, data);
			append(' ');
		}

		String typeString = node.getType().toString();
		if (node.isVarArgs()) {
			typeString = typeString.substring(0, typeString.lastIndexOf('['));// TODO AV - ugly
		}
		append(typeString);
		if (node.isIncludeSubtypes()) {
			append('+');
		}
		if (node.isVarArgs()) {
			append("...");
		}
		if (node.getAnnotationPattern() != AnnotationTypePattern.ANY) {
			append(')');
		}
		return null;
	}

	public Object visit(KindedPointcut node, Object data) {
		append(node.getKind().getSimpleName());
		append('(');
		node.getSignature().accept(this, data);
		append(')');
		return null;
	}

	public Object visit(ModifiersPattern node, Object data) {
		append(node.toString());// note: node takes care of forbidden mods
		return null;
	}

	public Object visit(NamePattern node, Object data) {
		append(node.toString());
		return null;
	}

	public Object visit(NotAnnotationTypePattern node, Object data) {
		append("!");
		node.getNegatedPattern().accept(this, data);
		return null;
	}

	public Object visit(NotPointcut node, Object data) {
		append("!(");
		node.getNegatedPointcut().accept(this, data);
		append(')');
		return null;
	}

	public Object visit(NotTypePattern node, Object data) {
		append("!(");
		node.getNegatedPattern().accept(this, data);
		append(')');
		return null;
	}

	public Object visit(OrAnnotationTypePattern node, Object data) {
		append('(');
		node.getLeft().accept(this, data);
		append(" || ");
		node.getRight().accept(this, data);
		append(')');
		return null;
	}

	public Object visit(OrPointcut node, Object data) {
		append('(');
		node.getLeft().accept(this, data);
		append(" || ");
		node.getRight().accept(this, data);
		append(')');
		return null;
	}

	public Object visit(OrTypePattern node, Object data) {
		append('(');
		node.getLeft().accept(this, data);
		append(" || ");
		node.getRight().accept(this, data);
		append(')');
		return null;
	}

	public Object visit(ReferencePointcut node, Object data) {
		append(node.toString());
		return null;
	}

	public Object visit(SignaturePattern node, Object data) {
		if (node.getAnnotationPattern() != AnnotationTypePattern.ANY) {
			node.getAnnotationPattern().accept(this, data);
			append(' ');
		}

		if (node.getModifiers() != ModifiersPattern.ANY) {
			node.getModifiers().accept(this, data);
			append(' ');
		}

		if (node.getKind() == Member.STATIC_INITIALIZATION) {
			node.getDeclaringType().accept(this, data);
		} else if (node.getKind() == Member.HANDLER) {
			append("handler(");
			node.getParameterTypes().get(0).accept(this, data);// Note: we know we have 1 child
			append(')');
		} else {
			if (!(node.getKind() == Member.CONSTRUCTOR)) {
				node.getReturnType().accept(this, data);
				append(' ');
			}
			if (node.getDeclaringType() != TypePattern.ANY) {
				node.getDeclaringType().accept(this, data);
				append('.');
			}
			if (node.getKind() == Member.CONSTRUCTOR) {
				append("new");
			} else {
				node.getName().accept(this, data);
			}
			if (node.getKind() == Member.METHOD || node.getKind() == Member.CONSTRUCTOR) {
				append('(');
				node.getParameterTypes().accept(this, data);
				append(')');
			}
			if (node.getThrowsPattern() != null) {
				append(' ');
				node.getThrowsPattern().accept(this, data);
			}
		}
		return null;
	}

	public Object visit(ThisOrTargetAnnotationPointcut node, Object data) {
		append(node.isThis() ? "@this(" : "@target(");
		node.getAnnotationTypePattern().accept(this, data);
		append(')');
		return null;
	}

	public Object visit(ThisOrTargetPointcut node, Object data) {
		append(node.isThis() ? "this(" : "target(");
		node.getType().accept(this, data);
		append(')');
		return null;
	}

	// Note: a visitor instance is not thread safe so should not be shared
	private boolean inThrowsForbidden = false;

	public Object visit(ThrowsPattern node, Object data) {
		if (node == ThrowsPattern.ANY) {
			return null;
		}

		append("throws ");
		node.getRequired().accept(this, data);
		if (node.getForbidden().size() > 0) {
			// a hack since throws !(A, B) cannot be parsed
			try {
				inThrowsForbidden = true;
				node.getForbidden().accept(this, data);
			} finally {
				inThrowsForbidden = false;
			}
		}
		return null;
	}

	public Object visit(TypePatternList node, Object data) {
		if (node.getTypePatterns().length == 0) {
			return null;
		}

		TypePattern[] typePatterns = node.getTypePatterns();
		for (int i = 0; i < typePatterns.length; i++) {
			TypePattern typePattern = typePatterns[i];
			if (i > 0) {
				append(", ");
			}
			if (inThrowsForbidden) {
				append('!');
			}
			typePattern.accept(this, data);
		}
		return null;
	}

	public Object visit(WildAnnotationTypePattern node, Object data) {
		append("@(");
		node.getTypePattern().accept(this, data);
		append(')');
		return null;
	}

	public Object visit(WildTypePattern node, Object data) {
		if (node.getAnnotationPattern() != AnnotationTypePattern.ANY) {
			append('(');
			node.getAnnotationPattern().accept(this, data);
			append(' ');
		}
		NamePattern[] namePatterns = node.getNamePatterns();
		for (int i = 0; i < namePatterns.length; i++) {
			if (namePatterns[i] == null) {
				append('.');// FIXME mh, error prone, can't we have a nullNamePattern ?
			} else {
				if (i > 0) {
					append('.');
				}
				namePatterns[i].accept(this, data);
			}
		}
		if (node.isIncludeSubtypes()) {
			append('+');
		}
		if (node.isVarArgs()) {
			append("...");
		}
		if (node.getAnnotationPattern() != AnnotationTypePattern.ANY) {
			append(')');
		}
		return null;
	}

	public Object visit(WithinAnnotationPointcut node, Object data) {
		append("@within(");
		node.getAnnotationTypePattern().accept(this, data);
		append(')');
		return null;
	}

	public Object visit(WithinCodeAnnotationPointcut node, Object data) {
		append("@withincode(");
		node.getAnnotationTypePattern().accept(this, data);
		append(')');
		return null;
	}

	public Object visit(WithinPointcut node, Object data) {
		append("within(");
		node.getTypePattern().accept(this, data);
		append(')');
		return null;
	}

	public Object visit(WithincodePointcut node, Object data) {
		append("withincode(");
		node.getSignature().accept(this, data);
		append(')');
		return null;
	}

	public Object visit(Pointcut.MatchesNothingPointcut node, Object data) {
		append("");// TODO shouldn't that be a "false" ?
		return null;
	}

	// -------------- perX

	public Object visit(PerCflow node, Object data) {
		append(node);
		return null;
	}

	public Object visit(PerFromSuper node, Object data) {
		append(node);
		return null;
	}

	public Object visit(PerObject node, Object data) {
		append(node);
		return null;
	}

	public Object visit(PerSingleton node, Object data) {
		append(node);
		return null;
	}

	public Object visit(PerTypeWithin node, Object data) {
		append(node);
		return null;
	}

	// ------------- declare X

	public Object visit(DeclareAnnotation node, Object data) {
		append(node);
		return null;
	}

	public Object visit(DeclareErrorOrWarning node, Object data) {
		append(node);
		return null;
	}

	public Object visit(DeclareParents node, Object data) {
		append(node);
		return null;
	}

	public Object visit(DeclarePrecedence node, Object data) {
		append(node);
		return null;
	}

	public Object visit(DeclareSoft node, Object data) {
		append(node);
		return null;
	}

	// ----------- misc

	public Object visit(ConcreteCflowPointcut node, Object data) {
		append(node);
		return null;
	}

	public Object visit(HandlerPointcut node, Object data) {
		append(node);
		return null;
	}

	public Object visit(IfPointcut node, Object data) {
		append(node);
		return null;
	}

	public Object visit(TypeVariablePattern node, Object data) {
		append(node);
		return null;
	}

	public Object visit(TypeVariablePatternList node, Object data) {
		append(node);
		return null;
	}

	public Object visit(HasMemberTypePattern node, Object data) {
		append(node);
		return null;
	}

	public Object visit(TypeCategoryTypePattern node, Object data) {
		append(node);
		return null;
	}

	public static void check(String s) {
		check(Pointcut.fromString(s), false);
	}

	public static void check(PatternNode pc, boolean isTypePattern) {
		DumpPointcutVisitor v1 = new DumpPointcutVisitor();
		pc.accept(v1, null);

		DumpPointcutVisitor v2 = new DumpPointcutVisitor();
		final PatternNode pc2;
		if (isTypePattern) {
			pc2 = new PatternParser(v1.get()).parseTypePattern();
		} else {
			pc2 = Pointcut.fromString(v1.get());
		}
		pc2.accept(v2, null);

		// at second parsing, the String form stay stable when parsed and parsed again
		if (!v1.get().equals(v2.get())) {
			throw new ParserException("Unstable back parsing for '" + pc + "', got '" + v1.get() + "' and '" + v2.get() + "'", null);
		}
	}

	public static void main(String args[]) throws Throwable {
		String[] s = new String[] {
		// "@args(Foo, Goo, *, .., Moo)",
		// "execution(* *())",
		// "call(* *(int, Integer...))",
		// "staticinitialization(@(Foo) @(Boo) @(Goo) Moo)",
		"(if(true) && set(int BaseApp.i))"

		};
		for (String value : s) {
			check(value);
		}
	}

}