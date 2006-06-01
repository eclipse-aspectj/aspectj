/* *******************************************************************
 * Copyright (c) 2003 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Mik Kersten     initial implementation 
 * ******************************************************************/
 package org.aspectj.tools.ajdoc;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.aspectj.asm.AsmManager;
import org.aspectj.asm.IProgramElement;
import org.aspectj.asm.IRelationship;

/**
 * @author Mik Kersten
 */
public class StructureUtil {

	/**
	 * Calculate the targets for a given IProgramElement (and it's 
	 * immediate children if its not a type or if the child is
	 * CODE) and relationship kind
	 * 
	 * @return	null if a relationship of that kind is not found
	 */
	public static List /*String*/ getTargets(IProgramElement node, IRelationship.Kind kind) {
		return getTargets(node,kind,null);
	}
	
	/**
	 * Calculate the targets for a given IProgramElement (and it's immediate
	 * children if its not a type or if the child is CODE) and relationship 
	 * kind with the specified relationship name.
	 * 
	 * @return null if a relationship of that kind is not found
	 */
	public static List /*String*/ getTargets(IProgramElement node, IRelationship.Kind kind, String relName) {
		List relations = new ArrayList();
		List rels = AsmManager.getDefault().getRelationshipMap().get(node);
		if (rels != null) {
			relations.addAll(rels);
		}
	    for (Iterator iter = node.getChildren().iterator(); iter.hasNext();) {
			IProgramElement child = (IProgramElement) iter.next();
			// if we're not a type, or if we are and the child is code, then
			// we want to get the relationships for this child - this means that the
			// correct relationships appear against the type in the ajdoc
			if (!node.getKind().isType() 
					|| child.getKind().equals(IProgramElement.Kind.CODE) ) {
				List childRelations = AsmManager.getDefault().getRelationshipMap().get(child);
				if (childRelations != null) {
					for (Iterator iterator = childRelations.iterator(); iterator
							.hasNext();) {
						IRelationship rel = (IRelationship) iterator.next();
						if (!relations.contains(rel)) {
							relations.add(rel);
						}
					}
				}					
			}
		}			
	    if (relations == null || relations.isEmpty()) return null;
		List targets = new ArrayList(); 
		for (Iterator it = relations.iterator(); it.hasNext(); ) {
	      	IRelationship rtn = (IRelationship)it.next();
	      	if (rtn.getKind().equals(kind)
	      			&& ((relName != null  && relName.equals(rtn.getName()))
	      					|| relName == null)){
	      		List targs = rtn.getTargets();
	      		for (Iterator iter = targs.iterator(); iter.hasNext();) {
					String element = (String) iter.next();
					if (!targets.contains(element)) {
						targets.add(element);
					}
				}
	      	}
	     }
		return targets;		
	}
	
	static List /*IProgramElement */ getDeclareInterTypeTargets(IProgramElement node, IProgramElement.Kind kind) {
		List targets = new ArrayList();
		List stringTargets = StructureUtil.getTargets(node,IRelationship.Kind.DECLARE_INTER_TYPE);
		if (stringTargets == null) {
			return null;
		}
		for (Iterator iter = stringTargets.iterator(); iter.hasNext();) {
			String element = (String) iter.next();
			IProgramElement ipe = AsmManager.getDefault().getHierarchy().findElementForHandle(element);
			if (ipe != null && ipe.getKind().equals(kind)) {
				targets.add(ipe);
			}
		}
		return targets;
	}

	public static List/*String*/ getDeclareTargets(IProgramElement node) {
	    List relations = AsmManager.getDefault().getRelationshipMap().get(node);
		List targets = null; 
	    if (relations == null) return null;
		for (Iterator it = relations.iterator(); it.hasNext(); ) {
	      	IRelationship rtn = (IRelationship)it.next();
	      	if (rtn.getKind().isDeclareKind()) {
	      		targets = rtn.getTargets();
	      	}
	     }
		return targets;
	}
	
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

	public static boolean isAnonymous(IProgramElement node) {
		boolean isIntName = true;
		try {
			Integer.valueOf(node.getName());
		} catch (NumberFormatException nfe) {
			// !!! using exceptions for logic, fix
			isIntName = false;
		}
//		System.err.println(">>>>>>>> " + node.getName());
		return isIntName || node.getName().startsWith("new ");
//		return isIntName;
//		if (!isIntName) {
//			 
//			return node.getName().startsWith("new ");
//		} else {
//			return false; 
//		}
	}

	/**
	 * @return	same path, but ending in ".java" instead of ".aj"
	 */
	public static String translateAjPathName(String path) {
    	if (path.endsWith(".aj")) {
    		path = path.substring(0, path.lastIndexOf(".aj")) + ".java";
    	} 
    	return path;
	}
}
