/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/

package org.aspectj.ajdt.internal.compiler.ast;

import java.lang.reflect.Modifier;

import org.aspectj.org.eclipse.jdt.core.compiler.CharOperation;
import org.aspectj.org.eclipse.jdt.internal.compiler.CompilationResult;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.Argument;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.Expression;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.FalseLiteral;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.MemberValuePair;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.NormalAnnotation;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.ReturnStatement;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.Statement;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.StringLiteral;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.TrueLiteral;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.aspectj.org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.parser.Parser;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.ResolvedMemberImpl;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.patterns.IfPointcut;
import org.aspectj.weaver.patterns.Pointcut;

/**
 * (formals*): ... if(expr) ...
 * 
 * generates the following: public static final boolean ajc$if_N(formals*,
 * [thisJoinPoints as needed]) { return expr; }
 * 
 * Here's the complicated bit, it deals with cflow: (a): ... this(a) &amp;&amp; cflow(if
 * (a == foo)) is an error. The way we capture this is: We generate the ajc$if
 * method with an (a) parameter, we let eclipse do the proper name binding. We
 * then, as a post pass (that we need to do anyway) look for the used
 * parameters. If a is used, we signal an error because a was not one of the
 * cflow variables. XXX we'll do this part after we do cflow
 * 
 * The IfPointcut pcd then generates itself always as a dynamic test, it has to
 * get the right parameters through any named pointcut references...
 */
public class IfPseudoToken extends PseudoToken {
	public Expression expr;
	public MethodDeclaration testMethod;
	private IfPointcut pointcut;

	public IfPseudoToken(Parser parser, Expression expr) {
		super(parser, "if", false);
		this.expr = expr;
	}

	public Pointcut maybeGetParsedPointcut() {
		if (expr instanceof FalseLiteral) {
			return IfPointcut.makeIfFalsePointcut(Pointcut.SYMBOLIC);
		} else if (expr instanceof TrueLiteral) {
			return IfPointcut.makeIfTruePointcut(Pointcut.SYMBOLIC);
		} else {
			pointcut = new IfPointcut(new ResolvedMemberImpl(Member.METHOD,
					UnresolvedType.OBJECT, 0, "if_", "()V"), 0);
		}
		return pointcut;

	}

	/**
	 * enclosingDec is either AdviceDeclaration or PointcutDeclaration
	 */
	public int postParse(TypeDeclaration typeDec,
			MethodDeclaration enclosingDec, int counter) {
		// typeDec.scope.problemReporter().signalError(sourceStart, sourceEnd,
		// "if pcd is not implemented in 1.1alpha1");
		// XXX need to implement correctly
		if (pointcut == null)
			return 0;

		testMethod = makeIfMethod(enclosingDec.compilationResult, enclosingDec, typeDec, counter);
		AstUtil.addMethodDeclaration(typeDec, testMethod);
		return 1;
	}

	private final static char[] CodeGenerationHint = "CodeGenerationHint".toCharArray();
	private final static char[] FullyQualifiedCodeGenerationHint = "org.aspectj.lang.annotation.control.CodeGenerationHint".toCharArray();
	private final static char[] IfNameSuffix = "ifNameSuffix".toCharArray();
	
	// XXX todo: make sure that errors in Arguments only get displayed once
	private MethodDeclaration makeIfMethod(CompilationResult result, MethodDeclaration enclosingDec, TypeDeclaration containingTypeDec, int counter) {
		MethodDeclaration ret = new IfMethodDeclaration(result, pointcut);
		ret.modifiers = ClassFileConstants.AccStatic | ClassFileConstants.AccFinal | ClassFileConstants.AccPublic;
		ret.returnType = AstUtil.makeTypeReference(TypeBinding.BOOLEAN);
		
		String nameSuffix = null;
		
		if (enclosingDec!=null && enclosingDec.annotations!=null) {
			NormalAnnotation interestingAnnotation = null;
			Annotation[] as = enclosingDec.annotations;
			if (as!=null) {
				for (int a = 0; a < as.length && interestingAnnotation == null; a++) {
					if (as[a] instanceof NormalAnnotation) {
						TypeReference tr = as[a].type;
						if (tr instanceof SingleTypeReference) {
							if (CharOperation.equals(CodeGenerationHint,((SingleTypeReference)tr).token)) {
								interestingAnnotation = (NormalAnnotation)as[a];
							}
						} else if (tr instanceof QualifiedTypeReference) {
							char[] qualifiedName = CharOperation.concatWith(((QualifiedTypeReference)tr).tokens,'.');
							if (CharOperation.equals(FullyQualifiedCodeGenerationHint,qualifiedName)) {
								interestingAnnotation = (NormalAnnotation)as[a];
							}
						}
					}
				}
			}
			if (interestingAnnotation!=null) {
				MemberValuePair[] memberValuePairs = interestingAnnotation.memberValuePairs;
				for (MemberValuePair memberValuePair: memberValuePairs) {
					if (CharOperation.equals(IfNameSuffix,memberValuePair.name) && (memberValuePair.value instanceof StringLiteral)) {
						nameSuffix = new String(((StringLiteral)memberValuePair.value).source());
					}
				}
			}
		}
		
		
		// create a more stable name 277508
		StringBuffer ifSelector = new StringBuffer();
		ifSelector.append("ajc$if$");
		if (nameSuffix == null || nameSuffix.length()==0) {
			boolean computedName = false;
			try {
				// possibly even better logic for more reliable name:
				if (enclosingDec instanceof AdviceDeclaration) {
					// name is ajc$if$<adviceSequenceNumber>[$<ifnumberinPcd>]$<hashcodeOfIfExpressionInHex>
					ifSelector.append(((AdviceDeclaration)enclosingDec).adviceSequenceNumberInType).append("$");
					if (counter!=0) {
						ifSelector.append(counter);
						ifSelector.append("$");
					}
					ifSelector.append(Integer.toHexString(expr.toString().hashCode()));
					computedName = true;
				} else if (enclosingDec instanceof PointcutDeclaration) {
					if (counter!=0) {
						ifSelector.append(counter);
						ifSelector.append("$");
					}
					StringBuilder toHash = new StringBuilder(((PointcutDeclaration) enclosingDec).getPointcutText());
					toHash.append(expr.toString());
					// name is pointcut selector then $if[$<ifnumberinpcd>]$<hashcodeofpointcuttextandexpressiontext>
					ifSelector.append(Integer.toHexString(toHash.toString().hashCode()));
					computedName = true;
				}
			} catch (Throwable t) {
				throw new IllegalStateException(t);
				// let it build a name the 'old way'
			}
			if (!computedName) {
				ifSelector.append(Integer.toHexString(expr.sourceStart));
			}
		} else {
			ifSelector.append(nameSuffix);
		}

		
		ret.selector = ifSelector.toString().toCharArray();
		ret.arguments = makeArguments(enclosingDec, containingTypeDec);
		ret.statements = new Statement[] { new ReturnStatement(expr,
				expr.sourceStart, expr.sourceEnd) };
		return ret;
	}

	private Argument[] makeArguments(MethodDeclaration enclosingDec,
			TypeDeclaration containingTypeDec) {
		Argument[] baseArguments = enclosingDec.arguments;
		int len = baseArguments.length;
		if (enclosingDec instanceof AdviceDeclaration) {
			len = ((AdviceDeclaration) enclosingDec).baseArgumentCount;
		}

		Argument[] ret = new Argument[len];
		for (int i = 0; i < len; i++) {
			Argument a = baseArguments[i];
			ret[i] = new Argument(a.name, AstUtil.makeLongPos(a.sourceStart,
					a.sourceEnd), a.type, Modifier.FINAL);
		}
		ret = AdviceDeclaration.addTjpArguments(ret, containingTypeDec);

		return ret;
	}

}
