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
 *******************************************************************************/
package org.aspectj.weaver.loadtime;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import org.aspectj.asm.IRelationship;
import org.aspectj.bridge.AbortException;
import org.aspectj.bridge.Constants;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.util.LangUtil;
import org.aspectj.weaver.ICrossReferenceHandler;
import org.aspectj.weaver.Lint;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.World;
import org.aspectj.weaver.Lint.Kind;
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

/**
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class ClassLoaderWeavingAdaptor extends WeavingAdaptor {

    private final static String AOP_XML = Constants.AOP_USER_XML + ";" + Constants.AOP_AJC_XML + ";" + Constants.AOP_OSGI_XML;

    private boolean initialized;
    
    private List m_dumpTypePattern = new ArrayList();
    private boolean m_dumpBefore = false;
    private List m_includeTypePattern = new ArrayList();
    private List m_excludeTypePattern = new ArrayList();
    private List m_includeStartsWith = new ArrayList();
    private List m_excludeStartsWith = new ArrayList();
    private List m_aspectExcludeTypePattern = new ArrayList();
    private List m_aspectExcludeStartsWith = new ArrayList();
    private List m_aspectIncludeTypePattern = new ArrayList();
    private List m_aspectIncludeStartsWith = new ArrayList();

    private StringBuffer namespace;
    private IWeavingContext weavingContext;

    private List concreteAspects = new ArrayList();

	private static Trace trace = TraceFactory.getTraceFactory().getTrace(ClassLoaderWeavingAdaptor.class);
    
    public ClassLoaderWeavingAdaptor() {
    	super();
    	if (trace.isTraceEnabled()) trace.enter("<init>",this);
    	if (trace.isTraceEnabled()) trace.exit("<init>");
    }
    
    /**
     * We don't need a reference to the class loader and using it during 
     * construction can cause problems with recursion. It also makes sense
     * to supply the weaving context during initialization to. 
     * @deprecated
     */
    public ClassLoaderWeavingAdaptor(final ClassLoader deprecatedLoader, final IWeavingContext deprecatedContext) {
    	super();
    	if (trace.isTraceEnabled()) trace.enter("<init>",this,new Object[] { deprecatedLoader, deprecatedContext });
    	if (trace.isTraceEnabled()) trace.exit("<init>");
    }

    protected void initialize (final ClassLoader classLoader, IWeavingContext context) {
    	if (initialized) return;

    	boolean success = true;
    	if (trace.isTraceEnabled()) trace.enter("initialize",this,new Object[] { classLoader, context });

    	this.weavingContext = context;
        if (weavingContext == null) {
        	weavingContext = new DefaultWeavingContext(classLoader);
        }

        createMessageHandler();
    	
        this.generatedClassHandler = new GeneratedClassHandler() {
            /**
             * Callback when we need to define a Closure in the JVM
             *
             * @param name
             * @param bytes
             */
            public void acceptClass(String name, byte[] bytes) {
                try {
                    if (shouldDump(name.replace('/', '.'), false)) {
                        dump(name, bytes, false);
                    }
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }

                defineClass(classLoader, name, bytes);// could be done lazily using the hook
            }
        };

        List definitions = weavingContext.getDefinitions(classLoader,this);
        if (definitions.isEmpty()) {
        	disable(); // TODO maw Needed to ensure messages are flushed
        	if (trace.isTraceEnabled()) trace.exit("initialize",definitions);
        	return;
        }
        
        bcelWorld = new LTWWorld(
        		classLoader, weavingContext, // TODO when the world works in terms of the context, we can remove the loader...
        		getMessageHandler(), new ICrossReferenceHandler() {
                    public void addCrossReference(ISourceLocation from, ISourceLocation to, IRelationship.Kind kind, boolean runtimeTest) {
                        ;// for tools only
                    }
                }
        );

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
        }
        else {
        	disable();
        	bcelWorld = null;
        	weaver = null;
        }
        
        initialized = true;
    	if (trace.isTraceEnabled()) trace.exit("initialize",isEnabled());
    }

    /**
     * Load and cache the aop.xml/properties according to the classloader visibility rules
     *
     * @param weaver
     * @param loader
     */
    List parseDefinitions(final ClassLoader loader) {
        if (trace.isTraceEnabled()) trace.enter("parseDefinitions",this,loader);

        List definitions = new ArrayList();
    	try {
            info("register classloader " + getClassLoaderName(loader));
            //TODO av underoptimized: we will parse each XML once per CL that see it

            //TODO av dev mode needed ? TBD -Daj5.def=...
            if (loader.equals(ClassLoader.getSystemClassLoader())) {
                String file = System.getProperty("aj5.def", null);
                if (file != null) {
                    info("using (-Daj5.def) " + file);
                    definitions.add(DocumentParser.parse((new File(file)).toURL()));
                }
            }

            String resourcePath = System.getProperty("org.aspectj.weaver.loadtime.configuration",AOP_XML);
            if (trace.isTraceEnabled()) trace.event("parseDefinitions",this,resourcePath);
    		StringTokenizer st = new StringTokenizer(resourcePath,";");

    		while(st.hasMoreTokens()){
    			Enumeration xmls = weavingContext.getResources(st.nextToken());
//    			System.out.println("? registerDefinitions: found-aop.xml=" + xmls.hasMoreElements() + ", loader=" + loader);

    			Set seenBefore = new HashSet();
    			while (xmls.hasMoreElements()) {
    			    URL xml = (URL) xmls.nextElement();
    			    if (trace.isTraceEnabled()) trace.event("parseDefinitions",this,xml);
    			    if (!seenBefore.contains(xml)) {
    			    	info("using configuration " + weavingContext.getFile(xml));
    			    	definitions.add(DocumentParser.parse(xml));
        			    seenBefore.add(xml);
    			    }
    			    else {
    			    	warn("ignoring duplicate definition: " + xml);
    			    }
    			}
    		}
    		if (definitions.isEmpty()) {
        		info("no configuration found. Disabling weaver for class loader " + getClassLoaderName(loader));
    		}
        } catch (Exception e) {
        	definitions.clear();
            warn("parse definitions failed",e);
        }
        
        if (trace.isTraceEnabled()) trace.exit("parseDefinitions",definitions);
		return definitions;
    }
        
    private boolean registerDefinitions(final BcelWeaver weaver, final ClassLoader loader, List definitions) {
        if (trace.isTraceEnabled()) trace.enter("registerDefinitions",this,definitions);
        boolean success = true;

        try {
            registerOptions(weaver, loader, definitions);
            registerAspectExclude(weaver, loader, definitions);
            registerAspectInclude(weaver, loader, definitions);
            success = registerAspects(weaver, loader, definitions);
            registerIncludeExclude(weaver, loader, definitions);
            registerDump(weaver, loader, definitions);
        } catch (Exception ex) {
            trace.error("register definition failed",ex);
            success = false;
            warn("register definition failed",(ex instanceof AbortException)?null:ex);
        }
        
        if (trace.isTraceEnabled()) trace.exit("registerDefinitions",success);
        return success;
    }

    private String getClassLoaderName (ClassLoader loader) {
    	return weavingContext.getClassLoaderName();
   	}
    
    /**
     * Configure the weaver according to the option directives
     * TODO av - don't know if it is that good to reuse, since we only allow a small subset of options in LTW
     *
     * @param weaver
     * @param loader
     * @param definitions
     */
    private void registerOptions(final BcelWeaver weaver, final ClassLoader loader, final List definitions) {
        StringBuffer allOptions = new StringBuffer();
        for (Iterator iterator = definitions.iterator(); iterator.hasNext();) {
            Definition definition = (Definition) iterator.next();
            allOptions.append(definition.getWeaverOptions()).append(' ');
        }

        Options.WeaverOption weaverOption = Options.parse(allOptions.toString(), loader, getMessageHandler());

        // configure the weaver and world
        // AV - code duplicates AspectJBuilder.initWorldAndWeaver()
        World world = weaver.getWorld();
        setMessageHandler(weaverOption.messageHandler);
        world.setXlazyTjp(weaverOption.lazyTjp);
        world.setXHasMemberSupportEnabled(weaverOption.hasMember);
        world.setOptionalJoinpoints(weaverOption.optionalJoinpoints);
        world.setPinpointMode(weaverOption.pinpoint);
        weaver.setReweavableMode(weaverOption.notReWeavable);
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
                	warn("Cannot access resource for -Xlintfile:"+weaverOption.lintFile,failure);
//                    world.getMessageHandler().handleMessage(new Message(
//                            "Cannot access resource for -Xlintfile:"+weaverOption.lintFile,
//                            IMessage.WARNING,
//                            failure,
//                            null));
                }
            } finally {
                try { resource.close(); } catch (Throwable t) {;}
            }
       } 
        
       /* Fourth override with -Xlint */
       if (weaverOption.lint != null) {
            if (weaverOption.lint.equals("default")) {//FIXME should be AjBuildConfig.AJLINT_DEFAULT but yetanother deps..
                bcelWorld.getLint().loadDefaultProperties();
            } else {
                bcelWorld.getLint().setAll(weaverOption.lint);
            }
        }
        //TODO proceedOnError option
    }

    private void registerAspectExclude(final BcelWeaver weaver, final ClassLoader loader, final List definitions) {
        String fastMatchInfo = null;
        for (Iterator iterator = definitions.iterator(); iterator.hasNext();) {
            Definition definition = (Definition) iterator.next();
            for (Iterator iterator1 = definition.getAspectExcludePatterns().iterator(); iterator1.hasNext();) {
                String exclude = (String) iterator1.next();
                TypePattern excludePattern = new PatternParser(exclude).parseTypePattern();
                m_aspectExcludeTypePattern.add(excludePattern);
                fastMatchInfo = looksLikeStartsWith(exclude);
                if (fastMatchInfo != null) {
                    m_aspectExcludeStartsWith.add(fastMatchInfo);
                }
            }
        }
    }

    private void registerAspectInclude(final BcelWeaver weaver, final ClassLoader loader, final List definitions) {
        String fastMatchInfo = null;
        for (Iterator iterator = definitions.iterator(); iterator.hasNext();) {
            Definition definition = (Definition) iterator.next();
            for (Iterator iterator1 = definition.getAspectIncludePatterns().iterator(); iterator1.hasNext();) {
                String include = (String) iterator1.next();
                TypePattern includePattern = new PatternParser(include).parseTypePattern();
                m_aspectIncludeTypePattern.add(includePattern);
                fastMatchInfo = looksLikeStartsWith(include);
                if (fastMatchInfo != null) {
                    m_aspectIncludeStartsWith.add(fastMatchInfo);
                }
            }
        }
    }

    protected void lint (String name, String[] infos) {
    	Lint lint = bcelWorld.getLint();
    	Kind kind = lint.getLintKind(name);
    	kind.signal(infos,null,null);
    }
	
	public String getContextId () {
		return weavingContext.getId();
	}
    
    /**
     * Register the aspect, following include / exclude rules
     *
     * @param weaver
     * @param loader
     * @param definitions
     */
    private boolean registerAspects(final BcelWeaver weaver, final ClassLoader loader, final List definitions) {
    	if (trace.isTraceEnabled()) trace.enter("registerAspects",this, new Object[] { weaver, loader, definitions} );
    	boolean success = true;
    	
        //TODO: the exclude aspect allow to exclude aspect defined upper in the CL hierarchy - is it what we want ??
        // if not, review the getResource so that we track which resource is defined by which CL

        //iterate aspectClassNames
        //exclude if in any of the exclude list
        for (Iterator iterator = definitions.iterator(); iterator.hasNext();) {
            Definition definition = (Definition) iterator.next();
            for (Iterator aspects = definition.getAspectClassNames().iterator(); aspects.hasNext();) {
                String aspectClassName = (String) aspects.next();
                if (acceptAspect(aspectClassName)) {
                	info("register aspect " + aspectClassName);
//                	System.err.println("? ClassLoaderWeavingAdaptor.registerAspects() aspectName=" + aspectClassName + ", loader=" + loader + ", bundle=" + weavingContext.getClassLoaderName());
                    /*ResolvedType aspect = */weaver.addLibraryAspect(aspectClassName);

                    //generate key for SC
                    if(namespace==null){
                    	namespace=new StringBuffer(aspectClassName);
                    }else{
                    	namespace = namespace.append(";"+aspectClassName);
                    }
                }
                else {
//                	warn("aspect excluded: " + aspectClassName);
                	lint("aspectExcludedByConfiguration", new String[] { aspectClassName, getClassLoaderName(loader) });
                }
            }
        }

        //iterate concreteAspects
        //exclude if in any of the exclude list - note that the user defined name matters for that to happen
        for (Iterator iterator = definitions.iterator(); iterator.hasNext();) {
            Definition definition = (Definition) iterator.next();
            for (Iterator aspects = definition.getConcreteAspects().iterator(); aspects.hasNext();) {
                Definition.ConcreteAspect concreteAspect = (Definition.ConcreteAspect) aspects.next();
                if (acceptAspect(concreteAspect.name)) {
                	info("define aspect " + concreteAspect.name);
                    ConcreteAspectCodeGen gen = new ConcreteAspectCodeGen(concreteAspect, weaver.getWorld());
                    if (!gen.validate()) {
                        error("Concrete-aspect '"+concreteAspect.name+"' could not be registered");
                        success = false;
                        break;
                    }
                    
              	  	((BcelWorld)weaver.getWorld()).addSourceObjectType(Utility.makeJavaClass(concreteAspect.name, gen.getBytes()));

              	  	concreteAspects.add(gen);
              	  	
                    weaver.addLibraryAspect(concreteAspect.name);

                    //generate key for SC
                    if(namespace==null){
                    	namespace=new StringBuffer(concreteAspect.name);
                    }else{
                    	namespace = namespace.append(";"+concreteAspect.name);
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

        if (trace.isTraceEnabled()) trace.exit("registerAspects",success);
        return success;
    }

    private boolean weaveAndDefineConceteAspects () {
    	if (trace.isTraceEnabled()) trace.enter("weaveAndDefineConceteAspects",this,concreteAspects);
    	boolean success = true;

    	for (Iterator iterator = concreteAspects.iterator(); iterator.hasNext();) {
    		ConcreteAspectCodeGen gen = (ConcreteAspectCodeGen)iterator.next();
        	String name = gen.getClassName();
        	byte[] bytes = gen.getBytes();

        	try {
        		byte[] newBytes = weaveClass(name, bytes,true);
                this.generatedClassHandler.acceptClass(name,newBytes);
        	}
        	catch (IOException ex) {
        		trace.error("weaveAndDefineConceteAspects",ex);
				error("exception weaving aspect '" + name + "'",ex);
        	}
        }
        
        if (trace.isTraceEnabled()) trace.exit("weaveAndDefineConceteAspects",success);
        return success;
    }
    
    /**
     * Register the include / exclude filters
     * We duplicate simple patterns in startWith filters that will allow faster matching without ResolvedType
     *
     * @param weaver
     * @param loader
     * @param definitions
     */
    private void registerIncludeExclude(final BcelWeaver weaver, final ClassLoader loader, final List definitions) {
        String fastMatchInfo = null;
        for (Iterator iterator = definitions.iterator(); iterator.hasNext();) {
            Definition definition = (Definition) iterator.next();
            for (Iterator iterator1 = definition.getIncludePatterns().iterator(); iterator1.hasNext();) {
                String include = (String) iterator1.next();
                TypePattern includePattern = new PatternParser(include).parseTypePattern();
                m_includeTypePattern.add(includePattern);
                fastMatchInfo = looksLikeStartsWith(include);
                if (fastMatchInfo != null) {
                    m_includeStartsWith.add(fastMatchInfo);
                }
            }
            for (Iterator iterator1 = definition.getExcludePatterns().iterator(); iterator1.hasNext();) {
                String exclude = (String) iterator1.next();
                TypePattern excludePattern = new PatternParser(exclude).parseTypePattern();
                m_excludeTypePattern.add(excludePattern);
                fastMatchInfo = looksLikeStartsWith(exclude);
                if (fastMatchInfo != null) {
                    m_excludeStartsWith.add(fastMatchInfo);
                }
            }
        }
    }

    /**
     * Checks if the type pattern can be handled as a startswith check
     *
     * TODO AV - enhance to support "char.sss" ie FQN direclty (match iff equals)
     * we could also add support for "*..*charss" endsWith style?
     *
     * @param typePattern
     * @return null if not possible, or the startWith sequence to test against
     */
    private String looksLikeStartsWith(String typePattern) {
        if (typePattern.indexOf('@') >= 0
            || typePattern.indexOf('+') >= 0
            || typePattern.indexOf(' ') >= 0
            || typePattern.charAt(typePattern.length()-1) != '*') {
            return null;
        }
        // now must looks like with "charsss..*" or "cha.rss..*" etc
        // note that "*" and "*..*" won't be fast matched
        // and that "charsss.*" will not neither
        int length = typePattern.length();
        if (typePattern.endsWith("..*") && length > 3) {
            if (typePattern.indexOf("..") == length-3 // no ".." before last sequence
                && typePattern.indexOf('*') == length-1) { // no "*" before last sequence
                return typePattern.substring(0, length-2).replace('$', '.');
                // ie "charsss." or "char.rss." etc
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
    private void registerDump(final BcelWeaver weaver, final ClassLoader loader, final List definitions) {
        for (Iterator iterator = definitions.iterator(); iterator.hasNext();) {
            Definition definition = (Definition) iterator.next();
            for (Iterator iterator1 = definition.getDumpPatterns().iterator(); iterator1.hasNext();) {
                String dump = (String) iterator1.next();
                TypePattern pattern = new PatternParser(dump).parseTypePattern();
                m_dumpTypePattern.add(pattern);
            }
            if (definition.shouldDumpBefore()) {
            	m_dumpBefore = true;
            }
        }
    }

    protected boolean accept(String className, byte[] bytes) {
        // avoid ResolvedType if not needed
        if (m_excludeTypePattern.isEmpty() && m_includeTypePattern.isEmpty()) {
            return true;
        }

        // still try to avoid ResolvedType if we have simple patterns
        String fastClassName = className.replace('/', '.').replace('$', '.');
        for (int i = 0; i < m_excludeStartsWith.size(); i++) {
            if (fastClassName.startsWith((String)m_excludeStartsWith.get(i))) {
                return false;
            }
        }
        
        /* 
         * Bug 120363
         * If we have an exclude pattern that cannot be matched using "starts with"
         * then we cannot fast accept
         */ 
        if (m_excludeTypePattern.isEmpty()) {
            boolean fastAccept = false;//defaults to false if no fast include
            for (int i = 0; i < m_includeStartsWith.size(); i++) {
                fastAccept = fastClassName.startsWith((String)m_includeStartsWith.get(i));
                if (fastAccept) {
                    break;
                }
            }
        }

        // needs further analysis
        // TODO AV - needs refactoring
        // during LTW this calling resolve at that stage is BAD as we do have the bytecode from the classloader hook
        // but still go thru resolve that will do a getResourcesAsStream on disk
        // this is also problematic for jit stub which are not on disk - as often underlying infra
        // does returns null or some other info for getResourceAsStream (f.e. WLS 9 CR248491)
        // Instead I parse the given bytecode. But this also means it will be parsed again in
        // new WeavingClassFileProvider() from WeavingAdaptor.getWovenBytes()...
        
        ensureDelegateInitialized(className,bytes);
        ResolvedType classInfo = delegateForCurrentClass.getResolvedTypeX();//BAD: weaver.getWorld().resolve(UnresolvedType.forName(className), true);

        //exclude are "AND"ed
        for (Iterator iterator = m_excludeTypePattern.iterator(); iterator.hasNext();) {
            TypePattern typePattern = (TypePattern) iterator.next();
            if (typePattern.matchesStatically(classInfo)) {
                // exclude match - skip
                return false;
            }
        }
        //include are "OR"ed
        boolean accept = true;//defaults to true if no include
        for (Iterator iterator = m_includeTypePattern.iterator(); iterator.hasNext();) {
            TypePattern typePattern = (TypePattern) iterator.next();
            accept = typePattern.matchesStatically(classInfo);
            if (accept) {
                break;
            }
            // goes on if this include did not match ("OR"ed)
        }
        return accept;
    }


	//FIXME we don't use include/exclude of others aop.xml
    //this can be nice but very dangerous as well to change that
    private boolean acceptAspect(String aspectClassName) {
        // avoid ResolvedType if not needed
        if (m_aspectExcludeTypePattern.isEmpty() && m_aspectIncludeTypePattern.isEmpty()) {
            return true;
        }

        // still try to avoid ResolvedType if we have simple patterns
        // EXCLUDE: if one match then reject
        String fastClassName = aspectClassName.replace('/', '.').replace('.', '$');
        for (int i = 0; i < m_aspectExcludeStartsWith.size(); i++) {
            if (fastClassName.startsWith((String)m_aspectExcludeStartsWith.get(i))) {
                return false;
            }
        }
        //INCLUDE: if one match then accept
        for (int i = 0; i < m_aspectIncludeStartsWith.size(); i++) {
            if (fastClassName.startsWith((String)m_aspectIncludeStartsWith.get(i))) {
                return true;
            }
        }

        // needs further analysis
        ResolvedType classInfo = weaver.getWorld().resolve(UnresolvedType.forName(aspectClassName), true);
        //exclude are "AND"ed
        for (Iterator iterator = m_aspectExcludeTypePattern.iterator(); iterator.hasNext();) {
            TypePattern typePattern = (TypePattern) iterator.next();
            if (typePattern.matchesStatically(classInfo)) {
                // exclude match - skip
                return false;
            }
        }
        //include are "OR"ed
        boolean accept = true;//defaults to true if no include
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

    protected boolean shouldDump(String className, boolean before) {
    	// Don't dump before weaving unless asked to
    	if (before && !m_dumpBefore) {
            return false;
    	}
    	
        // avoid ResolvedType if not needed
        if (m_dumpTypePattern.isEmpty()) {
            return false;
        }

        //TODO AV - optimize for className.startWith only
        ResolvedType classInfo = weaver.getWorld().resolve(UnresolvedType.forName(className), true);
        //dump
        for (Iterator iterator = m_dumpTypePattern.iterator(); iterator.hasNext();) {
            TypePattern typePattern = (TypePattern) iterator.next();
            if (typePattern.matchesStatically(classInfo)) {
                // dump match
                return true;
            }
        }
        return false;
    }

    /*
     *  shared classes methods
     */

    /**
	 * @return Returns the key.
	 */
	public String getNamespace() {
//		System.out.println("ClassLoaderWeavingAdaptor.getNamespace() classloader=" +  weavingContext.getClassLoaderName() + ", namespace=" + namespace);
		if(namespace==null) return "";
		else return new String(namespace);
	}

    /**
     * Check to see if any classes are stored in the generated classes cache.
     * Then flush the cache if it is not empty
     * @param className TODO
     * @return true if a class has been generated and is stored in the cache
     */
    public boolean generatedClassesExistFor (String className) {
//    	System.err.println("? ClassLoaderWeavingAdaptor.generatedClassesExist() classname=" + className + ", size=" + generatedClasses);
    	if (className == null) return !generatedClasses.isEmpty();
    	else return generatedClasses.containsKey(className);
    }

    /**
     * Flush the generated classes cache
     */
    public void flushGeneratedClasses() {
//    	System.err.println("? ClassLoaderWeavingAdaptor.flushGeneratedClasses() generatedClasses=" + generatedClasses);
    	generatedClasses = new HashMap();
    }

	private void defineClass(ClassLoader loader, String name, byte[] bytes) {
    	if (trace.isTraceEnabled()) trace.enter("defineClass",this,new Object[] {loader,name,bytes});
    	Object clazz = null;
		debug("generating class '" + name + "'");
		
		try {
			//TODO av protection domain, and optimize
			Method defineClass = ClassLoader.class.getDeclaredMethod(
					"defineClass", new Class[] { String.class,
							bytes.getClass(), int.class, int.class });
			defineClass.setAccessible(true);
			clazz = defineClass.invoke(loader, new Object[] { name, bytes,
					new Integer(0), new Integer(bytes.length) });
		} catch (InvocationTargetException e) {
			if (e.getTargetException() instanceof LinkageError) {
				warn("define generated class failed",e.getTargetException());
				//is already defined (happens for X$ajcMightHaveAspect interfaces since aspects are reweaved)
				// TODO maw I don't think this is OK and
			} else {
				warn("define generated class failed",e.getTargetException());
			}
		} catch (Exception e) {
			warn("define generated class failed",e);
		}

		if (trace.isTraceEnabled()) trace.exit("defineClass",clazz);
	}
}