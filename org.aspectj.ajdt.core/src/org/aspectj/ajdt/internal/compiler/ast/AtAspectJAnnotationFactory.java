/* *******************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *   Adrian Colyer			Initial implementation
 * ******************************************************************/
package org.aspectj.ajdt.internal.compiler.ast;

import org.aspectj.org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.Expression;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.IntLiteral;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.MemberValuePair;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.NormalAnnotation;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.StringLiteral;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.TrueLiteral;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.FalseLiteral;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.TagBits;

/**
 * @author colyer
 * Creates @AspectJ annotations for use by AtAspectJVisitor
 */
public class AtAspectJAnnotationFactory {
	
	static final char[] org = "org".toCharArray();
	static final char[] aspectj = "aspectj".toCharArray();
	static final char[] lang = "lang".toCharArray();
	static final char[] internal = "internal".toCharArray();
	static final char[] annotation = "annotation".toCharArray();
	static final char[] value = "value".toCharArray();
	
	static final char[] aspect = "Aspect".toCharArray();
	static final char[] privileged = "ajcPrivileged".toCharArray();
	static final char[] before = "Before".toCharArray();
	static final char[] after = "After".toCharArray();
	static final char[] afterReturning = "AfterReturning".toCharArray();
	static final char[] afterThrowing = "AfterThrowing".toCharArray();
	static final char[] around = "Around".toCharArray();
    static final char[] pointcut = "Pointcut".toCharArray(); 
	static final char[] declareErrorOrWarning = "ajcDeclareEoW".toCharArray();
	static final char[] declareParents = "ajcDeclareParents".toCharArray();
	static final char[] declareSoft = "ajcDeclareSoft".toCharArray();
	static final char[] declarePrecedence = "ajcDeclarePrecedence".toCharArray();
	static final char[] declareAnnotation = "ajcDeclareAnnotation".toCharArray();
	static final char[] itdAnnotation = "ajcITD".toCharArray();

	/**
	 * Create an @Aspect annotation for a code style aspect declaration starting at
	 * the given position in the source file
	 */
	public static Annotation createAspectAnnotation(String perclause, int pos) {
		char[][] typeName = new char[][] {org,aspectj,lang,annotation,aspect};
		long[] positions = new long[] {pos,pos,pos,pos,pos};
		TypeReference orgAspectJLangAnnotationAspect = new QualifiedTypeReference(typeName,positions);
		NormalAnnotation atAspectAnnotation = new NormalAnnotation(orgAspectJLangAnnotationAspect,pos);
		if (!perclause.equals("")) {
			// we have to set the value
			Expression perclauseExpr = new StringLiteral(perclause.toCharArray(),pos,pos,1);
			MemberValuePair[] mvps = new MemberValuePair[1];
			mvps[0] = new MemberValuePair(value,pos,pos,perclauseExpr);
			atAspectAnnotation.memberValuePairs = mvps;
		}
		return atAspectAnnotation;
	}
	
	public static Annotation createPrivilegedAnnotation(int pos) {
		char[][] typeName = new char[][] {org,aspectj,internal,lang,annotation,privileged};
		long[] positions = new long[] {pos,pos,pos,pos,pos,pos};
		TypeReference annType = new QualifiedTypeReference(typeName,positions);
		NormalAnnotation ann = new NormalAnnotation(annType,pos);
		return ann;
	}

	public static Annotation createBeforeAnnotation(String pointcutExpression, String argNames, int pos) {
		char[][] typeName = new char[][] {org,aspectj,lang,annotation,before};
		long[] positions = new long[] {pos,pos,pos,pos,pos};
		TypeReference annType = new QualifiedTypeReference(typeName,positions);
		NormalAnnotation ann = new NormalAnnotation(annType,pos);
		Expression pcExpr = new StringLiteral(pointcutExpression.toCharArray(),pos,pos,1);
		MemberValuePair[] mvps = new MemberValuePair[2];
		mvps[0] = new MemberValuePair("value".toCharArray(),pos,pos,pcExpr);
		Expression argNamesExpr = new StringLiteral(argNames.toCharArray(),pos,pos,1);
		mvps[1] = new MemberValuePair("argNames".toCharArray(),pos,pos,argNamesExpr);
		ann.memberValuePairs = mvps;
		return ann;
	}

	public static Annotation createAfterAnnotation(String pointcutExpression, String argNames, int pos) {
		char[][] typeName = new char[][] {org,aspectj,lang,annotation,after};
		long[] positions = new long[] {pos,pos,pos,pos,pos};
		TypeReference annType = new QualifiedTypeReference(typeName,positions);
		NormalAnnotation ann = new NormalAnnotation(annType,pos);
		Expression pcExpr = new StringLiteral(pointcutExpression.toCharArray(),pos,pos,1);
		MemberValuePair[] mvps = new MemberValuePair[2];
		mvps[0] = new MemberValuePair("value".toCharArray(),pos,pos,pcExpr);
		Expression argNamesExpr = new StringLiteral(argNames.toCharArray(),pos,pos,1);
		mvps[1] = new MemberValuePair("argNames".toCharArray(),pos,pos,argNamesExpr);
		ann.memberValuePairs = mvps;
		return ann;
	}

	public static Annotation createAfterReturningAnnotation(String pointcutExpression,  String argNames, String extraArgumentName, int pos) {
		char[][] typeName = new char[][] {org,aspectj,lang,annotation,afterReturning};
		long[] positions = new long[] {pos,pos,pos,pos,pos};
		TypeReference annType = new QualifiedTypeReference(typeName,positions);
		NormalAnnotation ann = new NormalAnnotation(annType,pos);
		Expression pcExpr = new StringLiteral(pointcutExpression.toCharArray(),pos,pos,1);
		MemberValuePair[] mvps = new MemberValuePair[3];
		mvps[0] = new MemberValuePair("pointcut".toCharArray(),pos,pos,pcExpr);
		Expression argExpr = new StringLiteral(extraArgumentName.toCharArray(),pos,pos,1);
		mvps[1] = new MemberValuePair("returning".toCharArray(),pos,pos,argExpr);
		Expression argNamesExpr = new StringLiteral(argNames.toCharArray(),pos,pos,1);
		mvps[2] = new MemberValuePair("argNames".toCharArray(),pos,pos,argNamesExpr);
		ann.memberValuePairs = mvps;
		return ann;
	}

	public static Annotation createAfterThrowingAnnotation(String pointcutExpression, String argNames, String extraArgumentName, int pos) {
		char[][] typeName = new char[][] {org,aspectj,lang,annotation,afterThrowing};
		long[] positions = new long[] {pos,pos,pos,pos,pos};
		TypeReference annType = new QualifiedTypeReference(typeName,positions);
		NormalAnnotation ann = new NormalAnnotation(annType,pos);
		Expression pcExpr = new StringLiteral(pointcutExpression.toCharArray(),pos,pos,1);
		MemberValuePair[] mvps = new MemberValuePair[3];
		mvps[0] = new MemberValuePair("pointcut".toCharArray(),pos,pos,pcExpr);
		Expression argExpr = new StringLiteral(extraArgumentName.toCharArray(),pos,pos,1);
		mvps[1] = new MemberValuePair("throwing".toCharArray(),pos,pos,argExpr);
		Expression argNamesExpr = new StringLiteral(argNames.toCharArray(),pos,pos,1);
		mvps[2] = new MemberValuePair("argNames".toCharArray(),pos,pos,argNamesExpr);
		ann.memberValuePairs = mvps;
		return ann;
	}

	public static Annotation createAroundAnnotation(String pointcutExpression, String argNames, int pos) {
		char[][] typeName = new char[][] {org,aspectj,lang,annotation,around};
		long[] positions = new long[] {pos,pos,pos,pos,pos};
		TypeReference annType = new QualifiedTypeReference(typeName,positions);
		NormalAnnotation ann = new NormalAnnotation(annType,pos);
		Expression pcExpr = new StringLiteral(pointcutExpression.toCharArray(),pos,pos,1);
		MemberValuePair[] mvps = new MemberValuePair[2];
		mvps[0] = new MemberValuePair("value".toCharArray(),pos,pos,pcExpr);
		Expression argNamesExpr = new StringLiteral(argNames.toCharArray(),pos,pos,1);
		mvps[1] = new MemberValuePair("argNames".toCharArray(),pos,pos,argNamesExpr);
		ann.memberValuePairs = mvps;
		return ann;
	}

	public static Annotation createPointcutAnnotation(String pointcutExpression, String argNames, int pos) {
		char[][] typeName = new char[][] {org,aspectj,lang,annotation,pointcut};
		long[] positions = new long[] {pos,pos,pos,pos,pos};
		TypeReference annType = new QualifiedTypeReference(typeName,positions);
		NormalAnnotation ann = new NormalAnnotation(annType,pos);
		Expression pcExpr = new StringLiteral(pointcutExpression.toCharArray(),pos,pos,1);
		MemberValuePair[] mvps = new MemberValuePair[2];
		mvps[0] = new MemberValuePair("value".toCharArray(),pos,pos,pcExpr);
		Expression argExpr = new StringLiteral(argNames.toCharArray(),pos,pos,1);
		mvps[1] = new MemberValuePair("argNames".toCharArray(),pos,pos,argExpr);
		ann.memberValuePairs = mvps;
		return ann;	
	}

	public static Annotation createDeclareErrorOrWarningAnnotation(String pointcutExpression, String message, boolean isError, int pos) {
		char[][] typeName = new char[][] {org,aspectj,internal,lang,annotation,declareErrorOrWarning};
		long[] positions = new long[typeName.length];
		for (int i = 0; i < positions.length; i++) positions[i] = pos;
		TypeReference annType = new QualifiedTypeReference(typeName,positions);
		NormalAnnotation ann = new NormalAnnotation(annType,pos);
		Expression pcutExpr = new StringLiteral(pointcutExpression.toCharArray(),pos,pos,1);
		Expression msgExpr = new StringLiteral(message.toCharArray(),pos,pos,1);
		Expression isErrorExpr;
		if (isError) {
			isErrorExpr = new TrueLiteral(pos,pos);
		} else {
			isErrorExpr =  new FalseLiteral(pos,pos);
		}
		MemberValuePair[] mvps = new MemberValuePair[3];
		mvps[0] = new MemberValuePair("pointcut".toCharArray(),pos,pos,pcutExpr);
		mvps[1] = new MemberValuePair("message".toCharArray(),pos,pos,msgExpr);
		mvps[2] = new MemberValuePair("isError".toCharArray(),pos,pos,isErrorExpr);
		ann.memberValuePairs = mvps;
		return ann;
	}
	
	public static Annotation createDeclareParentsAnnotation(String childPattern, String parentPatterns, boolean isExtends, int pos) {
		char[][] typeName = new char[][] {org,aspectj,internal,lang,annotation,declareParents};
		long[] positions = new long[typeName.length];
		for (int i = 0; i < positions.length; i++) positions[i] = pos;
		TypeReference annType = new QualifiedTypeReference(typeName,positions);
		NormalAnnotation ann = new NormalAnnotation(annType,pos);
		Expression targetExpression = new StringLiteral(childPattern.toCharArray(),pos,pos,1);
		Expression parentsExpression = new StringLiteral(parentPatterns.toCharArray(),pos,pos,1);
		Expression isExtendsExpression;
		if (isExtends) {
			isExtendsExpression = new TrueLiteral(pos,pos);
		} else {
			isExtendsExpression = new FalseLiteral(pos,pos);
		}
		MemberValuePair[] mvps = new MemberValuePair[3];
		mvps[0] = new MemberValuePair("targetTypePattern".toCharArray(),pos,pos,targetExpression);
		mvps[1] = new MemberValuePair("parentTypes".toCharArray(),pos,pos,parentsExpression);
		mvps[2] = new MemberValuePair("isExtends".toCharArray(),pos,pos,isExtendsExpression);
		ann.memberValuePairs = mvps;
		return ann;
	}
	
	public static Annotation createDeclareSoftAnnotation(String pointcutExpression, String exceptionType, int pos) {
		char[][] typeName = new char[][] {org,aspectj,internal,lang,annotation,declareSoft};
		long[] positions = new long[typeName.length];
		for (int i = 0; i < positions.length; i++) positions[i] = pos;
		TypeReference annType = new QualifiedTypeReference(typeName,positions);
		NormalAnnotation ann = new NormalAnnotation(annType,pos);
		Expression pcutExpr = new StringLiteral(pointcutExpression.toCharArray(),pos,pos,1);
		Expression exExpr = new StringLiteral(exceptionType.toCharArray(),pos,pos,1);
		MemberValuePair[] mvps = new MemberValuePair[2];
		mvps[0] = new MemberValuePair("pointcut".toCharArray(),pos,pos,pcutExpr);
		mvps[1] = new MemberValuePair("exceptionType".toCharArray(),pos,pos,exExpr);
		ann.memberValuePairs = mvps;
		return ann;
	}
	
	public static Annotation createDeclareAnnAnnotation(String patternString, String annString, String kind, int pos) {
		char[][] typeName = new char[][] {org,aspectj,internal,lang,annotation,declareAnnotation};
		long[] positions = new long[typeName.length];
		for (int i = 0; i < positions.length; i++) positions[i] = pos;
		TypeReference annType = new QualifiedTypeReference(typeName,positions);
		NormalAnnotation ann = new NormalAnnotation(annType,pos);
		Expression pattExpr = new StringLiteral(patternString.toCharArray(),pos,pos,1);
		Expression annExpr = new StringLiteral(annString.toCharArray(),pos,pos,1);
		Expression kindExpr = new StringLiteral(kind.toCharArray(),pos,pos,1);
		MemberValuePair[] mvps = new MemberValuePair[3];
		mvps[0] = new MemberValuePair("pattern".toCharArray(),pos,pos,pattExpr);
		mvps[1] = new MemberValuePair("annotation".toCharArray(),pos,pos,annExpr);
		mvps[2] = new MemberValuePair("kind".toCharArray(),pos,pos,kindExpr);
		ann.memberValuePairs = mvps;
		return ann;
	}
	
	public static Annotation createITDAnnotation(char[] targetTypeName, int modifiers, char[] name, int pos) {
		char[][] typeName = new char[][] {org,aspectj,internal,lang,annotation,itdAnnotation};
		long[] positions = new long[typeName.length];
		for (int i = 0; i < positions.length; i++) positions[i] = pos;
		TypeReference annType = new QualifiedTypeReference(typeName,positions);
		NormalAnnotation ann = new NormalAnnotation(annType,pos);
		Expression targetExpr = new StringLiteral(targetTypeName,pos,pos,1);
		Expression nameExpr = new StringLiteral(name,pos,pos,1);
		Expression modsExpr = IntLiteral.buildIntLiteral(Integer.toString(modifiers).toCharArray(),pos,pos);
		MemberValuePair[] mvps = new MemberValuePair[3];
		mvps[0] = new MemberValuePair("targetType".toCharArray(),pos,pos,targetExpr);
		mvps[1] = new MemberValuePair("name".toCharArray(),pos,pos,nameExpr);
		mvps[2] = new MemberValuePair("modifiers".toCharArray(),pos,pos,modsExpr);
		ann.memberValuePairs = mvps;
		return ann;		
	}
	
	public static Annotation createDeclarePrecedenceAnnotation(String pointcutExpression, int pos) {
		char[][] typeName = new char[][] {org,aspectj,internal,lang,annotation,declarePrecedence};
		return makeSingleStringMemberAnnotation(typeName, pos, pointcutExpression);

	}
	
	private static Annotation makeSingleStringMemberAnnotation(char[][] name, int pos, String annValue) {
		long[] positions = new long[name.length];
		for (int i = 0; i < positions.length; i++) positions[i] = pos;
		TypeReference annType = new QualifiedTypeReference(name,positions);
		NormalAnnotation ann = new NormalAnnotation(annType,pos);
		Expression valueExpr = new StringLiteral(annValue.toCharArray(),pos,pos,1);
		MemberValuePair[] mvps = new MemberValuePair[1];
		mvps[0] = new MemberValuePair(value,pos,pos,valueExpr);
		ann.memberValuePairs = mvps;
		return ann;		
	}

	public static void addAnnotation(AjMethodDeclaration decl, Annotation annotation, BlockScope scope) {
		if (decl.annotations == null) {
			decl.annotations = new Annotation[] { annotation };
		} else {
			Annotation[] old = decl.annotations;
			decl.annotations = new Annotation[old.length +1];
			System.arraycopy(old,0,decl.annotations,0,old.length);
			decl.annotations[old.length] = annotation;
		}
		if (decl.binding!= null) {
			if ((decl.binding.tagBits & TagBits.AnnotationResolved) != 0) {
				annotation.resolve(scope);
			}
		}
	}

}
