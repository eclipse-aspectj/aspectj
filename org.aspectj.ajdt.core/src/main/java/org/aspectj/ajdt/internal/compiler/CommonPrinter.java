/* *******************************************************************
 * Copyright (c) 2010 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Andy Clement - SpringSource
 * ******************************************************************/
package org.aspectj.ajdt.internal.compiler;

import org.aspectj.asm.internal.CharOperation;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.AbstractVariableDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.AllocationExpression;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.Argument;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.ArrayAllocationExpression;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.ArrayInitializer;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.ArrayReference;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.AssertStatement;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.Assignment;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.BinaryExpression;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.Block;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.BreakStatement;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.CaseStatement;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.CastExpression;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.CharLiteral;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.ClassLiteralAccess;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.ConditionalExpression;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.DoubleLiteral;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.Expression;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.FalseLiteral;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.FieldReference;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.FloatLiteral;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.ForStatement;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.ForeachStatement;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.IfStatement;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.InstanceOfExpression;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.IntLiteral;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.LongLiteral;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.MemberValuePair;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.NullLiteral;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.ParameterizedQualifiedTypeReference;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.ParameterizedSingleTypeReference;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.PostfixExpression;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.PrefixExpression;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.QualifiedAllocationExpression;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.QualifiedNameReference;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.ReturnStatement;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.Statement;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.StringLiteral;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.SwitchStatement;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.ThisReference;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.ThrowStatement;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.TrueLiteral;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.TryStatement;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeParameter;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.UnaryExpression;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.Wildcard;
import org.aspectj.org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ParameterizedFieldBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class CommonPrinter {

	StringBuilder output;
	private int tab = 0;
	MethodScope mscope;
	AbstractMethodDeclaration declaration;
	protected int expressionLevel = 0;

	public CommonPrinter(MethodScope mscope) {
		output = new StringBuilder();
		this.mscope = mscope;
	}

	protected StringBuilder printTypeReference(TypeReference tr) {
		if (tr instanceof Wildcard) {
			Wildcard w = (Wildcard) tr;
			output.append('?');
			if (w.bound != null) {
				if (w.kind == Wildcard.EXTENDS) {
					output.append(" extends ");
				} else if (w.kind == Wildcard.SUPER) {
					output.append(" super ");
				}
				printTypeReference(w.bound);
			}
			return output;
		} else if (tr instanceof ParameterizedSingleTypeReference) {
			ParameterizedSingleTypeReference pstr = (ParameterizedSingleTypeReference) tr;
			ReferenceBinding tb = (ReferenceBinding) mscope.getType(pstr.token);
			output.append(CharOperation.concatWith(tb.compoundName, '.'));
			output.append('<');
			TypeReference[] typeArguments = pstr.typeArguments;
			for (int i = 0; i < typeArguments.length; i++) {
				if (i > 0) {
					output.append(',');
				}
				printTypeReference(typeArguments[i]);
			}
			output.append('>');
			for (int i = 0; i < pstr.dimensions; i++) {
				output.append("[]"); //$NON-NLS-1$
			}
			return output;
		} else if (tr instanceof ParameterizedQualifiedTypeReference) {
			ParameterizedQualifiedTypeReference pqtr = (ParameterizedQualifiedTypeReference) tr;
			output.append(CharOperation.concatWith(pqtr.tokens, '.'));
			output.append('<');
			TypeReference[][] typeArguments = pqtr.typeArguments;
			// TODO don't support parameterized interim name components
			TypeReference[] ofInterest = typeArguments[typeArguments.length - 1];
			for (int i = 0; i < ofInterest.length; i++) {
				if (i > 0) {
					output.append(',');
				}
				printTypeReference(ofInterest[i]);
			}
			output.append('>');
			for (int i = 0; i < pqtr.dimensions(); i++) {
				output.append("[]"); //$NON-NLS-1$
			}
			return output;
		} else if (tr instanceof SingleTypeReference) {
			SingleTypeReference str = (SingleTypeReference) tr;
			TypeBinding tb = mscope.getType(str.token);
			output.append(tb.debugName()); // fq name
			for (int i = 0; i < str.dimensions(); i++) {
				output.append("[]"); //$NON-NLS-1$
			}
			return output;
		} else if (tr instanceof QualifiedTypeReference) {
			QualifiedTypeReference qtr = (QualifiedTypeReference) tr;
			output.append(CharOperation.concatWith(qtr.tokens, '.'));
			for (int i = 0; i < qtr.dimensions(); i++) {
				output.append("[]"); //$NON-NLS-1$
			}
			return output;
		}
		throwit(tr);
		return output;
	}

	protected StringBuilder printMemberValuePair(MemberValuePair mvp) {
		output.append(mvp.name).append(" = "); //$NON-NLS-1$
		printExpression(mvp.value);
		return output;
	}

	protected StringBuilder printAnnotations(Annotation[] annotations) {
		int length = annotations.length;
		for (Annotation annotation : annotations) {
			printAnnotation(annotation);
			output.append(" "); //$NON-NLS-1$
		}
		return output;
	}

	public StringBuilder printAnnotation(Annotation annotation) {
		output.append('@');
		printExpression(annotation.type);
		MemberValuePair[] mvps = annotation.memberValuePairs();
		if (mvps != null && mvps.length > 0) {
			output.append('(');
			for (int m = 0; m < mvps.length; m++) {
				if (m > 0) {
					output.append(',');
				}
				printMemberValuePair(mvps[m]);
			}
			output.append(')');
		}
		return output;
	}

	public String toString() {
		return output.toString();
	}

	protected StringBuilder printBody(int indent) {

		if (declaration.isAbstract()) { // || (md.modifiers & ExtraCompilerModifiers.AccSemicolonBody) != 0) {
			return output.append(';');
		}

		output.append(" {"); //$NON-NLS-1$
		if (declaration.statements != null) {
			for (int i = 0; i < declaration.statements.length; i++) {
				output.append('\n');
				printStatement(declaration.statements[i], indent);
			}
		}
		output.append('\n');
		printIndent(indent == 0 ? 0 : indent - 1).append('}');
		return output;
	}

	protected StringBuilder printBody(AbstractMethodDeclaration amd, int indent) {

		if (amd.isAbstract()) { // || (md.modifiers & ExtraCompilerModifiers.AccSemicolonBody) != 0) {
			return output.append(';');
		}

		output.append(" {"); //$NON-NLS-1$
		if (amd.statements != null) {
			for (int i = 0; i < amd.statements.length; i++) {
				output.append('\n');
				printStatement(amd.statements[i], indent);
			}
		}
		output.append('\n');
		printIndent(indent == 0 ? 0 : indent - 1).append('}');
		return output;
	}

	protected StringBuilder printArgument(Argument argument) {
		// printIndent(indent, output);
		printModifiers(argument.modifiers);
		if (argument.annotations != null) {
			printAnnotations(argument.annotations);
		}
		printTypeReference(argument.type).append(' ');
		return output.append(argument.name);
	}

	void throwit(Object o) {
		if (true) {
			System.out.println("so far:" + output.toString());
			throw new IllegalStateException(o == null ? "" : o.getClass().getName() + ":" + o);
		}
	}

	void throwit() {
		if (true) {
			throw new IllegalStateException();
		}
	}

	public StringBuilder printIndent(int indent) {
		for (int i = indent; i > 0; i--) {
			output.append("  "); //$NON-NLS-1$
		}
		return output;
	}

	public StringBuilder printModifiers(int modifiers) {

		if ((modifiers & ClassFileConstants.AccPublic) != 0) {
			output.append("public "); //$NON-NLS-1$
		}
		if ((modifiers & ClassFileConstants.AccPrivate) != 0) {
			output.append("private "); //$NON-NLS-1$
		}
		if ((modifiers & ClassFileConstants.AccProtected) != 0) {
			output.append("protected "); //$NON-NLS-1$
		}
		if ((modifiers & ClassFileConstants.AccStatic) != 0) {
			output.append("static "); //$NON-NLS-1$
		}
		if ((modifiers & ClassFileConstants.AccFinal) != 0) {
			output.append("final "); //$NON-NLS-1$
		}
		if ((modifiers & ClassFileConstants.AccSynchronized) != 0) {
			output.append("synchronized "); //$NON-NLS-1$
		}
		if ((modifiers & ClassFileConstants.AccVolatile) != 0) {
			output.append("volatile "); //$NON-NLS-1$
		}
		if ((modifiers & ClassFileConstants.AccTransient) != 0) {
			output.append("transient "); //$NON-NLS-1$
		}
		if ((modifiers & ClassFileConstants.AccNative) != 0) {
			output.append("native "); //$NON-NLS-1$
		}
		if ((modifiers & ClassFileConstants.AccAbstract) != 0) {
			output.append("abstract "); //$NON-NLS-1$
		}
		return output;
	}

	public StringBuilder printExpression(Expression e) {
		// TODO other literals
		try {
			expressionLevel++;
			if (e instanceof TypeReference) {
				return printTypeReference((TypeReference) e);
			} else if (e instanceof IntLiteral) {
				return output.append(((IntLiteral) e).value);
			} else if (e instanceof CharLiteral) {
				return output.append(((CharLiteral) e).source());
			} else if (e instanceof DoubleLiteral) {
				return output.append(((DoubleLiteral) e).source());
			} else if (e instanceof LongLiteral) {
				return output.append(((LongLiteral) e).source());
			} else if (e instanceof FloatLiteral) {
				return output.append(((FloatLiteral) e).source());
			} else if (e instanceof TrueLiteral) {
				return output.append(((TrueLiteral) e).source());
			} else if (e instanceof FalseLiteral) {
				return output.append(((FalseLiteral) e).source());
			} else if (e instanceof ClassLiteralAccess) {
				printTypeReference(((ClassLiteralAccess) e).type);
				return output.append(".class");
			} else if (e instanceof StringLiteral) {
				return printStringLiteral((StringLiteral) e);
			} else if (e instanceof SingleNameReference) {
				SingleNameReference snr = (SingleNameReference) e;
				if (snr.binding instanceof ReferenceBinding) {
					output.append(CharOperation.concatWith(((ReferenceBinding) snr.binding).compoundName, '.'));
				} else if (snr.binding instanceof ParameterizedFieldBinding) {
					ParameterizedFieldBinding pfb = (ParameterizedFieldBinding) snr.binding;
					output.append(pfb.name);
				} else if (snr.binding instanceof LocalVariableBinding) {
					LocalVariableBinding lvb = (LocalVariableBinding) snr.binding;
					output.append(lvb.name);
				} else if (snr.binding instanceof FieldBinding) {
					FieldBinding fb = (FieldBinding) snr.binding;
					ReferenceBinding rb = fb.declaringClass;
					if (fb.isStatic()) {
						// qualify it
						output.append(CharOperation.concatWith(rb.compoundName, '.'));
						output.append('.');
						output.append(fb.name);
					} else {
						output.append(snr.token);
					}
					int stop = 1;
				} else {
					throwit(snr.binding);
				}
				return output;
			} else if (e instanceof QualifiedNameReference) {
				QualifiedNameReference qnr = (QualifiedNameReference) e;
				if (qnr.binding instanceof FieldBinding) {
					FieldBinding fb = (FieldBinding) qnr.binding;
					ReferenceBinding rb = fb.declaringClass;
					if (fb.isStatic()) {
						output.append(CharOperation.concatWith(rb.compoundName, '.'));
						output.append('.');
						output.append(fb.name);
					} else {
						output.append(CharOperation.concatWith(qnr.tokens, '.'));// ((ReferenceBinding) qnr.binding).compoundName,
					}
				} else if (qnr.binding instanceof ReferenceBinding) {
					output.append(CharOperation.concatWith(qnr.tokens, '.'));// ((ReferenceBinding) qnr.binding).compoundName,
					// '.'));
				} else if (qnr.binding instanceof LocalVariableBinding) {
					output.append(CharOperation.concatWith(qnr.tokens, '.'));// oncatWith(((LocalVariableBinding)
					// qnr.binding).compoundName, '.'));
					// LocalVariableBinding lvb = (LocalVariableBinding) qnr.binding;
					// output.append(lvb.name);
				} else {
					throwit(qnr.binding);
				}
				// output.append(qnr.actualReceiverType.debugName());
				// output.append('.');
				// output.append(qnr.tokens[qnr.tokens.length - 1]);
				return output;
			} else if (e instanceof ArrayReference) {
				ArrayReference ar = (ArrayReference) e;
				printExpression(ar.receiver).append('[');
				return printExpression(ar.position).append(']');
			} else if (e instanceof MessageSend) {
				return printMessageSendStatement((MessageSend) e);
			} else if (e instanceof ThisReference) {
				ThisReference tr = (ThisReference) e;
				if (tr.isImplicitThis()) {
					return output;
				}
				return output.append("this"); //$NON-NLS-1$
			} else if (e instanceof CastExpression) {
				return printCastExpression((CastExpression) e);
			} else if (e instanceof BinaryExpression) {
				if (expressionLevel != 0) {
					output.append('(');
				}
				expressionLevel++;
				BinaryExpression be = (BinaryExpression) e;
				printExpression(be.left).append(' ').append(be.operatorToString()).append(' ');
				printExpression(be.right);
				expressionLevel--;
				if (expressionLevel != 0) {
					output.append(')');
				}
				return output;
			} else if (e instanceof NullLiteral) {
				return output.append("null");
			} else if (e instanceof QualifiedAllocationExpression) {
				return printQualifiedAllocationExpression((QualifiedAllocationExpression) e, 0);
			} else if (e instanceof AllocationExpression) {
				return printAllocationExpression((AllocationExpression) e);
			} else if (e instanceof ArrayInitializer) {
				return printArrayInitialized((ArrayInitializer) e);
			} else if (e instanceof FieldReference) {
				return printFieldReference((FieldReference) e);
			} else if (e instanceof UnaryExpression) {
				return printUnaryExpression((UnaryExpression) e);
			} else if (e instanceof InstanceOfExpression) {
				return printInstanceOfExpression((InstanceOfExpression) e);
			} else if (e instanceof Assignment) {
				return printAssignment((Assignment) e, false);
			} else if (e instanceof ArrayAllocationExpression) {
				return printArrayAllocationExpression((ArrayAllocationExpression) e);
			} else if (e instanceof ConditionalExpression) {
				return printConditionalExpression((ConditionalExpression) e);
			}
			throwit(e);
			return output;
		} finally {
			expressionLevel--;
		}
	}

	private StringBuilder printConditionalExpression(ConditionalExpression e) {
		if (expressionLevel != 0) {
			output.append('(');
		}
		expressionLevel++;
		printExpression(e.condition).append(" ? ");
		printExpression(e.valueIfTrue).append(" : ");
		printExpression(e.valueIfFalse);
		expressionLevel--;
		if (expressionLevel != 0) {
			output.append(')');
		}
		return output;
	}

	private StringBuilder printArrayAllocationExpression(ArrayAllocationExpression aae) {
		output.append("new "); //$NON-NLS-1$
		printTypeReference(aae.type);
		for (int i = 0; i < aae.dimensions.length; i++) {
			if (aae.dimensions[i] == null) {
				output.append("[]"); //$NON-NLS-1$
			} else {
				output.append('[');
				printExpression(aae.dimensions[i]);
				output.append(']');
			}
		}
		if (aae.initializer != null) {
			printExpression(aae.initializer);
		}
		return output;
	}

	private StringBuilder printInstanceOfExpression(InstanceOfExpression e) {
		if (expressionLevel != 0) {
			output.append('(');
		}
		expressionLevel++;
		printExpression(e.expression).append(" instanceof "); //$NON-NLS-1$
		printTypeReference(e.type);
		expressionLevel--;
		if (expressionLevel != 0) {
			output.append(')');
		}
		return output;
	}

	private StringBuilder printUnaryExpression(UnaryExpression e) {
		if (expressionLevel != 0) {
			output.append('(');
		}
		expressionLevel++;
		output.append(e.operatorToString()).append(' ');
		printExpression(e.expression);
		expressionLevel--;
		if (expressionLevel != 0) {
			output.append(')');
		}
		return output;
	}

	private StringBuilder printFieldReference(FieldReference fr) {
		printExpression(fr.receiver).append('.').append(fr.token);
		return output;
	}

	private StringBuilder printArrayInitialized(ArrayInitializer e) {

		output.append('{');
		if (e.expressions != null) {
			// int j = 20;
			for (int i = 0; i < e.expressions.length; i++) {
				if (i > 0) {
					output.append(", "); //$NON-NLS-1$
				}
				printExpression(e.expressions[i]);
				// expressions[i].printExpression(0, output);
				// j--;
				// if (j == 0) {
				// output.append('\n');
				// printIndent(indent + 1, output);
				// j = 20;
				// }
			}
		}
		return output.append('}');
	}

	private StringBuilder printCastExpression(CastExpression e) {
		output.append('(');
		output.append('(');
		printExpression(e.type).append(") "); //$NON-NLS-1$
		printExpression(e.expression);
		output.append(')');
		return output;
	}

	private StringBuilder printStringLiteral(StringLiteral e) {
		output.append('\"');
		for (int i = 0; i < e.source().length; i++) {
			switch (e.source()[i]) {
			case '\b':
				output.append("\\b"); //$NON-NLS-1$
				break;
			case '\t':
				output.append("\\t"); //$NON-NLS-1$
				break;
			case '\n':
				output.append("\\n"); //$NON-NLS-1$
				break;
			case '\f':
				output.append("\\f"); //$NON-NLS-1$
				break;
			case '\r':
				output.append("\\r"); //$NON-NLS-1$
				break;
			case '\"':
				output.append("\\\""); //$NON-NLS-1$
				break;
			case '\'':
				output.append("\\'"); //$NON-NLS-1$
				break;
			case '\\': // take care not to display the escape as a potential real char
				output.append("\\\\"); //$NON-NLS-1$
				break;
			default:
				output.append(e.source()[i]);
			}
		}
		output.append('\"');
		return output;
	}

	public StringBuilder printExpression(SingleTypeReference str) {
		output.append(str.token);
		return output;
	}

	protected StringBuilder printStatement(Statement statement, int indent) {
		return printStatement(statement, indent, true);
	}

	protected StringBuilder printStatement(Statement statement, int indent, boolean applyIndent) {
		if (statement instanceof ReturnStatement) {
			printIndent(indent).append("return "); //$NON-NLS-1$
			if (((ReturnStatement) statement).expression != null) {
				printExpression(((ReturnStatement) statement).expression);
			}
			return output.append(';');
		} else if (statement instanceof PostfixExpression) {
			return printPostfixExpression((PostfixExpression) statement);
		} else if (statement instanceof PrefixExpression) {
			return printPrefixExpression((PrefixExpression) statement);
		} else if (statement instanceof MessageSend) {
			printIndent(indent);
			MessageSend ms = (MessageSend) statement;
			printMessageSendStatement(ms);
			return output.append(';');
		} else if (statement instanceof QualifiedAllocationExpression) {
			printIndent(indent);
			printQualifiedAllocationExpression((QualifiedAllocationExpression) statement, indent);
			return output.append(';');
		} else if (statement instanceof Assignment) {
			printIndent(indent);
			printAssignment((Assignment) statement);
			return output.append(';');
		} else if (statement instanceof TryStatement) {
			printTryStatement((TryStatement) statement, indent);
			return output;
		} else if (statement instanceof IfStatement) {
			printIndent(indent);
			IfStatement is = (IfStatement) statement;
			printIndent(indent).append("if ("); //$NON-NLS-1$
			printExpression(is.condition).append(")\n"); //$NON-NLS-1$ 
			printStatement(is.thenStatement, indent + 2);
			if (is.elseStatement != null) {
				output.append('\n');
				printIndent(indent);
				output.append("else\n"); //$NON-NLS-1$
				printStatement(is.elseStatement, indent + 2);
			}
			return output;
		} else if (statement instanceof Block) {
			printBlock((Block) statement, indent, applyIndent);
			return output;
		} else if (statement instanceof LocalDeclaration) {
			return printLocalDeclaration((LocalDeclaration) statement, indent);
		} else if (statement instanceof SwitchStatement) {
			return printSwitchStatement((SwitchStatement) statement, indent);
		} else if (statement instanceof CaseStatement) {
			return printCaseStatement((CaseStatement) statement, indent);
		} else if (statement instanceof BreakStatement) {
			return printBreakStatement((BreakStatement) statement, indent);
		} else if (statement instanceof ThrowStatement) {
			return printThrowStatement((ThrowStatement) statement, indent);
		} else if (statement instanceof TypeDeclaration) {
			return printTypeDeclaration((TypeDeclaration) statement, indent, false).append(';');
		} else if (statement instanceof AssertStatement) {
			return printAssertStatement((AssertStatement) statement, indent);
		} else if (statement instanceof ForStatement) {
			return printForStatement((ForStatement) statement, indent);
		} else if (statement instanceof ForeachStatement) {
			return printForeachStatement((ForeachStatement) statement, indent);
		}
		System.err.println(statement);
		System.err.println(statement.getClass().getName());
		throwit(statement);

		return output;
	}

	private StringBuilder printPostfixExpression(PostfixExpression pe) {
		printExpression(pe.lhs);
		output.append(' ');
		output.append(pe.operatorToString());
		return output;
	}

	private StringBuilder printPrefixExpression(PrefixExpression pe) {
		output.append(pe.operatorToString());
		output.append(' ');
		printExpression(pe.lhs);
		return output;
	}

	public StringBuilder printAsExpression(LocalDeclaration ld, int indent) {
		// printIndent(indent);
		printModifiers(ld.modifiers);
		if (ld.annotations != null) {
			printAnnotations(ld.annotations);
		}

		if (ld.type != null) {
			printTypeReference(ld.type).append(' ');
		}
		output.append(ld.name);
		switch (ld.getKind()) {
		case AbstractVariableDeclaration.ENUM_CONSTANT:
			if (ld.initialization != null) {
				printExpression(ld.initialization);
			}
			break;
		default:
			if (ld.initialization != null) {
				output.append(" = "); //$NON-NLS-1$
				printExpression(ld.initialization);
			}
		}
		return output;
	}

	private StringBuilder printForeachStatement(ForeachStatement statement, int indent) {
		printIndent(indent).append("for ("); //$NON-NLS-1$
		printAsExpression(statement.elementVariable, indent);
		output.append(" : ");//$NON-NLS-1$
		printExpression(statement.collection).append(") "); //$NON-NLS-1$
		// block
		if (statement.action == null) {
			output.append(';');
		} else {
			printStatement(statement.action, indent + 1);
		}
		return output;
	}

	private StringBuilder printForStatement(ForStatement fs, int indent) {
		printIndent(indent).append("for ("); //$NON-NLS-1$

		// inits
		if (fs.initializations != null) {
			for (int i = 0; i < fs.initializations.length; i++) {
				// nice only with expressions
				if (i > 0) {
					output.append(", "); //$NON-NLS-1$
				}
				printStatement(fs.initializations[i], 0);
			}
		}
		if (!output.toString().endsWith(";")) {
			output.append("; "); //$NON-NLS-1$
		}
		// cond
		if (fs.condition != null) {
			printExpression(fs.condition);
		}
		output.append("; "); //$NON-NLS-1$
		// updates
		if (fs.increments != null) {
			for (int i = 0; i < fs.increments.length; i++) {
				if (i > 0) {
					output.append(", "); //$NON-NLS-1$
				}
				printStatement(fs.increments[i], 0);
			}
		}
		output.append(") "); //$NON-NLS-1$
		// block
		if (fs.action == null) {
			output.append(';');
		} else {
			// output.append('\n');
			printStatement(fs.action, indent + 1, false);
		}
		return output;
	}

	private StringBuilder printAssertStatement(AssertStatement as, int indent) {
		printIndent(indent);
		output.append("assert "); //$NON-NLS-1$
		printExpression(as.assertExpression);
		if (as.exceptionArgument != null) {
			output.append(": "); //$NON-NLS-1$
			printExpression(as.exceptionArgument);
		}
		return output.append(';');
	}

	private StringBuilder printThrowStatement(ThrowStatement ts, int indent) {
		printIndent(indent).append("throw "); //$NON-NLS-1$
		printExpression(ts.exception);
		return output.append(';');
	}

	private StringBuilder printBreakStatement(BreakStatement statement, int indent) {
		printIndent(indent).append("break "); //$NON-NLS-1$
		if (statement.label != null) {
			output.append(statement.label);
		}
		return output.append(';');
	}

	private StringBuilder printCaseStatement(CaseStatement statement, int indent) {
		printIndent(indent);
		if (statement.constantExpression == null) {
			output.append("default : "); //$NON-NLS-1$
		} else {
			output.append("case "); //$NON-NLS-1$
			printExpression(statement.constantExpression).append(" : "); //$NON-NLS-1$
		}
		return output;// output.append(';');
	}

	private StringBuilder printSwitchStatement(SwitchStatement statement, int indent) {
		printIndent(indent).append("switch ("); //$NON-NLS-1$
		printExpression(statement.expression).append(") {"); //$NON-NLS-1$
		if (statement.statements != null) {
			for (int i = 0; i < statement.statements.length; i++) {
				output.append('\n');
				if (statement.statements[i] instanceof CaseStatement) {
					printStatement(statement.statements[i], indent);
				} else {
					printStatement(statement.statements[i], indent + 2);
				}
			}
		}
		output.append("\n"); //$NON-NLS-1$
		return printIndent(indent).append('}');
	}

	private StringBuilder printLocalDeclaration(LocalDeclaration statement, int indent) {
		printAbstractVariableDeclarationAsExpression(statement, indent);
		switch (statement.getKind()) {
		case AbstractVariableDeclaration.ENUM_CONSTANT:
			return output.append(',');
		default:
			return output.append(';');
		}
	}

	private StringBuilder printAbstractVariableDeclarationAsExpression(AbstractVariableDeclaration avd, int indent) {
		printIndent(indent);
		printModifiers(avd.modifiers);
		if (avd.annotations != null) {
			printAnnotations(avd.annotations);
		}

		if (avd.type != null) {
			printTypeReference(avd.type).append(' ');
		}
		output.append(avd.name);
		switch (avd.getKind()) {
		case AbstractVariableDeclaration.ENUM_CONSTANT:
			if (avd.initialization != null) {
				printExpression(avd.initialization);
			}
			break;
		default:
			if (avd.initialization != null) {
				output.append(" = "); //$NON-NLS-1$
				printExpression(avd.initialization);
			}
		}
		return output;
	}

	private StringBuilder printBlock(Block b, int indent, boolean applyIndent) {
		if (applyIndent) {
			printIndent(indent);
		}
		output.append("{\n"); //$NON-NLS-1$
		printBody(b, indent);
		printIndent(indent);
		return output.append('}');
	}

	public StringBuilder printBody(Block b, int indent) {
		if (b.statements == null) {
			return output;
		}
		for (int i = 0; i < b.statements.length; i++) {
			printStatement(b.statements[i], indent + 1);
			output.append('\n');
		}
		return output;
	}

	private StringBuilder printTryStatement(TryStatement statement, int indent) {
		printIndent(indent).append("try "); //$NON-NLS-1$
		printBlock(statement.tryBlock, indent, false);
		// catches
		if (statement.catchBlocks != null) {
			for (int i = 0; i < statement.catchBlocks.length; i++) {
				// output.append('\n');
				// printIndent(indent).
				output.append(" catch ("); //$NON-NLS-1$
				printArgument(statement.catchArguments[i]).append(") "); //$NON-NLS-1$
				printBlock(statement.catchBlocks[i], indent, false);
				// statement.catchBlocks[i].printStatement(indent + 1, output);
			}
		}
		// finally
		if (statement.finallyBlock != null) {
			// output.append('\n');
			// printIndent(indent).
			output.append(" finally "); //$NON-NLS-1$
			printBlock(statement.finallyBlock, indent, false);// .printStatement(indent + 1, output);
		}
		return output;
	}

	private StringBuilder printAssignment(Assignment statement) {
		return printAssignment(statement, expressionLevel != 0);
	}

	private StringBuilder printAssignment(Assignment statement, boolean parens) {
		if (parens) {
			output.append('(');
		}
		printExpression(statement.lhs).append(" = ");
		printExpression(statement.expression);
		if (parens) {
			output.append(')');
		}
		return output;
	}

	private StringBuilder printMessageSendStatement(MessageSend ms) {
		if (!ms.receiver.isImplicitThis()) {
			printExpression(ms.receiver).append('.');
		}
		if (ms.typeArguments != null) {
			output.append('<');
			int max = ms.typeArguments.length - 1;
			for (int j = 0; j < max; j++) {
				printTypeReference(ms.typeArguments[j]);
				// ms.typeArguments[j].print(0, output);
				output.append(", ");//$NON-NLS-1$
			}
			printTypeReference(ms.typeArguments[max]);
			// ms.typeArguments[max].print(0, output);
			output.append('>');
		}
		output.append(ms.selector).append('(');
		if (ms.arguments != null) {
			for (int i = 0; i < ms.arguments.length; i++) {
				if (i > 0) {
					output.append(", "); //$NON-NLS-1$
				}
				printExpression(ms.arguments[i]);
			}
		}
		return output.append(')');
	}

	protected StringBuilder printQualifiedAllocationExpression(QualifiedAllocationExpression qae, int indent) {
		if (qae.enclosingInstance != null) {
			printExpression(qae.enclosingInstance).append('.');
		}
		printAllocationExpression(qae);
		if (qae.anonymousType != null) {
			printTypeDeclaration(qae.anonymousType, indent, true);
		}
		return output;
	}

	protected StringBuilder printTypeDeclaration(TypeDeclaration td, int indent, boolean isAnonymous) {
		if (td.javadoc != null) {
			throwit(td);
			// td.javadoc.print(indent, output);
		}
		if ((td.bits & ASTNode.IsAnonymousType) == 0) {
			printIndent(tab);
			printTypeDeclarationHeader(td);
		}
		printTypeDeclarationBody(td, indent, isAnonymous);
		return output;
	}

	public StringBuilder printTypeDeclarationBody(TypeDeclaration td, int indent, boolean isAnonymous) {
		output.append(" {"); //$NON-NLS-1$
		if (td.memberTypes != null) {
			for (int i = 0; i < td.memberTypes.length; i++) {
				if (td.memberTypes[i] != null) {
					output.append('\n');
					printTypeDeclaration(td.memberTypes[i], indent + 1, false);
				}
			}
		}
		if (td.fields != null) {
			for (int fieldI = 0; fieldI < td.fields.length; fieldI++) {
				if (td.fields[fieldI] != null) {
					output.append('\n');
					printFieldDeclaration(td.fields[fieldI], indent + 1);
				}
			}
		}
		if (td.methods != null) {
			for (int i = 0; i < td.methods.length; i++) {
				if (td.methods[i] != null) {
					AbstractMethodDeclaration amd = td.methods[i];
					if (amd instanceof MethodDeclaration) {
						output.append('\n');
						printMethodDeclaration(((MethodDeclaration) amd), indent + 1);
					} else if (amd instanceof ConstructorDeclaration) {
						if (!isAnonymous) {
							output.append('\n');
							// likely to be just a ctor with name 'x' as set in TypeDeclaration.createDefaultConstructorWithBinding
							printConstructorDeclaration(((ConstructorDeclaration) amd), indent + 1);
						}
					} else {
						throwit(amd);
					}
				}
			}
		}
		output.append('\n');
		return printIndent(indent).append('}');
	}

	protected StringBuilder printFieldDeclaration(FieldDeclaration fd, int indent) {
		printIndent(indent);
		printModifiers(fd.modifiers);
		if (fd.annotations != null) {
			printAnnotations(fd.annotations);
		}

		if (fd.type != null) {
			printTypeReference(fd.type).append(' ');
		}
		output.append(fd.name);
		switch (fd.getKind()) {
		case AbstractVariableDeclaration.ENUM_CONSTANT:
			if (fd.initialization != null) {
				printExpression(fd.initialization);
			}
			break;
		default:
			if (fd.initialization != null) {
				output.append(" = "); //$NON-NLS-1$
				printExpression(fd.initialization);
			}
		}
		output.append(';');
		return output;
	}

	protected StringBuilder printConstructorDeclaration(ConstructorDeclaration amd, int tab) {
		if (amd.javadoc != null) {
			throwit();
			// amd.javadoc.print(tab, output);
		}
		printIndent(tab);
		if (amd.annotations != null) {
			printAnnotations(amd.annotations);
		}
		printModifiers(amd.modifiers);

		TypeParameter[] typeParams = amd.typeParameters();
		if (typeParams != null) {
			output.append('<');
			int max = typeParams.length - 1;
			for (int j = 0; j < max; j++) {
				printTypeParameter(typeParams[j]);
				// typeParams[j].print(0, output);
				output.append(", ");//$NON-NLS-1$
			}
			printTypeParameter(typeParams[max]);
			output.append('>');
		}

		// TODO confirm selector is right name
		output.append(amd.selector).append('(');
		if (amd.arguments != null) {
			for (int i = 0; i < amd.arguments.length; i++) {
				if (i > 0) {
					output.append(", "); //$NON-NLS-1$
				}
				printArgument(amd.arguments[i]);
			}
		}
		output.append(')');
		if (amd.thrownExceptions != null) {
			output.append(" throws "); //$NON-NLS-1$
			for (int i = 0; i < amd.thrownExceptions.length; i++) {
				if (i > 0) {
					output.append(", "); //$NON-NLS-1$
				}
				throwit();
				// this.thrownExceptions[i].print(0, output);
			}
		}
		printBody(amd, tab + 1);
		return output;
	}

	private StringBuilder printMethodDeclaration(MethodDeclaration amd, int tab) {

		if (amd.javadoc != null) {
			throwit();
			// amd.javadoc.print(tab, output);
		}
		printIndent(tab);
		if (amd.annotations != null) {
			printAnnotations(amd.annotations);
		}
		printModifiers(amd.modifiers);

		TypeParameter[] typeParams = amd.typeParameters();
		if (typeParams != null) {
			output.append('<');
			int max = typeParams.length - 1;
			for (int j = 0; j < max; j++) {
				printTypeParameter(typeParams[j]);
				// typeParams[j].print(0, output);
				output.append(", ");//$NON-NLS-1$
			}
			printTypeParameter(typeParams[max]);
			output.append('>');
		}

		printReturnType(amd.returnType).append(amd.selector).append('(');
		if (amd.arguments != null) {
			for (int i = 0; i < amd.arguments.length; i++) {
				if (i > 0) {
					output.append(", "); //$NON-NLS-1$
				}
				printArgument(amd.arguments[i]);
			}
		}
		output.append(')');
		if (amd.thrownExceptions != null) {
			output.append(" throws "); //$NON-NLS-1$
			for (int i = 0; i < amd.thrownExceptions.length; i++) {
				if (i > 0) {
					output.append(", "); //$NON-NLS-1$
				}
				throwit();
				// this.thrownExceptions[i].print(0, output);
			}
		}
		printBody(amd, tab + 1);
		return output;
	}

	public StringBuilder printReturnType(TypeReference tr) {
		if (tr == null) {
			return output;
		}
		return printExpression(tr).append(' ');
	}

	public final static int kind(int flags) {
		switch (flags & (ClassFileConstants.AccInterface | ClassFileConstants.AccAnnotation | ClassFileConstants.AccEnum)) {
		case ClassFileConstants.AccInterface:
			return TypeDeclaration.INTERFACE_DECL;
		case ClassFileConstants.AccInterface | ClassFileConstants.AccAnnotation:
			return TypeDeclaration.ANNOTATION_TYPE_DECL;
		case ClassFileConstants.AccEnum:
			return TypeDeclaration.ENUM_DECL;
		default:
			return TypeDeclaration.CLASS_DECL;
		}
	}

	protected StringBuilder printTypeDeclarationHeader(TypeDeclaration td) {
		printModifiers(td.modifiers);
		if (td.annotations != null) {
			printAnnotations(td.annotations);
		}

		switch (kind(td.modifiers)) {
		case TypeDeclaration.CLASS_DECL:
			output.append("class "); //$NON-NLS-1$
			break;
		case TypeDeclaration.INTERFACE_DECL:
			output.append("interface "); //$NON-NLS-1$
			break;
		case TypeDeclaration.ENUM_DECL:
			output.append("enum "); //$NON-NLS-1$
			break;
		case TypeDeclaration.ANNOTATION_TYPE_DECL:
			output.append("@interface "); //$NON-NLS-1$
			break;
		}
		output.append(td.name);
		if (td.typeParameters != null) {
			output.append("<");//$NON-NLS-1$
			for (int i = 0; i < td.typeParameters.length; i++) {
				if (i > 0) {
					output.append(", "); //$NON-NLS-1$
				}
				printTypeParameter(td.typeParameters[i]);
				// this.typeParameters[i].print(0, output);
			}
			output.append(">");//$NON-NLS-1$
		}
		if (td.superclass != null) {
			output.append(" extends "); //$NON-NLS-1$
			printTypeReference(td.superclass);
		}
		if (td.superInterfaces != null && td.superInterfaces.length > 0) {
			switch (kind(td.modifiers)) {
			case TypeDeclaration.CLASS_DECL:
			case TypeDeclaration.ENUM_DECL:
				output.append(" implements "); //$NON-NLS-1$
				break;
			case TypeDeclaration.INTERFACE_DECL:
			case TypeDeclaration.ANNOTATION_TYPE_DECL:
				output.append(" extends "); //$NON-NLS-1$
				break;
			}
			for (int i = 0; i < td.superInterfaces.length; i++) {
				if (i > 0) {
					output.append(", "); //$NON-NLS-1$
				}
				printTypeReference(td.superInterfaces[i]);
			}
		}
		return output;
	}

	protected StringBuilder printTypeParameter(TypeParameter tp) {
		output.append(tp.name);
		if (tp.type != null) {
			output.append(" extends "); //$NON-NLS-1$
			printTypeReference(tp.type);
		}
		if (tp.bounds != null) {
			for (int i = 0; i < tp.bounds.length; i++) {
				output.append(" & "); //$NON-NLS-1$
				printTypeReference(tp.bounds[i]);
			}
		}
		return output;
	}

	protected StringBuilder printAllocationExpression(AllocationExpression ae) {
		if (ae.type != null) { // type null for enum constant initializations
			output.append("new "); //$NON-NLS-1$
		}
		if (ae.typeArguments != null) {
			output.append('<');
			int max = ae.typeArguments.length - 1;
			for (int j = 0; j < max; j++) {
				printTypeReference(ae.typeArguments[j]);
				output.append(", ");//$NON-NLS-1$
			}
			printTypeReference(ae.typeArguments[max]);
			output.append('>');
		}
		if (ae.type != null) { // type null for enum constant initializations
			printExpression(ae.type);
		}
		output.append('(');
		if (ae.arguments != null) {
			for (int i = 0; i < ae.arguments.length; i++) {
				if (i > 0) {
					output.append(", "); //$NON-NLS-1$
				}
				printExpression(ae.arguments[i]);
			}
		}
		return output.append(')');
	}
}
