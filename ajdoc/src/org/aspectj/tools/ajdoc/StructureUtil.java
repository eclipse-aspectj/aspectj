/* *******************************************************************
 * Copyright (c) 2003 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Mik Kersten     initial implementation 
 * ******************************************************************/
 package org.aspectj.tools.ajdoc;

import java.io.File;
import java.util.Iterator;

import org.aspectj.asm.AsmManager;
import org.aspectj.asm.IProgramElement;

/**
 * @author Mik Kersten
 */
public class StructureUtil {
	
	public static String getPackageDeclarationFromFile(File file) {
    	IProgramElement fileNode = (IProgramElement)AsmManager.getDefault().getHierarchy().findElementForSourceFile(file.getAbsolutePath());
    	String packageName = ((IProgramElement)fileNode.getChildren().get(0)).getPackageName();
    	return packageName;
	}
	
	public static String genSignature(IProgramElement node) {
		StringBuffer sb = new StringBuffer();
		
		String accessibility = node.getAccessibility().toString();
		if (!accessibility.equals("package")) {
			sb.append(accessibility);
			sb.append(' ');
		}
		
		String modifiers = "";
		for (Iterator modIt = node.getModifiers().iterator(); modIt.hasNext(); ) {
			modifiers += modIt.next() + " ";
		}
	
		if (node.getKind().equals(IProgramElement.Kind.METHOD) || 
			node.getKind().equals(IProgramElement.Kind.FIELD)) {
			sb.append(node.getCorrespondingType());
			sb.append(' ');
		}

		if (node.getKind().equals(IProgramElement.Kind.CLASS)) {
			sb.append("class ");
		} else if (node.getKind().equals(IProgramElement.Kind.INTERFACE)) {
			sb.append("interface ");
		} 

		sb.append(node.getName());
		
		if (node.getParameterTypes() != null ) {
			sb.append('('); 
			for (int i = 0; i < node.getParameterTypes().size(); i++) {
				sb.append((String)node.getParameterTypes().get(i));
				sb.append(' ');
				sb.append((String)node.getParameterNames().get(i));
				if (i < node.getParameterTypes().size()-1) {
					sb.append(", ");
				}
			}
			sb.append(')');
		}
		
		return sb.toString();
	}
}
