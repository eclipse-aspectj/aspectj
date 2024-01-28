/*******************************************************************************
 * Copyright (c) 2005, 2017 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *******************************************************************************/
package org.aspectj.weaver.loadtime;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.ProtectionDomain;
import java.util.*;
import java.util.Map.Entry;

import org.aspectj.bridge.AbortException;
import org.aspectj.bridge.Constants;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.util.LangUtil;
import org.aspectj.weaver.IUnwovenClassFile;
import org.aspectj.weaver.Lint;
import org.aspectj.weaver.Lint.Kind;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.World;
import org.aspectj.weaver.bcel.BcelWeakClassLoaderReference;
import org.aspectj.weaver.bcel.BcelWeaver;
import org.aspectj.weaver.bcel.BcelWorld;
import org.aspectj.weaver.bcel.Utility;
import org.aspectj.weaver.loadtime.definition.Definition;
import org.aspectj.weaver.loadtime.definition.DocumentParser;
import org.aspectj.weaver.ltw.LTWWorld;
import org.aspectj.weaver.patterns.PatternParser;
import org.aspectj.weaver.patterns.TypePattern;
import org.aspectj.weaver.tools.GeneratedClassHandler;
import org.aspectj.weaver.tools.Trace;
import org.aspectj.weaver.tools.TraceFactory;
import org.aspectj.weaver.tools.WeavingAdaptor;
import org.aspectj.weaver.tools.cache.WeavedClassCache;

import org.objectweb.asm.*;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;
import sun.misc.Unsafe;

/**
 * @author Alexandre Vasseur
 * @author Andy Clement
 * @author Abraham Nevado
 * @author David Knibb
 * @author John Kew
 */
public class ClassLoaderWeavingAdaptor extends WeavingAdaptor {

	private final static String AOP_XML = Constants.AOP_USER_XML + ";" + Constants.AOP_AJC_XML + ";" + Constants.AOP_OSGI_XML;

	private boolean initialized;

	private List<TypePattern> dumpTypePattern = new ArrayList<>();
	private boolean dumpBefore = false;
	private boolean dumpDirPerClassloader = false;

	private boolean hasExcludes = false;
	private List<TypePattern> excludeTypePattern = new ArrayList<>(); // anything
	private List<String> excludeStartsWith = new ArrayList<>(); // com.foo..*
	private List<String> excludeStarDotDotStar = new ArrayList<>(); // *..*CGLIB*
	private List<String> excludeExactName = new ArrayList<>(); // com.foo.Bar
	private List<String> excludeEndsWith = new ArrayList<>(); // com.foo.Bar
	private List<String[]> excludeSpecial = new ArrayList<>();

	private boolean hasIncludes = false;
	private List<TypePattern> includeTypePattern = new ArrayList<>();
	private List<String> includeStartsWith = new ArrayList<>();
	private List<String> includeExactName = new ArrayList<>();
	private boolean includeStar = false;

	private List<TypePattern> aspectExcludeTypePattern = new ArrayList<>();
	private List<String> aspectExcludeStartsWith = new ArrayList<>();
	private List<TypePattern> aspectIncludeTypePattern = new ArrayList<>();
	private List<String> aspectIncludeStartsWith = new ArrayList<>();

	private StringBuffer namespace;
	private IWeavingContext weavingContext;

	private List<ConcreteAspectCodeGen> concreteAspects = new ArrayList<>();

	private static Trace trace = TraceFactory.getTraceFactory().getTrace(ClassLoaderWeavingAdaptor.class);

	public ClassLoaderWeavingAdaptor() {
		super();
		if (trace.isTraceEnabled()) {
			trace.enter("<init>", this);
		}
		if (trace.isTraceEnabled()) {
			trace.exit("<init>");
		}
	}

	/**
	 * We don't need a reference to the class loader and using it during construction can cause problems with recursion. It also
	 * makes sense to supply the weaving context during initialization to.
	 *
	 * @deprecated
	 */
	@Deprecated
	public ClassLoaderWeavingAdaptor(final ClassLoader deprecatedLoader, final IWeavingContext deprecatedContext) {
		super();
		if (trace.isTraceEnabled()) {
			trace.enter("<init>", this, new Object[] { deprecatedLoader, deprecatedContext });
		}
		if (trace.isTraceEnabled()) {
			trace.exit("<init>");
		}
	}

	class SimpleGeneratedClassHandler implements GeneratedClassHandler {
		private BcelWeakClassLoaderReference loaderRef;

		SimpleGeneratedClassHandler(ClassLoader loader) {
			loaderRef = new BcelWeakClassLoaderReference(loader);
		}

		/**
		 * Callback when we need to define a Closure in the JVM
		 *
		 */
		@Override
		public void acceptClass (String name, byte[] originalBytes, byte[] wovenBytes) {
			try {
				if (shouldDump(name.replace('/', '.'), false)) {
					dump(name, wovenBytes, false);
				}
			} catch (Throwable throwable) {
				throwable.printStackTrace();
			}
			if (activeProtectionDomain != null) {
				defineClass(loaderRef.getClassLoader(), name, wovenBytes, activeProtectionDomain);
			} else {
				defineClass(loaderRef.getClassLoader(), name, wovenBytes); // could be done lazily using the hook
			}
		}
	}

	public void initialize(final ClassLoader classLoader, IWeavingContext context) {
		if (initialized) {
			return;
		}

		boolean success = true;

		this.weavingContext = context;
		if (weavingContext == null) {
			weavingContext = new DefaultWeavingContext(classLoader);
		}

		createMessageHandler();

		this.generatedClassHandler = new SimpleGeneratedClassHandler(classLoader);

		List<Definition> definitions = weavingContext.getDefinitions(classLoader, this);
		if (definitions.isEmpty()) {
			disable(); // TODO maw Needed to ensure messages are flushed
			if (trace.isTraceEnabled()) {
				trace.exit("initialize", definitions);
			}
			return;
		}

		// TODO when the world works in terms of the context, we can remove the loader
		bcelWorld = new LTWWorld(classLoader, weavingContext, getMessageHandler(), null);

		weaver = new BcelWeaver(bcelWorld);

		// register the definitions
		success = registerDefinitions(weaver, classLoader, definitions);
		if (success) {

			// after adding aspects
			weaver.prepareForWeave();

			enable(); // TODO maw Needed to ensure messages are flushed
			success = weaveAndDefineConceteAspects();
		}

		if (success) {
			enable();
		} else {
			disable();
			bcelWorld = null;
			weaver = null;
		}
		if (WeavedClassCache.isEnabled()) {
			initializeCache(classLoader, getAspectClassNames(definitions), generatedClassHandler, getMessageHandler());
		}

		initialized = true;
		if (trace.isTraceEnabled()) {
			trace.exit("initialize", isEnabled());
		}
	}

	/**
	 * Get the list of all aspects from the defintion list
	 * @param definitions
	 * @return
	 */
	List<String> getAspectClassNames(List<Definition> definitions) {
		List<String> aspects = new LinkedList<>();
		for (Definition def : definitions) {
			List<String> defAspects = def.getAspectClassNames();
			if (defAspects != null) {
				aspects.addAll(defAspects);
			}
		}
		return aspects;
	}

	/**
	 * Load and cache the aop.xml/properties according to the classloader visibility rules
	 *
	 * @param loader
	 */
	List<Definition> parseDefinitions(final ClassLoader loader) {
		if (trace.isTraceEnabled()) {
			trace.enter("parseDefinitions", this);
		}

		List<Definition> definitions = new ArrayList<>();
		try {
			info("register classloader " + getClassLoaderName(loader));
			// TODO av underoptimized: we will parse each XML once per CL that see it

			// TODO av dev mode needed ? TBD -Daj5.def=...
			if (loader.equals(ClassLoader.getSystemClassLoader())) {
				String file = System.getProperty("aj5.def", null);
				if (file != null) {
					info("using (-Daj5.def) " + file);
					definitions.add(DocumentParser.parse((new File(file)).toURI().toURL()));
				}
			}

			String resourcePath = System.getProperty("org.aspectj.weaver.loadtime.configuration", AOP_XML);
			if (trace.isTraceEnabled()) {
				trace.event("parseDefinitions", this, resourcePath);
			}

			StringTokenizer st = new StringTokenizer(resourcePath, ";");

			while (st.hasMoreTokens()) {
				String nextDefinition = st.nextToken();
				if (nextDefinition.startsWith("file:")) {
					try {
						String fpath = new URL(nextDefinition).getFile();
						File configFile = new File(fpath);
						if (!configFile.exists()) {
							warn("configuration does not exist: " + nextDefinition);
						} else {
							definitions.add(DocumentParser.parse(configFile.toURI().toURL()));
						}
					} catch (MalformedURLException mue) {
						error("malformed definition url: " + nextDefinition);
					}
				} else {
					Enumeration<URL> xmls = weavingContext.getResources(nextDefinition);
					// System.out.println("? registerDefinitions: found-aop.xml=" + xmls.hasMoreElements() + ", loader=" + loader);

					Set<URL> seenBefore = new HashSet<>();
					while (xmls.hasMoreElements()) {
						URL xml = xmls.nextElement();
						if (trace.isTraceEnabled()) {
							trace.event("parseDefinitions", this, xml);
						}
						if (!seenBefore.contains(xml)) {
							info("using configuration " + weavingContext.getFile(xml));
							definitions.add(DocumentParser.parse(xml));
							seenBefore.add(xml);
						} else {
							debug("ignoring duplicate definition: " + xml);
						}
					}
				}
			}
			if (definitions.isEmpty()) {
				info("no configuration found. Disabling weaver for class loader " + getClassLoaderName(loader));
			}
		} catch (Exception e) {
			definitions.clear();
			warn("parse definitions failed", e);
		}

		if (trace.isTraceEnabled()) {
			trace.exit("parseDefinitions", definitions);
		}
		return definitions;
	}

	private boolean registerDefinitions(final BcelWeaver weaver, final ClassLoader loader, List<Definition> definitions) {
		if (trace.isTraceEnabled()) {
			trace.enter("registerDefinitions", this, definitions);
		}
		boolean success = true;

		try {
			registerOptions(weaver, loader, definitions);
			registerAspectExclude(weaver, loader, definitions);
			registerAspectInclude(weaver, loader, definitions);
			success = registerAspects(weaver, loader, definitions);
			registerIncludeExclude(weaver, loader, definitions);
			registerDump(weaver, loader, definitions);
		} catch (Exception ex) {
			trace.error("register definition failed", ex);
			success = false;
			warn("register definition failed", (ex instanceof AbortException) ? null : ex);
		}

		if (trace.isTraceEnabled()) {
			trace.exit("registerDefinitions", success);
		}
		return success;
	}

	private String getClassLoaderName(ClassLoader loader) {
		return weavingContext.getClassLoaderName();
	}

	/**
	 * Configure the weaver according to the option directives TODO av - don't know if it is that good to reuse, since we only allow
	 * a small subset of options in LTW
	 *
	 * @param weaver
	 * @param loader
	 * @param definitions
	 */
	private void registerOptions(final BcelWeaver weaver, final ClassLoader loader, final List<Definition> definitions) {
		StringBuilder allOptions = new StringBuilder();
		for (Definition definition : definitions) {
			allOptions.append(definition.getWeaverOptions()).append(' ');
		}

		Options.WeaverOption weaverOption = Options.parse(allOptions.toString(), loader, getMessageHandler());

		// configure the weaver and world
		// AV - code duplicates AspectJBuilder.initWorldAndWeaver()
		World world = weaver.getWorld();
		setMessageHandler(weaverOption.messageHandler);
		world.setXlazyTjp(weaverOption.lazyTjp);
		world.setXHasMemberSupportEnabled(weaverOption.hasMember);
		world.setTiming(weaverOption.timers, true);
		world.setOptionalJoinpoints(weaverOption.optionalJoinpoints);
		world.setPinpointMode(weaverOption.pinpoint);
		weaver.setReweavableMode(weaverOption.notReWeavable);
		if (weaverOption.loadersToSkip != null && weaverOption.loadersToSkip.length() > 0) {
			Aj.loadersToSkip = LangUtil.anySplit(weaverOption.loadersToSkip, ",");
		}
		if (Aj.loadersToSkip != null) {
			MessageUtil.info(world.getMessageHandler(),"no longer creating weavers for these classloaders: "+Aj.loadersToSkip);
		}
		world.performExtraConfiguration(weaverOption.xSet);
		world.setXnoInline(weaverOption.noInline);
		// AMC - autodetect as per line below, needed for AtAjLTWTests.testLTWUnweavable
		world.setBehaveInJava5Way(true);
		world.setAddSerialVerUID(weaverOption.addSerialVersionUID);

		/* First load defaults */
		bcelWorld.getLint().loadDefaultProperties();

		/* Second overlay LTW defaults */
		bcelWorld.getLint().adviceDidNotMatch.setKind(null);

		/* Third load user file using -Xlintfile so that -Xlint wins */
		if (weaverOption.lintFile != null) {
			InputStream resource = null;
			try {
				resource = loader.getResourceAsStream(weaverOption.lintFile);
				Exception failure = null;
				if (resource != null) {
					try {
						Properties properties = new Properties();
						properties.load(resource);
						world.getLint().setFromProperties(properties);
					} catch (IOException e) {
						failure = e;
					}
				}
				if (failure != null || resource == null) {
					warn("Cannot access resource for -Xlintfile:" + weaverOption.lintFile, failure);
					// world.getMessageHandler().handleMessage(new Message(
					// "Cannot access resource for -Xlintfile:"+weaverOption.lintFile,
					// IMessage.WARNING,
					// failure,
					// null));
				}
			} finally {
				try {
					resource.close();
				} catch (Throwable t) {
				}
			}
		}

		/* Fourth override with -Xlint */
		if (weaverOption.lint != null) {
			if (weaverOption.lint.equals("default")) {// FIXME should be AjBuildConfig.AJLINT_DEFAULT but yetanother deps..
				bcelWorld.getLint().loadDefaultProperties();
			} else {
				bcelWorld.getLint().setAll(weaverOption.lint);
				if (weaverOption.lint.equals("ignore")) {
					bcelWorld.setAllLintIgnored();
				}
			}
		}
		// TODO proceedOnError option
	}

	private void registerAspectExclude(final BcelWeaver weaver, final ClassLoader loader, final List<Definition> definitions) {
		String fastMatchInfo = null;
		for (Definition definition : definitions) {
			for (String exclude : definition.getAspectExcludePatterns()) {
				TypePattern excludePattern = new PatternParser(exclude).parseTypePattern();
				aspectExcludeTypePattern.add(excludePattern);
				fastMatchInfo = looksLikeStartsWith(exclude);
				if (fastMatchInfo != null) {
					aspectExcludeStartsWith.add(fastMatchInfo);
				}
			}
		}
	}

	private void registerAspectInclude(final BcelWeaver weaver, final ClassLoader loader, final List<Definition> definitions) {
		String fastMatchInfo = null;
		for (Definition definition : definitions) {
			for (String include : definition.getAspectIncludePatterns()) {
				TypePattern includePattern = new PatternParser(include).parseTypePattern();
				aspectIncludeTypePattern.add(includePattern);
				fastMatchInfo = looksLikeStartsWith(include);
				if (fastMatchInfo != null) {
					aspectIncludeStartsWith.add(fastMatchInfo);
				}
			}
		}
	}

	protected void lint(String name, String[] infos) {
		Lint lint = bcelWorld.getLint();
		Kind kind = lint.getLintKind(name);
		kind.signal(infos, null, null);
	}

	@Override
	public String getContextId() {
		return weavingContext.getId();
	}

	/**
	 * Register the aspect, following include / exclude rules
	 *
	 * @param weaver
	 * @param loader
	 * @param definitions
	 */
	private boolean registerAspects(final BcelWeaver weaver, final ClassLoader loader, final List<Definition> definitions) {
		if (trace.isTraceEnabled()) {
			trace.enter("registerAspects", this, new Object[] { weaver, loader, definitions });
		}
		boolean success = true;

		// TODO: the exclude aspect allow to exclude aspect defined upper in the CL hierarchy - is it what we want ??
		// if not, review the getResource so that we track which resource is defined by which CL

		// iterate aspectClassNames
		// exclude if in any of the exclude list
		for (Definition definition : definitions) {
			for (String aspectClassName : definition.getAspectClassNames()) {
				if (acceptAspect(aspectClassName)) {
					info("register aspect " + aspectClassName);
					// System.err.println("? ClassLoaderWeavingAdaptor.registerAspects() aspectName=" + aspectClassName +
					// ", loader=" + loader + ", bundle=" + weavingContext.getClassLoaderName());
					String requiredType = definition.getAspectRequires(aspectClassName);
					if (requiredType != null) {
						// This aspect expresses that it requires a type to be around, otherwise it should 'switch off'
						((BcelWorld) weaver.getWorld()).addAspectRequires(aspectClassName, requiredType);
					}
					String definedScope = definition.getScopeForAspect(aspectClassName);
					if (definedScope != null) {
						((BcelWorld) weaver.getWorld()).addScopedAspect(aspectClassName, definedScope);
					}
					// ResolvedType aspect =
					weaver.addLibraryAspect(aspectClassName);

					// generate key for SC
					if (namespace == null) {
						namespace = new StringBuffer(aspectClassName);
					} else {
						namespace = namespace.append(";").append(aspectClassName);
					}

				} else {
					// warn("aspect excluded: " + aspectClassName);
					lint("aspectExcludedByConfiguration", new String[] { aspectClassName, getClassLoaderName(loader) });
				}
			}
		}

		// iterate concreteAspects
		// exclude if in any of the exclude list - note that the user defined name matters for that to happen
		for (Definition definition : definitions) {
			for (Definition.ConcreteAspect concreteAspect : definition.getConcreteAspects()) {
				if (acceptAspect(concreteAspect.name)) {
					info("define aspect " + concreteAspect.name);
					ConcreteAspectCodeGen gen = new ConcreteAspectCodeGen(concreteAspect, weaver.getWorld());
					if (!gen.validate()) {
						error("Concrete-aspect '" + concreteAspect.name + "' could not be registered");
						success = false;
						break;
					}

					((BcelWorld) weaver.getWorld()).addSourceObjectType(Utility.makeJavaClass(concreteAspect.name, gen.getBytes()),
							true);

					concreteAspects.add(gen);

					weaver.addLibraryAspect(concreteAspect.name);

					// generate key for SC
					if (namespace == null) {
						namespace = new StringBuffer(concreteAspect.name);
					} else {
						namespace = namespace.append(";" + concreteAspect.name);
					}
				}
			}
		}

		/* We couldn't register one or more aspects so disable the adaptor */
		if (!success) {
			warn("failure(s) registering aspects. Disabling weaver for class loader " + getClassLoaderName(loader));
		}

		/* We didn't register any aspects so disable the adaptor */
		else if (namespace == null) {
			success = false;
			info("no aspects registered. Disabling weaver for class loader " + getClassLoaderName(loader));
		}

		if (trace.isTraceEnabled()) {
			trace.exit("registerAspects", success);
		}
		return success;
	}

	private boolean weaveAndDefineConceteAspects() {
		if (trace.isTraceEnabled()) {
			trace.enter("weaveAndDefineConceteAspects", this, concreteAspects);
		}
		boolean success = true;

		for (ConcreteAspectCodeGen gen : concreteAspects) {
			String name = gen.getClassName();
			byte[] bytes = gen.getBytes();

			try {
				byte[] newBytes = weaveClass(name, bytes, true);
				this.generatedClassHandler.acceptClass(name, bytes, newBytes);
			} catch (IOException ex) {
				trace.error("weaveAndDefineConceteAspects", ex);
				error("exception weaving aspect '" + name + "'", ex);
			}
		}

		if (trace.isTraceEnabled()) {
			trace.exit("weaveAndDefineConceteAspects", success);
		}
		return success;
	}

	/**
	 * Register the include / exclude filters. We duplicate simple patterns in startWith filters that will allow faster matching
	 * without ResolvedType
	 *
	 * @param weaver
	 * @param loader
	 * @param definitions
	 */
	private void registerIncludeExclude(final BcelWeaver weaver, final ClassLoader loader, final List<Definition> definitions) {
		String fastMatchInfo = null;
		for (Definition definition : definitions) {
			for (String value : definition.getIncludePatterns()) {
				hasIncludes = true;
				String include = value;
				fastMatchInfo = looksLikeStartsWith(include);
				if (fastMatchInfo != null) {
					includeStartsWith.add(fastMatchInfo);
				} else if (include.equals("*")) {
					includeStar = true;
				} else if ((fastMatchInfo = looksLikeExactName(include)) != null) {
					includeExactName.add(fastMatchInfo);
				} else {
					TypePattern includePattern = new PatternParser(include).parseTypePattern();
					includeTypePattern.add(includePattern);
				}
			}
			for (String s : definition.getExcludePatterns()) {
				hasExcludes = true;
				String exclude = s;
				fastMatchInfo = looksLikeStartsWith(exclude);
				if (fastMatchInfo != null) {
					excludeStartsWith.add(fastMatchInfo);
				} else if ((fastMatchInfo = looksLikeStarDotDotStarExclude(exclude)) != null) {
					excludeStarDotDotStar.add(fastMatchInfo);
				} else if ((fastMatchInfo = looksLikeExactName(exclude)) != null) {
					excludeExactName.add(exclude);
				} else if ((fastMatchInfo = looksLikeEndsWith(exclude)) != null) {
					excludeEndsWith.add(fastMatchInfo);
				} else if (exclude
						.equals("org.codehaus.groovy..* && !org.codehaus.groovy.grails.web.servlet.mvc.SimpleGrailsController*")) {
					// TODO need a more sophisticated analysis here, to allow for similar situations
					excludeSpecial.add(new String[]{"org.codehaus.groovy.",
							"org.codehaus.groovy.grails.web.servlet.mvc.SimpleGrailsController"});
					// for the related test:
					// } else if (exclude.equals("testdata..* && !testdata.sub.Oran*")) {
					// excludeSpecial.add(new String[] { "testdata.", "testdata.sub.Oran" });
				} else {
					TypePattern excludePattern = new PatternParser(exclude).parseTypePattern();
					excludeTypePattern.add(excludePattern);
				}
			}
		}
	}

	/**
	 * Checks if the pattern looks like "*..*XXXX*" and if so returns XXXX. This will enable fast name matching of CGLIB exclusion
	 *
	 */
	private String looksLikeStarDotDotStarExclude(String typePattern) {
		if (!typePattern.startsWith("*..*")) {
			return null;
		}
		if (!typePattern.endsWith("*")) {
			return null;
		}
		String subPattern = typePattern.substring(4, typePattern.length() - 1);
		if (hasStarDot(subPattern, 0)) {
			return null;
		}
		return subPattern.replace('$', '.');
	}

	/**
	 * Checks if the pattern looks like "com.foo.Bar" - an exact name
	 */
	private String looksLikeExactName(String typePattern) {
		if (hasSpaceAnnotationPlus(typePattern, 0) || typePattern.contains("*")) {
			return null;
		}
		return typePattern.replace('$', '.');
	}

	/**
	 * Checks if the pattern looks like "*Exception"
	 */
	private String looksLikeEndsWith(String typePattern) {
		if (typePattern.charAt(0) != '*') {
			return null;
		}
		if (hasSpaceAnnotationPlus(typePattern, 1) || hasStarDot(typePattern, 1)) {
			return null;
		}
		return typePattern.substring(1).replace('$', '.');
	}

	/**
	 * Determine if something in the string is going to affect our ability to optimize. Checks for: ' ' '@' '+'
	 */
	private boolean hasSpaceAnnotationPlus(String string, int pos) {
		for (int i = pos, max = string.length(); i < max; i++) {
			char ch = string.charAt(i);
			if (ch == ' ' || ch == '@' || ch == '+') {
				return true;
			}
		}
		return false;
	}

	/**
	 * Determine if something in the string is going to affect our ability to optimize. Checks for: '*' '.'
	 */
	private boolean hasStarDot(String string, int pos) {
		for (int i = pos, max = string.length(); i < max; i++) {
			char ch = string.charAt(i);
			if (ch == '*' || ch == '.') {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks if the type pattern looks like "com.foo..*"
	 */
	private String looksLikeStartsWith(String typePattern) {
		if (hasSpaceAnnotationPlus(typePattern, 0) || typePattern.charAt(typePattern.length() - 1) != '*') {
			return null;
		}
		// now must looks like with "charsss..*" or "cha.rss..*" etc
		// note that "*" and "*..*" won't be fast matched
		// and that "charsss.*" will not neither
		int length = typePattern.length();
		if (typePattern.endsWith("..*") && length > 3) {
			if (typePattern.indexOf("..") == length - 3 // no ".." before last sequence
					&& typePattern.indexOf('*') == length - 1) { // no earlier '*'
				return typePattern.substring(0, length - 2).replace('$', '.'); // "charsss." or "char.rss." etc
			}
		}
		return null;
	}

	/**
	 * Register the dump filter
	 *
	 * @param weaver
	 * @param loader
	 * @param definitions
	 */
	private void registerDump(final BcelWeaver weaver, final ClassLoader loader, final List<Definition> definitions) {
		for (Definition definition : definitions) {
			for (String dump : definition.getDumpPatterns()) {
				TypePattern pattern = new PatternParser(dump).parseTypePattern();
				dumpTypePattern.add(pattern);
			}
			if (definition.shouldDumpBefore()) {
				dumpBefore = true;
			}
			if (definition.createDumpDirPerClassloader()) {
				dumpDirPerClassloader = true;
			}
		}
	}

	/**
	 * Determine whether a type should be accepted for weaving, by checking it against any includes/excludes.
	 *
	 * @param className the name of the type to possibly accept
	 * @param bytes the bytecode for the type (in case we need to look inside, eg. annotations)
	 * @return true if it should be accepted for weaving
	 */
	@Override
	protected boolean accept(String className, byte[] bytes) {

		if (!hasExcludes && !hasIncludes) {
			return true;
		}

		// still try to avoid ResolvedType if we have simple patterns
		String fastClassName = className.replace('/', '.');
		for (String excludeStartsWithString : excludeStartsWith) {
			if (fastClassName.startsWith(excludeStartsWithString)) {
				return false;
			}
		}

		// Fast exclusion of patterns like: "*..*CGLIB*"
		if (!excludeStarDotDotStar.isEmpty()) {
			for (String namePiece : excludeStarDotDotStar) {
				int index = fastClassName.lastIndexOf('.');
				if (fastClassName.indexOf(namePiece, index + 1) != -1) {
					return false;
				}
			}
		}
		fastClassName = fastClassName.replace('$', '.');

		if (!excludeEndsWith.isEmpty()) {
			for (String lastPiece : excludeEndsWith) {
				if (fastClassName.endsWith(lastPiece)) {
					return false;
				}
			}
		}

		// Fast exclusion of exact names
		if (!excludeExactName.isEmpty()) {
			for (String name : excludeExactName) {
				if (fastClassName.equals(name)) {
					return false;
				}
			}
		}

		if (!excludeSpecial.isEmpty()) {
			for (String[] entry : excludeSpecial) {
				String excludeThese = entry[0];
				String exceptThese = entry[1];
				if (fastClassName.startsWith(excludeThese) && !fastClassName.startsWith(exceptThese)) {
					return false;
				}
			}
		}

		/*
		 * Bug 120363 If we have an exclude pattern that cannot be matched using "starts with" then we cannot fast accept
		 */
		boolean didSomeIncludeMatching = false;
		if (excludeTypePattern.isEmpty()) {
			if (includeStar) {
				return true;
			}
			if (!includeExactName.isEmpty()) {
				didSomeIncludeMatching = true;
				for (String exactname : includeExactName) {
					if (fastClassName.equals(exactname)) {
						return true;
					}
				}
			}
			boolean fastAccept = false;// defaults to false if no fast include
			for (String s : includeStartsWith) {
				didSomeIncludeMatching = true;
				fastAccept = fastClassName.startsWith(s);
				if (fastAccept) {
					return true;
				}
			}
			// We may have processed all patterns now... check that and return
			if (includeTypePattern.isEmpty()) {
				return !didSomeIncludeMatching;
			}
		}

		boolean accept;
		try {
			ensureDelegateInitialized(className, bytes);

			ResolvedType classInfo = delegateForCurrentClass.getResolvedTypeX();

			// exclude are "AND"ed
			for (TypePattern typePattern : excludeTypePattern) {
				if (typePattern.matchesStatically(classInfo)) {
					// exclude match - skip
					return false;
				}
			}
			// include are "OR"ed
			if (includeStar) {
				return true;
			}
			if (!includeExactName.isEmpty()) {
				didSomeIncludeMatching = true;
				for (String exactname : includeExactName) {
					if (fastClassName.equals(exactname)) {
						return true;
					}
				}
			}
			for (String s : includeStartsWith) {
				didSomeIncludeMatching = true;
				boolean fastaccept = fastClassName.startsWith(s);
				if (fastaccept) {
					return true;
				}
			}
			accept = !didSomeIncludeMatching; // only true if no includes at all
			for (TypePattern typePattern : includeTypePattern) {
				accept = typePattern.matchesStatically(classInfo);
				if (accept) {
					break;
				}
				// goes on if this include did not match ("OR"ed)
			}
		} finally {
			this.bcelWorld.demote();
		}
		return accept;
	}

	// FIXME we don't use include/exclude of others aop.xml
	// this can be nice but very dangerous as well to change that
	private boolean acceptAspect(String aspectClassName) {
		// avoid ResolvedType if not needed
		if (aspectExcludeTypePattern.isEmpty() && aspectIncludeTypePattern.isEmpty()) {
			return true;
		}

		// still try to avoid ResolvedType if we have simple patterns
		// EXCLUDE: if one match then reject
		String fastClassName = aspectClassName.replace('/', '.').replace('.', '$');
		for (String value : aspectExcludeStartsWith) {
			if (fastClassName.startsWith(value)) {
				return false;
			}
		}
		// INCLUDE: if one match then accept
		for (String s : aspectIncludeStartsWith) {
			if (fastClassName.startsWith(s)) {
				return true;
			}
		}

		// needs further analysis
		ResolvedType classInfo = weaver.getWorld().resolve(UnresolvedType.forName(aspectClassName), true);
		// exclude are "AND"ed
		for (TypePattern typePattern: aspectExcludeTypePattern) {
			if (typePattern.matchesStatically(classInfo)) {
				// exclude match - skip
				return false;
			}
		}
		// include are "OR"ed
		boolean accept = true;// defaults to true if no include
		for (TypePattern typePattern: aspectIncludeTypePattern) {
			accept = typePattern.matchesStatically(classInfo);
			if (accept) {
				break;
			}
			// goes on if this include did not match ("OR"ed)
		}
		return accept;
	}

	@Override
	protected boolean shouldDump(String className, boolean before) {
		// Don't dump before weaving unless asked to
		if (before && !dumpBefore) {
			return false;
		}

		// avoid ResolvedType if not needed
		if (dumpTypePattern.isEmpty()) {
			return false;
		}

		// TODO AV - optimize for className.startWith only
		ResolvedType classInfo = weaver.getWorld().resolve(UnresolvedType.forName(className), true);
		// dump
		for (TypePattern typePattern : dumpTypePattern) {
			if (typePattern.matchesStatically(classInfo)) {
				// dump match
				return true;
			}
		}
		return false;
	}

	@Override
	protected String getDumpDir() {
		if (dumpDirPerClassloader) {
			StringBuilder dir = new StringBuilder();
			dir.append("_ajdump").append(File.separator).append(weavingContext.getId());
			return dir.toString();
		} else {
			return super.getDumpDir();
		}
	}

	/*
	 * shared classes methods
	 */

	/**
	 * @return Returns the key.
	 */
	public String getNamespace() {
		// System.out.println("ClassLoaderWeavingAdaptor.getNamespace() classloader=" + weavingContext.getClassLoaderName() +
		// ", namespace=" + namespace);
		if (namespace == null) {
			return "";
		} else {
			return new String(namespace);
		}
	}

	/**
	 * Check to see if any classes are stored in the generated classes cache. Then flush the cache if it is not empty
	 *
	 * @param className TODO
	 * @return true if a class has been generated and is stored in the cache
	 */
	public boolean generatedClassesExistFor(String className) {
		// System.err.println("? ClassLoaderWeavingAdaptor.generatedClassesExist() classname=" + className + ", size=" +
		// generatedClasses);
		if (className == null) {
			return !generatedClasses.isEmpty();
		} else {
			return generatedClasses.containsKey(className);
		}
	}

	/**
	 * Flush the generated classes cache
	 */
	public void flushGeneratedClasses() {
		// System.err.println("? ClassLoaderWeavingAdaptor.flushGeneratedClasses() generatedClasses=" + generatedClasses);
		generatedClasses = new HashMap<>();
	}

	/**
	 * Remove generated classes based on the supplied className. This will
	 * remove any entries related to this name - so the class itself plus
	 * and inner classes.
	 * @param className a slashed classname (e.g. com/foo/Bar)
	 */
	public void flushGeneratedClassesFor(String className) {
		try {
			String dottedClassName = className.replace('/', '.');
			String dottedClassNameDollar = dottedClassName+"$"; // to pickup inner classes
			Iterator<Map.Entry<String, IUnwovenClassFile>> iter = generatedClasses.entrySet().iterator();
			while (iter.hasNext()) {
				Entry<String, IUnwovenClassFile> next = iter.next();
				String existingGeneratedName = next.getKey();
				if (existingGeneratedName.equals(dottedClassName) ||
						existingGeneratedName.startsWith(dottedClassNameDollar)) {
					iter.remove();
				}
			}
		} catch (Throwable t) {
			new RuntimeException("Unexpected problem tidying up generated classes for "+className,t).printStackTrace();
		}
	}

	private static final Object lock = new Object();

	/**
	 * Instance of either {@link sun.misc.Unsafe} or {@link jdk.internal.misc.Unsafe}. Luckily, both types have
	 * {@code defineClass} methods with identical signatures. I.e., method handle {@link #defineClassMethodHandle} can be
	 * invoked with the same set of parameters for both types.
	 */
	private static Object unsafeInstance = null;

	/**
	 * Method handle for defining new classes in arbitrary class loaders. For invocation, use in connection with
	 * {@link #unsafeInstance}.
	 */
	private static MethodHandle defineClassMethodHandle;

	static {
		try {
			createDefineClassMethodHandle();
		}
		catch (Exception initializationError) {
			new RuntimeException(
				"The aspect weaver cannot determine any valid method to define auxiliary classes in arbitrary class loaders. " +
					"Aspect weaving will *not* work, and you will see subsequent errors. Please search for corresponding " +
					"issues at https://github.com/eclipse-aspectj/aspectj/issues. If there are none, please create a new one.",
				initializationError
			).printStackTrace();
		}
	}

	/**
	 * Scaffolding for defining classes in arbitrary class loaders
	 * <p>
	 * Inspired by and shamelessly adapted from <a href="https://bit.ly/3w10oH5">Byte Buddy's {@code ClassInjector}</a>.
	 * Special thanks to Byte Buddy (BB) author Rafael Winterhalter, who briefly mentioned this approach in a
	 * <a href="https://bit.ly/3SjFOZY">GitHub comment</a> related to JDK issue
	 * <a href="https://bugs.openjdk.org/browse/JDK-8200559">JDK-8200559</a>.
	 * <p>
	 * <b>Background:</b> Instead of BB, we use ASM and reflection as follows:
	 * <ul>
	 *   <li>
	 *     Create a mirror class for {@link AccessibleObject} with a different package name in a separate, throw-away
	 *     class loader.
	 *   </li>
	 *   <li>
	 *     Use the mirror class to calculate the {@link Unsafe#objectFieldOffset(Field)} for boolean field
	 *     {@link AccessibleObject#override}, which is expected to be identical to the offset of the same field in the
	 *     original class.
	 *   </li>
	 *   <li>
	 *     After we have the offset, we can use it to override the field value in the original class, deactivating access
	 *     checks for {@link jdk.internal.misc.Unsafe#defineClass(String, byte[], int, int, ClassLoader, ProtectionDomain)},
	 *     the method we need to execute using a method handle.
	 *   </li>
	 * </ul>
	 * All these serve the sole purpose enable LTW without {@code --add-opens java.base/java.lang=ALL-UNNAMED} on the
	 * JVM command line on JDK 16+, which was necessary for AspectJ 1.9.7 (Java 16) to 1.9.21 (Java 21).
	 *
	 * @throws Exception if anything goes wrong, trying to determine a usable {@code defineClass} method handle from any
	 * of the inspected classes
	 */
	private static synchronized void createDefineClassMethodHandle() throws Exception {
		Unsafe publicUnsafeInstance = null;
		try {
			Field publicUnsafeField = Unsafe.class.getDeclaredField("theUnsafe");
			publicUnsafeField.setAccessible(true);
			publicUnsafeInstance = (Unsafe) publicUnsafeField.get(null);
			synchronized (lock) {
				unsafeInstance = publicUnsafeInstance;
				defineClassMethodHandle =  createMethodHandle(
					"sun.misc.Unsafe", "defineClass",
					String.class, byte[].class, int.class, int.class, ClassLoader.class, ProtectionDomain.class
				);
			}
		}
		catch (Exception publicUnsafeException) {
			if (publicUnsafeInstance == null)
				throw publicUnsafeException;
			long overrideOffset = getAccessibleObjectOverrideOffset(publicUnsafeInstance);
			Class<?> internalUnsafeType = Class.forName("jdk.internal.misc.Unsafe");
			Field internalUnsafeField = internalUnsafeType.getDeclaredField("theUnsafe");
			publicUnsafeInstance.putBoolean(internalUnsafeField, overrideOffset, true);
			Method internalUnsafeDefineClassMethod = internalUnsafeType.getMethod(
				"defineClass",
				String.class, byte[].class, int.class, int.class, ClassLoader.class, ProtectionDomain.class
			);
			publicUnsafeInstance.putBoolean(internalUnsafeDefineClassMethod, overrideOffset, true);
			synchronized (lock) {
				unsafeInstance = internalUnsafeField.get(null);
				defineClassMethodHandle = createMethodHandle(internalUnsafeDefineClassMethod);
			}
		}
	}

	private static long getAccessibleObjectOverrideOffset(Unsafe unsafe)
		throws IOException, ClassNotFoundException, NoSuchFieldException
	{
		Objects.requireNonNull(unsafe);
		Field overrideField;
		try {
			overrideField = AccessibleObject.class.getDeclaredField("override");
		}
		catch (NoSuchFieldException ignored) {
			// On JDK 12+, field AccessibleObject.override is protected from reflection. The work-around is to create a
			// mirror class with the same field layout by transforming the original class, so we can calculate the field
			// offset of 'override' and set a value in the original class using the now known offset.
			Class<?> mirrorClass = getMirrorClass(
				"java.lang.reflect.AccessibleObject", "org.aspectj.mirror.AccessibleObject", true
			);
			overrideField = mirrorClass.getDeclaredField("override");
		}
		return unsafe.objectFieldOffset(overrideField);
	}

	public static MethodHandle createMethodHandle(String className, String methodName, Class<?>... argumentTypes)
		throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException
	{
		Class<?> clazz = Class.forName(className);
		Method method = clazz.getDeclaredMethod(methodName, argumentTypes);
		return createMethodHandle(method, false);
	}

	public static MethodHandle createMethodHandle(Method method) throws IllegalAccessException {
		return createMethodHandle(method, false);
	}

	public static MethodHandle createMethodHandle(Method method, boolean setAccessible)
		throws IllegalAccessException
	{
		// Use Method::setAccessible to access private methods. Caveat: This does not work for classes in packages not
		// exported to the calling module (for LTW usually the unnamed module).
		if (setAccessible)
			method.setAccessible(true);
		return MethodHandles.lookup().unreflect(method);
	}

	@SuppressWarnings("SameParameterValue")
	private static Class<?> getMirrorClass(String originalClass, String mirrorClass, boolean removeMethods)
		throws IOException, ClassNotFoundException
	{
		Objects.requireNonNull(originalClass);
		Objects.requireNonNull(mirrorClass);
		if (mirrorClass.equals(originalClass))
			throw new IllegalArgumentException("Mirror class name must be different from original " + originalClass);
		byte[] mirrorClassBytes = getMirrorClassBytes(originalClass, mirrorClass, removeMethods);
		ClassLoader mirrorClassLoader = new SingleClassLoader(mirrorClass, mirrorClassBytes);
		return mirrorClassLoader.loadClass(mirrorClass);
	}

	private static byte[] getMirrorClassBytes(String originalClass, String mirrorClass, boolean removeMethods)
		throws IOException, ClassNotFoundException
	{
		Class<?> aClass = Class.forName(originalClass);
		try (InputStream input = aClass.getResourceAsStream(aClass.getSimpleName() + ".class")) {
			Objects.requireNonNull(input);
			ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
			ClassRemapper classRemapper = new ClassRemapper(classWriter, new ClassNameRemapper(originalClass, mirrorClass));
			ClassVisitor classVisitor = removeMethods ? new MethodAndConstructorRemover(classRemapper) : classRemapper;
			new ClassReader(input).accept(classVisitor, 0);
			return classWriter.toByteArray();
		}
	}

	private static class ClassNameRemapper extends Remapper {
		private final String originalClass;
		private final String mirrorClass;

		public ClassNameRemapper(String originalClass, String mirrorClass) {
			this.originalClass = originalClass.replace('.', '/');
			this.mirrorClass = mirrorClass.replace('.', '/');
		}

		@Override
		public String map(String internalName) {
			return internalName.equals(originalClass) ? mirrorClass : internalName;
		}
	}

	/**
	 * ASM class visitor removing all methods and constructors from the given class, leaving only the original fields
	 */
	private static class MethodAndConstructorRemover extends ClassVisitor {
		public MethodAndConstructorRemover(ClassRemapper classRemapper) {
			super(Opcodes.ASM9, classRemapper);
		}

		@Override
		public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
			// Do not visit any methods or constructors, effectively removing them
			return null;
		}
	}

	/**
	 * Throw-away child classloader with the sole purpose to define a single {@link Class} from the given bytecode
	 */
	private static class SingleClassLoader extends ClassLoader {
		private final String mirrorClass;
		private final byte[] mirrorClassBytes;

		private SingleClassLoader(String mirrorClass, byte[] mirrorClassBytes) {
			super(SingleClassLoader.class.getClassLoader());
			this.mirrorClass = mirrorClass;
			this.mirrorClassBytes = mirrorClassBytes;
		}

		@Override
		public Class<?> loadClass(String name) throws ClassNotFoundException {
			return name.equals(mirrorClass)
				? super.defineClass(null, mirrorClassBytes, 0, mirrorClassBytes.length)
				: super.loadClass(name);
		}
	}

	private void defineClass(ClassLoader loader, String name, byte[] bytes) {
		defineClass(loader, name, bytes, null);
	}

	private void defineClass(ClassLoader loader, String name, byte[] bytes, ProtectionDomain protectionDomain) {
		if (trace.isTraceEnabled())
			trace.enter("defineClass", this, new Object[] { loader, name, bytes });
		debug("generating class '" + name + "'");
		Class<?> definedClass = null;
		try {
			if (defineClassMethodHandle == null)
				throw new RuntimeException("no valid method to define auxiliary classes -> weaver not working");
			definedClass = (Class<?>) defineClassMethodHandle
				.bindTo(unsafeInstance)
				.invokeWithArguments(name, bytes, 0, bytes.length, loader, protectionDomain);
		}
		catch (Throwable t) {
			t.printStackTrace(System.err);
			warn("define generated class failed", t);
		}
		finally {
			if (trace.isTraceEnabled())
				trace.exit("defineClass", definedClass);
		}
	}

}
