/*******************************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Vasseur         initial implementation
 *******************************************************************************/
package org.aspectj.weaver.loadtime.definition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A POJO that contains raw strings from the XML (sort of XMLBean for our simple LTW DTD)
 * 
 * @author Alexandre Vasseur (alex AT gnilux DOT com)
 */
public class Definition {

	private final StringBuffer weaverOptions;
	private final List<String> dumpPatterns;
	private boolean dumpBefore;
	private boolean perClassloaderDumpDir;
	private final List<String> includePatterns;
	private final List<String> excludePatterns;
	private final List<String> aspectClassNames;
	private final List<String> aspectExcludePatterns;
	private final List<String> aspectIncludePatterns;
	private final List<Definition.ConcreteAspect> concreteAspects;

	/**
	 * When aspects are defined, they can specify a scope type pattern and then will only apply to types matching that pattern.
	 */
	private final Map<String, String> scopedAspects;

	/**
	 * Some aspects (from aspect libraries) will describe a type that must be around for them to function properly
	 */
	private final Map<String, String> requiredTypesForAspects;

	public Definition() {
		weaverOptions = new StringBuffer();
		dumpBefore = false;
		perClassloaderDumpDir = false;
		dumpPatterns = new ArrayList<>();
		includePatterns = new ArrayList<>();
		excludePatterns = new ArrayList<>();
		aspectClassNames = new ArrayList<>();
		aspectExcludePatterns = new ArrayList<>();
		aspectIncludePatterns = new ArrayList<>();
		concreteAspects = new ArrayList<>();
		scopedAspects = new HashMap<>();
		requiredTypesForAspects = new HashMap<>();
	}

	public String getWeaverOptions() {
		return weaverOptions.toString();
	}

	public List<String> getDumpPatterns() {
		return dumpPatterns;
	}

	public void setDumpBefore(boolean b) {
		dumpBefore = b;
	}

	public boolean shouldDumpBefore() {
		return dumpBefore;
	}

	public void setCreateDumpDirPerClassloader(boolean b) {
		perClassloaderDumpDir = b;
	}

	public boolean createDumpDirPerClassloader() {
		return perClassloaderDumpDir;
	}

	public List<String> getIncludePatterns() {
		return includePatterns;
	}

	public List<String> getExcludePatterns() {
		return excludePatterns;
	}

	public List<String> getAspectClassNames() {
		return aspectClassNames;
	}

	public List<String> getAspectExcludePatterns() {
		return aspectExcludePatterns;
	}

	public List<String> getAspectIncludePatterns() {
		return aspectIncludePatterns;
	}

	public List<Definition.ConcreteAspect> getConcreteAspects() {
		return concreteAspects;
	}

	public static class ConcreteAspect {
		public final String name;
		public final String extend;
		public final String precedence;
		public final List<Definition.Pointcut> pointcuts;
		public final List<Definition.DeclareAnnotation> declareAnnotations;
		public final List<Definition.PointcutAndAdvice> pointcutsAndAdvice;
		public final String perclause;
		public List<Definition.DeclareErrorOrWarning> deows;

		public ConcreteAspect(String name, String extend) {
			this(name, extend, null, null);
		}

		public ConcreteAspect(String name, String extend, String precedence, String perclause) {
			this.name = name;
			// make sure extend set to null if ""
			if (extend == null || extend.length() == 0) {
				this.extend = null;
				if (precedence == null || precedence.length() == 0) {
					// if (pointcutsAndAdvice.size() == 0) {
					// throw new RuntimeException("Not allowed");
					// }
				}
			} else {
				this.extend = extend;
			}
			this.precedence = precedence;
			this.pointcuts = new ArrayList<>();
			this.declareAnnotations = new ArrayList<>();
			this.pointcutsAndAdvice = new ArrayList<>();
			this.deows = new ArrayList<>();
			this.perclause = perclause;
		}
	}

	public static class Pointcut {
		public final String name;
		public final String expression;

		public Pointcut(String name, String expression) {
			this.name = name;
			this.expression = expression;
		}
	}

	public enum AdviceKind {
		Before, After, AfterReturning, AfterThrowing, Around;
	}
	
	public enum DeclareAnnotationKind {
		Method, Field, Type;
	}
	
	public static class DeclareAnnotation {
		public final DeclareAnnotationKind declareAnnotationKind;
		public final String pattern;
		public final String annotation;
		
		public DeclareAnnotation(DeclareAnnotationKind kind, String pattern, String annotation) {
			this.declareAnnotationKind = kind;
			this.pattern = pattern;
			this.annotation = annotation;
		}
	}

	public static class PointcutAndAdvice {
		public final AdviceKind adviceKind;
		public final String pointcut;
		public final String adviceClass; // com.foo.Bar
		public final String adviceMethod; // foo(java.lang.String,org.aspectj.lang.JoinPoint)

		public PointcutAndAdvice(AdviceKind adviceKind, String pointcut, String adviceClass, String adviceMethod) {
			this.adviceKind = adviceKind;
			this.pointcut = pointcut;
			this.adviceClass = adviceClass;
			this.adviceMethod = adviceMethod;
		}
	}

	public static class DeclareErrorOrWarning {
		public final boolean isError;
		public final String pointcut;
		public final String message;

		public DeclareErrorOrWarning(boolean isError, String pointcut, String message) {
			this.isError = isError;
			this.pointcut = pointcut;
			this.message = message;
		}
	}

	public void appendWeaverOptions(String option) {
		weaverOptions.append(option.trim()).append(' ');
	}

	public void addScopedAspect(String name, String scopePattern) {
		scopedAspects.put(name, scopePattern);
	}

	public String getScopeForAspect(String name) {
		return scopedAspects.get(name);
	}

	public void setAspectRequires(String name, String requiredType) {
		requiredTypesForAspects.put(name, requiredType);
	}

	public String getAspectRequires(String name) {
		return requiredTypesForAspects.get(name);
	}

}
