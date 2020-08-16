/* *******************************************************************
 * Copyright (c) 2008 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Andy Clement     initial implementation 
 * ******************************************************************/
package org.aspectj.weaver;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class TestUtils {
	private static final String[] ZERO_STRINGS = new String[0];

	// For stringifying a delegate - extracted from AbstractReferenceTypeDelegate, not fixed up
	// /**
	// * Create the string representation for a delegate, allowing us to
	// * more easily compare delegate implementations.
	// */
	// public String stringifyDelegate() {
	//    	
	// StringBuffer result = new StringBuffer();
	// result.append("=== Delegate for "+getResolvedTypeX().getName()+"\n");
	//    	
	// result.append("isAspect?"+isAspect()+"\n");
	// result.append("isAnnotationStyleAspect?"+isAnnotationStyleAspect()+"\n");
	// result.append("isInterface?"+isInterface()+"\n");
	// result.append("isEnum?"+isEnum()+"\n");
	// result.append("isClass?"+isClass()+"\n");
	// result.append("-\n");
	// result.append("isAnnotation?"+isAnnotation()+"\n");
	// result.append("retentionPolicy="+getRetentionPolicy()+"\n");
	// result.append("canAnnotationTargetType?"+canAnnotationTargetType()+"\n");
	// AnnotationTargetKind[] kinds = getAnnotationTargetKinds();
	// if (kinds!=null && kinds.length>0) {
	// result.append("annotationTargetKinds:[");
	// for (int i = 0; i < kinds.length; i++) {
	// AnnotationTargetKind kind = kinds[i];
	// result.append(kind);
	// if ((i+1)<kinds.length) result.append(" ");
	// }
	// result.append("]\n");
	// }
	// result.append("isAnnotationWithRuntimeRetention?"+isAnnotationWithRuntimeRetention()+"\n");
	// result.append("-\n");
	//    	
	// result.append("isAnonymous?"+isAnonymous()+"\n");
	// result.append("isNested?"+isNested()+"\n");
	// result.append("-\n");
	//
	// result.append("isGeneric?"+isGeneric()+"\n");
	// result.append("declaredGenericSignature="+getDeclaredGenericSignature()+"\n");
	// result.append("-\n");
	//    	
	// AnnotationX[] axs = getAnnotations();
	// if (axs!=null && axs.length>0) {
	// result.append("getAnnotations() returns: "+axs.length+" annotations\n");
	// for (int i = 0; i < axs.length; i++) {
	// AnnotationX annotationX = axs[i];
	// result.append("  #"+i+") "+annotationX+"\n");
	// }
	// } else {
	// result.append("getAnnotations() returns nothing\n");
	// }
	// ResolvedType[] axtypes = getAnnotationTypes();
	// if (axtypes!=null && axtypes.length>0) {
	// result.append("getAnnotationTypes() returns: "+axtypes.length+" annotations\n");
	// for (int i = 0; i < axtypes.length; i++) {
	// ResolvedType annotation = axtypes[i];
	// result.append("  #"+i+") "+annotation+":"+annotation.getClass()+"\n");
	// }
	// } else {
	// result.append("getAnnotationTypes() returns nothing\n");
	// }
	//    	
	// result.append("isExposedToWeaver?"+isExposedToWeaver()+"\n");
	// result.append("getSuperclass?"+getSuperclass()+"\n");
	// result.append("getResolvedTypeX?"+getResolvedTypeX()+"\n");
	// result.append("--\n");
	//    	
	// ResolvedMember[] fields = getDeclaredFields();
	// if (fields!=null && fields.length>0) {
	// result.append("The fields: "+fields.length+"\n");
	// for (int i = 0; i < fields.length; i++) {
	// ResolvedMember member = fields[i];
	// result.append("f"+i+") "+member.toDebugString()+"\n");
	// }
	// }
	// ResolvedMember[] methods = getDeclaredMethods();
	// if (methods!=null && methods.length>0) {
	// result.append("The methods: "+methods.length+"\n");
	// for (int i = 0; i < methods.length; i++) {
	// ResolvedMember member = methods[i];
	// result.append("m"+i+") "+member.toDebugString()+"\n");
	// }
	// }
	// ResolvedType[] interfaces = getDeclaredInterfaces();
	// if (interfaces!=null && interfaces.length>0) {
	// result.append("The interfaces: "+interfaces.length+"\n");
	// for (int i = 0; i < interfaces.length; i++) {
	// ResolvedType member = interfaces[i];
	// result.append("i"+i+") "+member+"\n");
	// }
	// }
	//
	// result.append("getModifiers?"+getModifiers()+"\n");
	//    	
	// result.append("perclause="+getPerClause()+"\n");
	//    	
	// result.append("aj:weaverstate="+getWeaverState()+"\n");
	//    	
	// ResolvedMember[] pointcuts = getDeclaredPointcuts();
	// if (pointcuts!=null && pointcuts.length>0) {
	// result.append("The pointcuts: "+pointcuts.length+"\n");
	//    		
	// // Sort the damn things
	// List sortedSetOfPointcuts = new ArrayList();
	// for (int i = 0; i < pointcuts.length; i++) {sortedSetOfPointcuts.add(pointcuts[i]);}
	// Collections.sort(sortedSetOfPointcuts);
	//        	
	// int i =0;
	// for (Iterator iter = sortedSetOfPointcuts.iterator(); iter.hasNext();) {
	// ResolvedMember member = (ResolvedMember) iter.next();
	// result.append("p"+i+") "+member.toDebugString()+"\n");
	// i++;
	// }
	// }
	//    	
	// Collection declares = getDeclares();
	// if (declares.size()>0) {
	// result.append("The declares: "+declares.size()+"\n");
	//    		
	// // // Sort the damn things
	// // List sortedSetOfPointcuts = new ArrayList();
	// // for (int i = 0; i < pointcuts.length; i++) {sortedSetOfPointcuts.add(pointcuts[i]);}
	// // Collections.sort(sortedSetOfPointcuts);
	//        	
	// int i=0;
	// for (Iterator iter = declares.iterator(); iter.hasNext();) {
	// Declare dec = (Declare) iter.next();
	// result.append("d"+i+") "+dec.toString()+"\n");
	// i++;
	// }
	// }
	//    	
	// TypeVariable[] tv = getTypeVariables();
	// if (tv!=null && tv.length>0) {
	// result.append("The type variables: "+tv.length+"\n");
	// for (int i = 0; i < tv.length; i++) {
	// result.append("tv"+i+") "+tv[i]+"\n");
	// }
	// }
	//    	
	// Collection tmungers = getTypeMungers();
	// if (tmungers.size()>0) {
	// List sorted = new ArrayList();
	// sorted.addAll(tmungers);
	// Collections.sort(sorted,new Comparator() {
	// public int compare(Object arg0, Object arg1) {
	// return arg0.toString().compareTo(arg1.toString());
	// }
	// });
	// result.append("The type mungers: "+tmungers.size()+"\n");
	// int i=0;
	// for (Iterator iter = sorted.iterator(); iter.hasNext();) {
	// ConcreteTypeMunger mun = (ConcreteTypeMunger) iter.next();
	// result.append("tm"+i+") "+mun.toString()+"\n");
	// i++;
	// }
	// }
	//
	// result.append("doesNotExposeShadowMungers?"+doesNotExposeShadowMungers()+"\n");
	//    	
	// Collection pas = getPrivilegedAccesses();
	// if (pas!=null && pas.size()>0) {
	// // List sorted = new ArrayList();
	// // sorted.addAll(tmungers);
	// // Collections.sort(sorted,new Comparator() {
	// // public int compare(Object arg0, Object arg1) {
	// // return arg0.toString().compareTo(arg1.toString());
	// // }
	// // });
	// result.append("The privileged accesses: "+pas.size()+"\n");
	// int i=0;
	// for (Iterator iter = pas.iterator(); iter.hasNext();) {
	// ResolvedMember mun = (ResolvedMember) iter.next();
	// result.append("tm"+i+") "+mun.toDebugString()+"\n");
	// i++;
	// }
	// }
	//
	// // public Collection getPrivilegedAccesses();
	// // public boolean hasAnnotation(UnresolvedType ofType);
	// result.append("===");
	// return result.toString();
	// }

	/**
	 * Build a member from a string representation: <blockquote>
	 * 
	 * <pre>
	 * static? TypeName TypeName.Id
	 * </pre>
	 * 
	 * </blockquote>
	 */
	public static MemberImpl fieldFromString(String str) {
		str = str.trim();
		final int len = str.length();
		int i = 0;
		int mods = 0;
		if (str.startsWith("static", i)) {
			mods = Modifier.STATIC;
			i += 6;
			while (Character.isWhitespace(str.charAt(i)))
				i++;
		}
		int start = i;
		while (!Character.isWhitespace(str.charAt(i)))
			i++;
		UnresolvedType retTy = UnresolvedType.forName(str.substring(start, i));

		start = i;
		i = str.lastIndexOf('.');
		UnresolvedType declaringTy = UnresolvedType.forName(str.substring(start, i).trim());
		start = ++i;
		String name = str.substring(start, len).trim();
		return new MemberImpl(Member.FIELD, declaringTy, mods, retTy, name, UnresolvedType.NONE);
	}

	/**
	 * Build a member from a string representation: <blockquote>
	 * 
	 * <pre>
	 * (static|interface|private)? TypeName TypeName . Id ( TypeName , ...)
	 * </pre>
	 * 
	 * </blockquote>
	 */

	public static Member methodFromString(String str) {
		str = str.trim();
		// final int len = str.length();
		int i = 0;

		int mods = 0;
		if (str.startsWith("static", i)) {
			mods = Modifier.STATIC;
			i += 6;
		} else if (str.startsWith("interface", i)) {
			mods = Modifier.INTERFACE;
			i += 9;
		} else if (str.startsWith("private", i)) {
			mods = Modifier.PRIVATE;
			i += 7;
		}
		while (Character.isWhitespace(str.charAt(i)))
			i++;

		int start = i;
		while (!Character.isWhitespace(str.charAt(i)))
			i++;
		UnresolvedType returnTy = UnresolvedType.forName(str.substring(start, i));

		start = i;
		i = str.indexOf('(', i);
		i = str.lastIndexOf('.', i);
		UnresolvedType declaringTy = UnresolvedType.forName(str.substring(start, i).trim());

		start = ++i;
		i = str.indexOf('(', i);
		String name = str.substring(start, i).trim();
		start = ++i;
		i = str.indexOf(')', i);

		String[] paramTypeNames = parseIds(str.substring(start, i).trim());

		return MemberImpl.method(declaringTy, mods, returnTy, name, UnresolvedType.forNames(paramTypeNames));
	}

	public static String[] parseIds(String str) {
		if (str.length() == 0)
			return ZERO_STRINGS;
		List<String> l = new ArrayList<>();
		int start = 0;
		while (true) {
			int i = str.indexOf(',', start);
			if (i == -1) {
				l.add(str.substring(start).trim());
				break;
			}
			l.add(str.substring(start, i).trim());
			start = i + 1;
		}
		return (String[]) l.toArray(new String[0]);
	}

}
