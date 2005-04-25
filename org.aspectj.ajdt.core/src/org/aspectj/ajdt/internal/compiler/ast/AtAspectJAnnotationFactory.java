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
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.MemberValuePair;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.NormalAnnotation;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.StringLiteral;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeReference;

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
			Expression perclauseExpr = new StringLiteral(perclause.toCharArray(),pos,pos);
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

	public static Annotation createBeforeAnnotation(String pointcutExpression, int pos) {
		char[][] typeName = new char[][] {org,aspectj,lang,annotation,before};
		long[] positions = new long[] {pos,pos,pos,pos,pos};
		TypeReference annType = new QualifiedTypeReference(typeName,positions);
		NormalAnnotation ann = new NormalAnnotation(annType,pos);
		Expression pcExpr = new StringLiteral(pointcutExpression.toCharArray(),pos,pos);
		MemberValuePair[] mvps = new MemberValuePair[1];
		mvps[0] = new MemberValuePair(value,pos,pos,pcExpr);
		ann.memberValuePairs = mvps;
		return ann;
	}

	public static Annotation createAfterAnnotation(String pointcutExpression, int pos) {
		char[][] typeName = new char[][] {org,aspectj,lang,annotation,after};
		long[] positions = new long[] {pos,pos,pos,pos,pos};
		TypeReference annType = new QualifiedTypeReference(typeName,positions);
		NormalAnnotation ann = new NormalAnnotation(annType,pos);
		Expression pcExpr = new StringLiteral(pointcutExpression.toCharArray(),pos,pos);
		MemberValuePair[] mvps = new MemberValuePair[1];
		mvps[0] = new MemberValuePair(value,pos,pos,pcExpr);
		ann.memberValuePairs = mvps;
		return ann;
	}

	public static Annotation createAfterReturningAnnotation(String pointcutExpression, String extraArgumentName, int pos) {
		char[][] typeName = new char[][] {org,aspectj,lang,annotation,afterReturning};
		long[] positions = new long[] {pos,pos,pos,pos,pos};
		TypeReference annType = new QualifiedTypeReference(typeName,positions);
		NormalAnnotation ann = new NormalAnnotation(annType,pos);
		Expression pcExpr = new StringLiteral(pointcutExpression.toCharArray(),pos,pos);
		MemberValuePair[] mvps = new MemberValuePair[2];
		mvps[0] = new MemberValuePair("pointcut".toCharArray(),pos,pos,pcExpr);
		Expression argExpr = new StringLiteral(extraArgumentName.toCharArray(),pos,pos);
		mvps[1] = new MemberValuePair("returning".toCharArray(),pos,pos,argExpr);
		ann.memberValuePairs = mvps;
		return ann;
	}

	public static Annotation createAfterThrowingAnnotation(String pointcutExpression, String extraArgumentName, int pos) {
		char[][] typeName = new char[][] {org,aspectj,lang,annotation,afterThrowing};
		long[] positions = new long[] {pos,pos,pos,pos,pos};
		TypeReference annType = new QualifiedTypeReference(typeName,positions);
		NormalAnnotation ann = new NormalAnnotation(annType,pos);
		Expression pcExpr = new StringLiteral(pointcutExpression.toCharArray(),pos,pos);
		MemberValuePair[] mvps = new MemberValuePair[2];
		mvps[0] = new MemberValuePair("pointcut".toCharArray(),pos,pos,pcExpr);
		Expression argExpr = new StringLiteral(extraArgumentName.toCharArray(),pos,pos);
		mvps[1] = new MemberValuePair("throwing".toCharArray(),pos,pos,argExpr);
		ann.memberValuePairs = mvps;
		return ann;
	}

	public static Annotation createAroundAnnotation(String pointcutExpression, int pos) {
		char[][] typeName = new char[][] {org,aspectj,lang,annotation,around};
		long[] positions = new long[] {pos,pos,pos,pos,pos};
		TypeReference annType = new QualifiedTypeReference(typeName,positions);
		NormalAnnotation ann = new NormalAnnotation(annType,pos);
		Expression pcExpr = new StringLiteral(pointcutExpression.toCharArray(),pos,pos);
		MemberValuePair[] mvps = new MemberValuePair[1];
		mvps[0] = new MemberValuePair(value,pos,pos,pcExpr);
		ann.memberValuePairs = mvps;
		return ann;
	}

	public static Annotation createPointcutAnnotation(String pointcutExpression, int pos) {
		char[][] typeName = new char[][] {org,aspectj,lang,annotation,pointcut};
		long[] positions = new long[] {pos,pos,pos,pos,pos};
		TypeReference annType = new QualifiedTypeReference(typeName,positions);
		NormalAnnotation ann = new NormalAnnotation(annType,pos);
		Expression pcExpr = new StringLiteral(pointcutExpression.toCharArray(),pos,pos);
		MemberValuePair[] mvps = new MemberValuePair[1];
		mvps[0] = new MemberValuePair(value,pos,pos,pcExpr);
		ann.memberValuePairs = mvps;
		return ann;
	}
}
