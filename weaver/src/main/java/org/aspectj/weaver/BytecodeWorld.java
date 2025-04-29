/*******************************************************************************
 * Copyright (c) 2025 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *******************************************************************************/
package org.aspectj.weaver;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aspectj.asm.AsmManager;
import org.aspectj.asm.IRelationship;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.bridge.Message;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.bridge.WeaveMessage;
import org.aspectj.weaver.loadtime.definition.Definition;
import org.aspectj.weaver.loadtime.definition.DocumentParser;
import org.aspectj.weaver.model.AsmRelationshipProvider;
import org.aspectj.weaver.patterns.ParserException;
import org.aspectj.weaver.patterns.PatternParser;
import org.aspectj.weaver.patterns.TypePattern;
import org.aspectj.weaver.tools.Trace;
import org.aspectj.weaver.tools.TraceFactory;

public abstract class BytecodeWorld extends World {

	protected static Trace trace = TraceFactory.getTraceFactory().getTrace(BytecodeWorld.class);

	private boolean isXmlConfiguredWorld = false;
	private WeavingXmlConfig xmlConfiguration;

	protected List<TypeDelegateResolver> typeDelegateResolvers;

	public final IRelationship.Kind determineRelKind(ShadowMunger munger) {
		AdviceKind ak = ((Advice) munger).getKind();
		if (ak.getKey() == AdviceKind.Before.getKey()) {
			return IRelationship.Kind.ADVICE_BEFORE;
		} else if (ak.getKey() == AdviceKind.After.getKey()) {
			return IRelationship.Kind.ADVICE_AFTER;
		} else if (ak.getKey() == AdviceKind.AfterThrowing.getKey()) {
			return IRelationship.Kind.ADVICE_AFTERTHROWING;
		} else if (ak.getKey() == AdviceKind.AfterReturning.getKey()) {
			return IRelationship.Kind.ADVICE_AFTERRETURNING;
		} else if (ak.getKey() == AdviceKind.Around.getKey()) {
			return IRelationship.Kind.ADVICE_AROUND;
		} else if (ak.getKey() == AdviceKind.CflowEntry.getKey() || ak.getKey() == AdviceKind.CflowBelowEntry.getKey()
				|| ak.getKey() == AdviceKind.InterInitializer.getKey() || ak.getKey() == AdviceKind.PerCflowEntry.getKey()
				|| ak.getKey() == AdviceKind.PerCflowBelowEntry.getKey() || ak.getKey() == AdviceKind.PerThisEntry.getKey()
				|| ak.getKey() == AdviceKind.PerTargetEntry.getKey() || ak.getKey() == AdviceKind.Softener.getKey()
				|| ak.getKey() == AdviceKind.PerTypeWithinEntry.getKey()) {
			// System.err.println("Dont want a message about this: "+ak);
			return null;
		}
		throw new RuntimeException("Shadow.determineRelKind: What the hell is it? " + ak);
	}
	
	/*
	 * Ensure we report a nice source location - particular in the case where the source info is missing (binary weave).
	 */
	protected String beautifyLocation(ISourceLocation isl) {
		StringBuilder nice = new StringBuilder();
		if (isl == null || isl.getSourceFile() == null || isl.getSourceFile().getName().contains("no debug info available")) {
			nice.append("no debug info available");
		} else {
			// can't use File.getName() as this fails when a Linux box encounters a path created on Windows and vice-versa
			int takeFrom = isl.getSourceFile().getPath().lastIndexOf('/');
			if (takeFrom == -1) {
				takeFrom = isl.getSourceFile().getPath().lastIndexOf('\\');
			}
			int binary = isl.getSourceFile().getPath().lastIndexOf('!');
			if (binary != -1 && binary < takeFrom) {
				// we have been woven by a binary aspect
				String pathToBinaryLoc = isl.getSourceFile().getPath().substring(0, binary + 1);
				if (pathToBinaryLoc.contains(".jar")) {
					// only want to add the extra info if we're from a jar file
					int lastSlash = pathToBinaryLoc.lastIndexOf('/');
					if (lastSlash == -1) {
						lastSlash = pathToBinaryLoc.lastIndexOf('\\');
					}
					nice.append(pathToBinaryLoc.substring(lastSlash + 1));
				}
			}
			nice.append(isl.getSourceFile().getPath().substring(takeFrom + 1));
			if (isl.getLine() != 0) {
				nice.append(":").append(isl.getLine());
			}
			// if it's a binary file then also want to give the file name
			if (isl.getSourceFileName() != null) {
				nice.append("(from ").append(isl.getSourceFileName()).append(")");
			}
		}
		return nice.toString();
	}

	protected boolean areTheSame(ISourceLocation locA, ISourceLocation locB) {
		if (locA == null) {
			return locB == null;
		}
		if (locB == null) {
			return false;
		}
		if (locA.getLine() != locB.getLine()) {
			return false;
		}
		File fA = locA.getSourceFile();
		File fB = locA.getSourceFile();
		if (fA == null) {
			return fB == null;
		}
		if (fB == null) {
			return false;
		}
		return fA.getName().equals(fB.getName());
	}

	/*
	 * Report a message about the advice weave that has occurred. Some messing about to make it pretty ! This code is just asking
	 * for an NPE to occur ...
	 */
	protected void reportWeavingMessage(ShadowMunger munger, Shadow shadow) {
		Advice advice = (Advice) munger;
		AdviceKind aKind = advice.getKind();
		// Only report on interesting advice kinds ...
		if (aKind == null || advice.getConcreteAspect() == null) {
			// We suspect someone is programmatically driving the weaver
			// (e.g. IdWeaveTestCase in the weaver testcases)
			return;
		}
		if (!(aKind.equals(AdviceKind.Before) || aKind.equals(AdviceKind.After) || aKind.equals(AdviceKind.AfterReturning)
				|| aKind.equals(AdviceKind.AfterThrowing) || aKind.equals(AdviceKind.Around) || aKind.equals(AdviceKind.Softener))) {
			return;
		}

		// synchronized blocks are implemented with multiple monitor_exit instructions in the bytecode
		// (one for normal exit from the method, one for abnormal exit), we only want to tell the user
		// once we have advised the end of the sync block, even though under the covers we will have
		// woven both exit points
		if (shadow.getKind() == Shadow.SynchronizationUnlock) {
			if (advice.lastReportedMonitorExitJoinpointLocation == null) {
				// this is the first time through, let's continue...
				advice.lastReportedMonitorExitJoinpointLocation = shadow.getSourceLocation();
			} else {
				if (areTheSame(shadow.getSourceLocation(), advice.lastReportedMonitorExitJoinpointLocation)) {
					// Don't report it again!
					advice.lastReportedMonitorExitJoinpointLocation = null;
					return;
				}
				// hmmm, this means some kind of nesting is going on, urgh
				advice.lastReportedMonitorExitJoinpointLocation = shadow.getSourceLocation();
			}
		}

		String description = advice.getKind().toString();
		String advisedType = shadow.getEnclosingType().getName();
		String advisingType = advice.getConcreteAspect().getName();
		Message msg = null;
		if (advice.getKind().equals(AdviceKind.Softener)) {
			msg = WeaveMessage.constructWeavingMessage(
				WeaveMessage.WEAVEMESSAGE_SOFTENS,
				new String[] {
					advisedType, beautifyLocation(shadow.getSourceLocation()),
					advisingType, beautifyLocation(munger.getSourceLocation())
				},
				advisedType, advisingType,
				shadow.getSourceLocation(), munger.getSourceLocation()
			);
		}
		else {
			boolean runtimeTest = advice.hasDynamicTests();
			String joinPointDescription = shadow.toString();
			msg = WeaveMessage.constructWeavingMessage(
				WeaveMessage.WEAVEMESSAGE_ADVISES,
				new String[] {
					joinPointDescription,
					advisedType, beautifyLocation(shadow.getSourceLocation()),
					description,
					advisingType, beautifyLocation(munger.getSourceLocation()),
					(runtimeTest ? " [with runtime test]" : "")
				},
				advisedType, advisingType,
				shadow.getSourceLocation(), munger.getSourceLocation()
			);
			// Boolean.toString(runtimeTest)});
		}
		getMessageHandler().handleMessage(msg);
	}

	public AsmManager getModelAsAsmManager() {
		return (AsmManager) getModel(); // For now... always an AsmManager in a bcel environment
	}

	@Override
	public void reportMatch(ShadowMunger munger, Shadow shadow) {
		if (getCrossReferenceHandler() != null) {
			final IRelationship.Kind kind = determineRelKind(munger);
			getCrossReferenceHandler().addCrossReference(
				munger.getSourceLocation(),           // What is being applied?
				shadow.getSourceLocation(),           // Where is it being applied?
				kind == null ? null : kind.getName(), // What kind of advice?
				((Advice) munger).hasDynamicTests()   // Is a runtime test being stuffed in the code?
			);
		}

		if (!getMessageHandler().isIgnoring(IMessage.WEAVEINFO)) {
			reportWeavingMessage(munger, shadow);
		}

		if (getModel() != null) {
			AsmRelationshipProvider.addAdvisedRelationship(getModelAsAsmManager(), shadow, munger);
		}
	}
	

	/**
	 * These are aop.xml files that can be used to alter the aspects that actually apply from those passed in - and also their scope
	 * of application to other files in the system.
	 *
	 * @param xmlFiles list of File objects representing any aop.xml files passed in to configure the build process
	 */
	public void setXmlFiles(List<File> xmlFiles) {
		if (!isXmlConfiguredWorld && !xmlFiles.isEmpty()) {
			raiseError("xml configuration files only supported by the compiler when -xmlConfigured option specified");
			return;
		}
		if (!xmlFiles.isEmpty()) {
			xmlConfiguration = new WeavingXmlConfig(this, WeavingXmlConfig.MODE_COMPILE);
		}
		for (File xmlfile : xmlFiles) {
			try {
				Definition d = DocumentParser.parse(xmlfile.toURI().toURL());
				xmlConfiguration.add(d);
			} catch (MalformedURLException e) {
				raiseError("Unexpected problem processing XML config file '" + xmlfile.getName() + "' :" + e.getMessage());
			} catch (Exception e) {
				raiseError("Unexpected problem processing XML config file '" + xmlfile.getName() + "' :" + e.getMessage());
			}
		}
	}

	void raiseError(String message) {
		getMessageHandler().handleMessage(MessageUtil.error(message));
	}

	/**
	 * Add a scoped aspects where the scoping was defined in an aop.xml file and this world is being used in a LTW configuration
	 */
	public void addScopedAspect(String name, String scope) {
		this.isXmlConfiguredWorld = true;
		if (xmlConfiguration == null) {
			xmlConfiguration = new WeavingXmlConfig(this, WeavingXmlConfig.MODE_LTW);
		}
		xmlConfiguration.addScopedAspect(name, scope);
	}

	public void setXmlConfigured(boolean b) {
		this.isXmlConfiguredWorld = b;
	}

	@Override
	public boolean isXmlConfigured() {
		return isXmlConfiguredWorld && xmlConfiguration != null;
	}

	public WeavingXmlConfig getXmlConfiguration() {
		return xmlConfiguration;
	}

	@Override
	public boolean isAspectIncluded(ResolvedType aspectType) {
		if (!isXmlConfigured()) {
			return true;
		}
		return xmlConfiguration.specifiesInclusionOfAspect(aspectType.getName());
	}

	@Override
	public TypePattern getAspectScope(ResolvedType declaringType) {
		return xmlConfiguration.getScopeFor(declaringType.getName());
	}

	/**
	 * A WeavingXmlConfig is initially a collection of definitions from XML files - once the world is ready and weaving is running
	 * it will initialize and transform those definitions into an optimized set of values (eg. resolve type patterns and string
	 * names to real entities). It can then answer questions quickly: (1) is this aspect included in the weaving? (2) Is there a
	 * scope specified for this aspect and does it include type X?
	 *
	 */
	public static class WeavingXmlConfig {

		final static int MODE_COMPILE = 1;
		final static int MODE_LTW = 2;

		private int mode;

		private boolean initialized = false; // Lazily done
		private List<Definition> definitions = new ArrayList<>();

		private List<String> resolvedIncludedAspects = new ArrayList<>();
		private Map<String, TypePattern> scopes = new HashMap<>();

		// these are not set for LTW mode (exclusion of these fast match patterns is handled before the weaver/world are used)
		private List<String> includedFastMatchPatterns = Collections.emptyList();
		private List<TypePattern> includedPatterns = Collections.emptyList();
		private List<String> excludedFastMatchPatterns = Collections.emptyList();
		private List<TypePattern> excludedPatterns = Collections.emptyList();

		private BytecodeWorld world;

		public WeavingXmlConfig(BytecodeWorld bcelWorld, int mode) {
			this.world = bcelWorld;
			this.mode = mode;
		}

		public void add(Definition d) {
			definitions.add(d);
		}

		public void addScopedAspect(String aspectName, String scope) {
			ensureInitialized();
			resolvedIncludedAspects.add(aspectName);
			try {
				TypePattern scopePattern = new PatternParser(scope).parseTypePattern();
				scopePattern.resolve(world);
				scopes.put(aspectName, scopePattern);
				if (!world.getMessageHandler().isIgnoring(IMessage.INFO)) {
					world.getMessageHandler().handleMessage(
							MessageUtil.info("Aspect '" + aspectName + "' is scoped to apply against types matching pattern '"
									+ scopePattern + "'"));
				}
			} catch (Exception e) {
				world.getMessageHandler().handleMessage(
						MessageUtil.error("Unable to parse scope as type pattern.  Scope was '" + scope + "': " + e.getMessage()));
			}
		}

		public void ensureInitialized() {
			if (!initialized) {
				try {
					resolvedIncludedAspects = new ArrayList<>();
					// Process the definitions into something more optimal
					for (Definition definition : definitions) {
						List<String> aspectNames = definition.getAspectClassNames();
						for (String name : aspectNames) {
							resolvedIncludedAspects.add(name);
							// TODO check for existence?
							// ResolvedType resolvedAspect = resolve(UnresolvedType.forName(name));
							// if (resolvedAspect.isMissing()) {
							// // ERROR
							// } else {
							// resolvedIncludedAspects.add(resolvedAspect);
							// }
							String scope = definition.getScopeForAspect(name);
							if (scope != null) {
								// Resolve the type pattern
								try {
									TypePattern scopePattern = new PatternParser(scope).parseTypePattern();
									scopePattern.resolve(world);
									scopes.put(name, scopePattern);
									if (!world.getMessageHandler().isIgnoring(IMessage.INFO)) {
										world.getMessageHandler().handleMessage(
												MessageUtil.info("Aspect '" + name
														+ "' is scoped to apply against types matching pattern '"
														+ scopePattern.toString() + "'"));
									}
								} catch (Exception e) {
									// TODO definitions should remember which file they came from, for inclusion in this message
									world.getMessageHandler().handleMessage(
											MessageUtil.error("Unable to parse scope as type pattern.  Scope was '" + scope + "': "
													+ e.getMessage()));
								}
							}
						}
						try {
							List<String> includePatterns = definition.getIncludePatterns();
							if (includePatterns.size() > 0) {
								includedPatterns = new ArrayList<>();
								includedFastMatchPatterns = new ArrayList<>();
							}
							for (String includePattern : includePatterns) {
								if (includePattern.endsWith("..*")) {
									// from 'blah.blah.blah..*' leave the 'blah.blah.blah.'
									includedFastMatchPatterns.add(includePattern.substring(0, includePattern.length() - 2));
								} else {
									TypePattern includedPattern = new PatternParser(includePattern).parseTypePattern();
									includedPatterns.add(includedPattern);
								}
							}
							List<String> excludePatterns = definition.getExcludePatterns();
							if (excludePatterns.size() > 0) {
								excludedPatterns = new ArrayList<>();
								excludedFastMatchPatterns = new ArrayList<>();
							}
							for (String excludePattern : excludePatterns) {
								if (excludePattern.endsWith("..*")) {
									// from 'blah.blah.blah..*' leave the 'blah.blah.blah.'
									excludedFastMatchPatterns.add(excludePattern.substring(0, excludePattern.length() - 2));
								} else {
									TypePattern excludedPattern = new PatternParser(excludePattern).parseTypePattern();
									excludedPatterns.add(excludedPattern);
								}
							}
						} catch (ParserException pe) {
							// TODO definitions should remember which file they came from, for inclusion in this message
							world.getMessageHandler().handleMessage(
									MessageUtil.error("Unable to parse type pattern: " + pe.getMessage()));

						}
					}
				} finally {
					initialized = true;
				}
			}
		}

		public boolean specifiesInclusionOfAspect(String name) {
			ensureInitialized();
			return resolvedIncludedAspects.contains(name);
		}

		public TypePattern getScopeFor(String name) {
			return scopes.get(name);
		}

		/**
		 * Checks if a given type is to be excluded from weaving.
		 * <p>
		 * For LTW, the development guide (<i>docs/devguide/ltw.adoc</i>) says:
		 * <p>
		 * <i>"The set of types to be woven are those types matched by at least one weaver {@code include} element and not
		 * matched by any weaver {@code exclude} element. If there are no weaver include statements, then all non-excluded
		 * types are included."</i>
		 * <p>
		 * In CTW mode, we cannot quite follow the same rules for exclusion as used for LTW: If the weaver is seeing it
		 * during this kind of build, the type is implicitly included. So all we should check for is exclusion.
		 *
		 * @param type resolved type to be checked
		 *
		 * @return Always false in LTW mode. In CTW mode true for excluded types, false otherwise.
		 */
		public boolean excludesType(ResolvedType type) {
			if (mode == MODE_LTW) {
				return false;
			}
			String typename = type.getName();
			boolean excluded = false;
			for (String excludedPattern : excludedFastMatchPatterns) {
				if (typename.startsWith(excludedPattern)) {
					excluded = true;
					break;
				}
			}
			if (!excluded) {
				for (TypePattern excludedPattern : excludedPatterns) {
					if (excludedPattern.matchesStatically(type)) {
						excluded = true;
						break;
					}
				}
			}
			return excluded;
		}

	}


	@Override
	public void reportCheckerMatch(Checker checker, Shadow shadow) {
		IMessage iMessage = new Message(checker.getMessage(shadow), shadow.toString(), checker.isError() ? IMessage.ERROR
				: IMessage.WARNING, shadow.getSourceLocation(), null, new ISourceLocation[] { checker.getSourceLocation() }, true,
				0, -1, -1);

		getMessageHandler().handleMessage(iMessage);

		if (getCrossReferenceHandler() != null) {
			getCrossReferenceHandler()
					.addCrossReference(
							checker.getSourceLocation(),
							shadow.getSourceLocation(),
							(checker.isError() ? IRelationship.Kind.DECLARE_ERROR.getName() : IRelationship.Kind.DECLARE_WARNING
									.getName()), false);

		}

		if (getModel() != null) {
			AsmRelationshipProvider.addDeclareErrorOrWarningRelationship(getModelAsAsmManager(), shadow, checker);
		}

	}

	protected abstract Clazz lookupJavaClass(ClassPathManager classPath, String name);

	public void addTypeDelegateResolver(TypeDelegateResolver typeDelegateResolver) {
		if (typeDelegateResolvers == null) {
			typeDelegateResolvers = new ArrayList<>();
		}
		typeDelegateResolvers.add(typeDelegateResolver);
	}
	
	public abstract void tidyUp();
	public abstract void demote(ResolvedType type);

	// TODO what on earth does this method do, what is 'source'
	protected abstract AbstractReferenceTypeDelegate addSourceObjectType(Clazz clazz, boolean b);

	public abstract ReferenceTypeDelegate getReferenceTypeDelegateIfBytecodey(ResolvedType concreteAspect);
}
