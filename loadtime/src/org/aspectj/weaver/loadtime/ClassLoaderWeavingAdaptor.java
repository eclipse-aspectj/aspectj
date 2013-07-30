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
 *   David Knibb		       weaving context enhancments
 *   John Kew (vmware)         caching hook
 *******************************************************************************/
package org.aspectj.weaver.loadtime;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.ProtectionDomain;
import java.util.*;

import org.aspectj.bridge.AbortException;
import org.aspectj.bridge.Constants;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.util.LangUtil;
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
import org.aspectj.weaver.tools.*;
import org.aspectj.weaver.tools.cache.WeavedClassCache;

/**
 * @author Alexandre Vasseur
 * @author Andy Clement
 * @author Abraham Nevado
 */
public class ClassLoaderWeavingAdaptor extends WeavingAdaptor {

	private final static String AOP_XML = Constants.AOP_USER_XML + ";" + Constants.AOP_AJC_XML + ";" + Constants.AOP_OSGI_XML;

	private boolean initialized;

	private List m_dumpTypePattern = new ArrayList();
	private boolean m_dumpBefore = false;
	private boolean dumpDirPerClassloader = false;

	private boolean hasExcludes = false;
	private List<TypePattern> excludeTypePattern = new ArrayList<TypePattern>(); // anything
	private List<String> excludeStartsWith = new ArrayList<String>(); // com.foo..*
	private List<String> excludeStarDotDotStar = new ArrayList<String>(); // *..*CGLIB*
	private List<String> excludeExactName = new ArrayList<String>(); // com.foo.Bar
	private List<String> excludeEndsWith = new ArrayList<String>(); // com.foo.Bar
	private List<String[]> excludeSpecial = new ArrayList<String[]>();

	private boolean hasIncludes = false;
	private List<TypePattern> includeTypePattern = new ArrayList<TypePattern>();
	private List<String> m_includeStartsWith = new ArrayList<String>();
	private List<String> includeExactName = new ArrayList<String>();
	private boolean includeStar = false;

	private List<TypePattern> m_aspectExcludeTypePattern = new ArrayList<TypePattern>();
	private List<String> m_aspectExcludeStartsWith = new ArrayList<String>();
	private List<TypePattern> m_aspectIncludeTypePattern = new ArrayList<TypePattern>();
	private List<String> m_aspectIncludeStartsWith = new ArrayList<String>();

	private StringBuffer namespace;
	private IWeavingContext weavingContext;

	private List<ConcreteAspectCodeGen> concreteAspects = new ArrayList<ConcreteAspectCodeGen>();

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

		List definitions = weavingContext.getDefinitions(classLoader, this);
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
		List<String> aspects = new LinkedList<String>();
		for (Iterator<Definition> it = definitions.iterator(); it.hasNext(); ) {
			Definition def = it.next();
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

		List<Definition> definitions = new ArrayList<Definition>();
		try {
			info("register classloader " + getClassLoaderName(loader));
			// TODO av underoptimized: we will parse each XML once per CL that see it

			// TODO av dev mode needed ? TBD -Daj5.def=...
			if (loader.equals(ClassLoader.getSystemClassLoader())) {
				String file = System.getProperty("aj5.def", null);
				if (file != null) {
					info("using (-Daj5.def) " + file);
					definitions.add(DocumentParser.parse((new File(file)).toURL()));
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
							definitions.add(DocumentParser.parse(configFile.toURL()));
						}
					} catch (MalformedURLException mue) {
						error("malformed definition url: " + nextDefinition);
					}
				} else {
					Enumeration<URL> xmls = weavingContext.getResources(nextDefinition);
					// System.out.println("? registerDefinitions: found-aop.xml=" + xmls.hasMoreElements() + ", loader=" + loader);

					Set<URL> seenBefore = new HashSet<URL>();
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
		StringBuffer allOptions = new StringBuffer();
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
		world.setBehaveInJava5Way(LangUtil.is15VMOrGreater());
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
				m_aspectExcludeTypePattern.add(excludePattern);
				fastMatchInfo = looksLikeStartsWith(exclude);
				if (fastMatchInfo != null) {
					m_aspectExcludeStartsWith.add(fastMatchInfo);
				}
			}
		}
	}

	private void registerAspectInclude(final BcelWeaver weaver, final ClassLoader loader, final List<Definition> definitions) {
		String fastMatchInfo = null;
		for (Definition definition : definitions) {
			for (String include : definition.getAspectIncludePatterns()) {
				TypePattern includePattern = new PatternParser(include).parseTypePattern();
				m_aspectIncludeTypePattern.add(includePattern);
				fastMatchInfo = looksLikeStartsWith(include);
				if (fastMatchInfo != null) {
					m_aspectIncludeStartsWith.add(fastMatchInfo);
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
			for (Iterator<String> iterator1 = definition.getIncludePatterns().iterator(); iterator1.hasNext();) {
				hasIncludes = true;
				String include = iterator1.next();
				fastMatchInfo = looksLikeStartsWith(include);
				if (fastMatchInfo != null) {
					m_includeStartsWith.add(fastMatchInfo);
				} else if (include.equals("*")) {
					includeStar = true;
				} else if ((fastMatchInfo = looksLikeExactName(include)) != null) {
					includeExactName.add(fastMatchInfo);
				} else {
					TypePattern includePattern = new PatternParser(include).parseTypePattern();
					includeTypePattern.add(includePattern);
				}
			}
			for (Iterator<String> iterator1 = definition.getExcludePatterns().iterator(); iterator1.hasNext();) {
				hasExcludes = true;
				String exclude = iterator1.next();
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
					excludeSpecial.add(new String[] { "org.codehaus.groovy.",
							"org.codehaus.groovy.grails.web.servlet.mvc.SimpleGrailsController" });
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
		if (hasSpaceAnnotationPlus(typePattern, 0) || typePattern.indexOf("*") != -1) {
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
			for (Iterator<String> iterator1 = definition.getDumpPatterns().iterator(); iterator1.hasNext();) {
				String dump = iterator1.next();
				TypePattern pattern = new PatternParser(dump).parseTypePattern();
				m_dumpTypePattern.add(pattern);
			}
			if (definition.shouldDumpBefore()) {
				m_dumpBefore = true;
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
			for (int i = 0; i < m_includeStartsWith.size(); i++) {
				didSomeIncludeMatching = true;
				fastAccept = fastClassName.startsWith(m_includeStartsWith.get(i));
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
			for (int i = 0; i < m_includeStartsWith.size(); i++) {
				didSomeIncludeMatching = true;
				boolean fastaccept = fastClassName.startsWith(m_includeStartsWith.get(i));
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
		if (m_aspectExcludeTypePattern.isEmpty() && m_aspectIncludeTypePattern.isEmpty()) {
			return true;
		}

		// still try to avoid ResolvedType if we have simple patterns
		// EXCLUDE: if one match then reject
		String fastClassName = aspectClassName.replace('/', '.').replace('.', '$');
		for (int i = 0; i < m_aspectExcludeStartsWith.size(); i++) {
			if (fastClassName.startsWith(m_aspectExcludeStartsWith.get(i))) {
				return false;
			}
		}
		// INCLUDE: if one match then accept
		for (int i = 0; i < m_aspectIncludeStartsWith.size(); i++) {
			if (fastClassName.startsWith(m_aspectIncludeStartsWith.get(i))) {
				return true;
			}
		}

		// needs further analysis
		ResolvedType classInfo = weaver.getWorld().resolve(UnresolvedType.forName(aspectClassName), true);
		// exclude are "AND"ed
		for (Iterator iterator = m_aspectExcludeTypePattern.iterator(); iterator.hasNext();) {
			TypePattern typePattern = (TypePattern) iterator.next();
			if (typePattern.matchesStatically(classInfo)) {
				// exclude match - skip
				return false;
			}
		}
		// include are "OR"ed
		boolean accept = true;// defaults to true if no include
		for (Iterator iterator = m_aspectIncludeTypePattern.iterator(); iterator.hasNext();) {
			TypePattern typePattern = (TypePattern) iterator.next();
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
		if (before && !m_dumpBefore) {
			return false;
		}

		// avoid ResolvedType if not needed
		if (m_dumpTypePattern.isEmpty()) {
			return false;
		}

		// TODO AV - optimize for className.startWith only
		ResolvedType classInfo = weaver.getWorld().resolve(UnresolvedType.forName(className), true);
		// dump
		for (Iterator iterator = m_dumpTypePattern.iterator(); iterator.hasNext();) {
			TypePattern typePattern = (TypePattern) iterator.next();
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
			StringBuffer dir = new StringBuffer();
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
		generatedClasses = new HashMap();
	}

	private Method defineClassMethod;
	private Method defineClassWithProtectionDomainMethod;

	private void defineClass(ClassLoader loader, String name, byte[] bytes) {
		if (trace.isTraceEnabled()) {
			trace.enter("defineClass", this, new Object[] { loader, name, bytes });
		}
		Object clazz = null;
		debug("generating class '" + name + "'");

		try {
			if (defineClassMethod == null) {
				defineClassMethod = ClassLoader.class.getDeclaredMethod("defineClass", new Class[] { String.class,
						bytes.getClass(), int.class, int.class });
			}
			defineClassMethod.setAccessible(true);
			clazz = defineClassMethod.invoke(loader, new Object[] { name, bytes, new Integer(0), new Integer(bytes.length) });
		} catch (InvocationTargetException e) {
			if (e.getTargetException() instanceof LinkageError) {
				warn("define generated class failed", e.getTargetException());
				// is already defined (happens for X$ajcMightHaveAspect interfaces since aspects are reweaved)
				// TODO maw I don't think this is OK and
			} else {
				warn("define generated class failed", e.getTargetException());
			}
		} catch (Exception e) {
			warn("define generated class failed", e);
		}

		if (trace.isTraceEnabled()) {
			trace.exit("defineClass", clazz);
		}
	}

	private void defineClass(ClassLoader loader, String name, byte[] bytes, ProtectionDomain protectionDomain) {
		if (trace.isTraceEnabled()) {
			trace.enter("defineClass", this, new Object[] { loader, name, bytes, protectionDomain });
		}
		Object clazz = null;
		debug("generating class '" + name + "'");

		try {
			// System.out.println(">> Defining with protection domain " + name + " pd=" + protectionDomain);
			if (defineClassWithProtectionDomainMethod == null) {
				defineClassWithProtectionDomainMethod = ClassLoader.class.getDeclaredMethod("defineClass", new Class[] {
						String.class, bytes.getClass(), int.class, int.class, ProtectionDomain.class });
			}
			defineClassWithProtectionDomainMethod.setAccessible(true);
			clazz = defineClassWithProtectionDomainMethod.invoke(loader, new Object[] { name, bytes, Integer.valueOf(0),
					new Integer(bytes.length), protectionDomain });
		} catch (InvocationTargetException e) {
			if (e.getTargetException() instanceof LinkageError) {
				warn("define generated class failed", e.getTargetException());
				// is already defined (happens for X$ajcMightHaveAspect interfaces since aspects are reweaved)
				// TODO maw I don't think this is OK and
			} else {
				warn("define generated class failed", e.getTargetException());
			}
		} catch (Exception e) {
			warn("define generated class failed", e);
		}

		if (trace.isTraceEnabled()) {
			trace.exit("defineClass", clazz);
		}
	}
}