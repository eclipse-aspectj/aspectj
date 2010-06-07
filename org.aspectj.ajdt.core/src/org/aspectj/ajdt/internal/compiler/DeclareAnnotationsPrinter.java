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
//package org.aspectj.ajdt.internal.compiler;
//
//import org.aspectj.ajdt.internal.compiler.ast.DeclareAnnotationDeclaration;
//import org.aspectj.org.eclipse.jdt.internal.compiler.ast.Annotation;
//import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.MethodScope;
//
//class DeclareAnnotationsPrinter extends HelperPrinter {
//
//	private DeclareAnnotationDeclaration dad;
//
//	DeclareAnnotationsPrinter(DeclareAnnotationDeclaration md, MethodScope mscope) {
//		super(mscope);
//		output = new StringBuilder();
//		this.amd = md;
//		this.dad = md;
//	}
//
//	public String print() {
//		return print(2);
//	}
//
//	public String print(int tab) {
//		output = new StringBuilder();
//		Annotation[] annos = dad.annotations;
//		int count = 0;
//		for (int a = 0; a < annos.length; a++) {
//			Annotation anno = annos[a];
//			String aa = anno.type.toString();
//			if (!aa.startsWith("org.aspectj")) {
//				if (count > 0) {
//					output.append(' ');
//				}
//				printAnnotation(anno);
//				count++;
//			}
//		}
//		return output.toString();
//	}
// }