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

import org.aspectj.asm.IRelationship;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.bridge.Message;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.weaver.ICrossReferenceHandler;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.World;
import org.aspectj.weaver.bcel.BcelWeaver;
import org.aspectj.weaver.bcel.BcelWorld;
import org.aspectj.weaver.loadtime.definition.Definition;
import org.aspectj.weaver.loadtime.definition.DocumentParser;
import org.aspectj.weaver.patterns.PatternParser;
import org.aspectj.weaver.patterns.TypePattern;
import org.aspectj.weaver.tools.GeneratedClassHandler;
import org.aspectj.weaver.tools.WeavingAdaptor;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class ClassLoaderWeavingAdaptor extends WeavingAdaptor {

    private final static String AOP_XML = "META-INF/aop.xml";

    private List m_dumpTypePattern = new ArrayList();
    private List m_includeTypePattern = new ArrayList();
    private List m_excludeTypePattern = new ArrayList();
    private List m_aspectExcludeTypePattern = new ArrayList();
    
    private StringBuffer namespace;
    private IWeavingContext weavingContext;

    public ClassLoaderWeavingAdaptor(final ClassLoader loader, IWeavingContext wContext) {
        super(null);// at this stage we don't have yet a generatedClassHandler to define to the VM the closures
        this.generatedClassHandler = new GeneratedClassHandler() {
            /**
             * Callback when we need to define a Closure in the JVM
             *
             * @param name
             * @param bytes
             */
            public void acceptClass(String name, byte[] bytes) {
                //TODO av make dump configurable
                try {
                    if (shouldDump(name.replace('/', '.'))) {
                        Aj.dump(name, bytes);
                    }
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
                Aj.defineClass(loader, name, bytes);// could be done lazily using the hook
            }
        };
        
        if(wContext==null){
        	weavingContext = new DefaultWeavingContext(loader);
        }else{
        	weavingContext = wContext ;
        }

        bcelWorld = new BcelWorld(
                loader, messageHandler, new ICrossReferenceHandler() {
                    public void addCrossReference(ISourceLocation from, ISourceLocation to, IRelationship.Kind kind, boolean runtimeTest) {
                        ;// for tools only
                    }
                }
        );

//            //TODO this AJ code will call
//            //org.aspectj.apache.bcel.Repository.setRepository(this);
//            //ie set some static things
//            //==> bogus as Bcel is expected to be
//            org.aspectj.apache.bcel.Repository.setRepository(new ClassLoaderRepository(loader));

        weaver = new BcelWeaver(bcelWorld);

        // register the definitions
        registerDefinitions(weaver, loader);
        messageHandler = bcelWorld.getMessageHandler();

        // after adding aspects
        weaver.prepareForWeave();
    }

    /**
     * Load and cache the aop.xml/properties according to the classloader visibility rules
     *
     * @param weaver
     * @param loader
     */
    private void registerDefinitions(final BcelWeaver weaver, final ClassLoader loader) {
        try {
            MessageUtil.info(messageHandler, "register classloader " + ((loader!=null)?loader.getClass().getName()+"@"+loader.hashCode():"null"));
            //TODO av underoptimized: we will parse each XML once per CL that see it
            List definitions = new ArrayList();

            //TODO av dev mode needed ? TBD -Daj5.def=...
            if (ClassLoader.getSystemClassLoader().equals(loader)) {
                String file = System.getProperty("aj5.def", null);
                if (file != null) {
                    MessageUtil.info(messageHandler, "using (-Daj5.def) " + file);
                    definitions.add(DocumentParser.parse((new File(file)).toURL()));
                }
            }
            
            String resourcePath = System.getProperty("org.aspectj.weaver.loadtime.configuration",AOP_XML);
    		StringTokenizer st = new StringTokenizer(resourcePath,";");

    		while(st.hasMoreTokens()){
    			Enumeration xmls = weavingContext.getResources(st.nextToken());
//    			System.out.println("? registerDefinitions: found-aop.xml=" + xmls.hasMoreElements() + ", loader=" + loader);


    			while (xmls.hasMoreElements()) {
    			    URL xml = (URL) xmls.nextElement();
    			    MessageUtil.info(messageHandler, "using " + xml.getFile());
    			    definitions.add(DocumentParser.parse(xml));
    			}
    		}

            // still go thru if definitions is empty since we will configure
            // the default message handler in there
            registerOptions(weaver, loader, definitions);
            registerAspectExclude(weaver, loader, definitions);
            registerAspects(weaver, loader, definitions);
            registerIncludeExclude(weaver, loader, definitions);
            registerDump(weaver, loader, definitions);
        } catch (Exception e) {
            weaver.getWorld().getMessageHandler().handleMessage(
                    new Message("Register definition failed", IMessage.WARNING, e, null)
            );
        }
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

        Options.WeaverOption weaverOption = Options.parse(allOptions.toString(), loader);

        // configure the weaver and world
        // AV - code duplicates AspectJBuilder.initWorldAndWeaver()
        World world = weaver.getWorld();
        world.setMessageHandler(weaverOption.messageHandler);
        world.setXlazyTjp(weaverOption.lazyTjp);
        world.setXHasMemberSupportEnabled(weaverOption.hasMember);
        world.setPinpointMode(weaverOption.pinpoint);
        weaver.setReweavableMode(weaverOption.reWeavable, false);
        world.setXnoInline(weaverOption.noInline);
        world.setBehaveInJava5Way(weaverOption.java5);//TODO should be autodetected ?
        //-Xlintfile: first so that lint wins
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
                    world.getMessageHandler().handleMessage(new Message(
                            "Cannot access resource for -Xlintfile:"+weaverOption.lintFile,
                            IMessage.WARNING,
                            failure,
                            null));
                }
            } finally {
                try { resource.close(); } catch (Throwable t) {;}
            }
        }
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
        for (Iterator iterator = definitions.iterator(); iterator.hasNext();) {
            Definition definition = (Definition) iterator.next();
            for (Iterator iterator1 = definition.getAspectExcludePatterns().iterator(); iterator1.hasNext();) {
                String exclude = (String) iterator1.next();
                TypePattern excludePattern = new PatternParser(exclude).parseTypePattern();
                m_aspectExcludeTypePattern.add(excludePattern);
            }
        }
    }

    /**
     * Register the aspect, following include / exclude rules
     *
     * @param weaver
     * @param loader
     * @param definitions
     */
    private void registerAspects(final BcelWeaver weaver, final ClassLoader loader, final List definitions) {
        //TODO: the exclude aspect allow to exclude aspect defined upper in the CL hierarchy - is it what we want ??
        // if not, review the getResource so that we track which resource is defined by which CL

        //it aspectClassNames
        //exclude if in any of the exclude list
        for (Iterator iterator = definitions.iterator(); iterator.hasNext();) {
            Definition definition = (Definition) iterator.next();
            for (Iterator aspects = definition.getAspectClassNames().iterator(); aspects.hasNext();) {
                String aspectClassName = (String) aspects.next();
                if (acceptAspect(aspectClassName)) {
                    weaver.addLibraryAspect(aspectClassName);
                	
                	//generate key for SC
                	String aspectCode = readAspect(aspectClassName, loader);
                    if(namespace==null){
                    	namespace=new StringBuffer(aspectCode);
                    }else{
                    	namespace = namespace.append(";"+aspectCode);
                    }
                }
            }
        }

        //it concreteAspects
        //exclude if in any of the exclude list
        //TODO
    }

    /**
     * Register the include / exclude filters
     *
     * @param weaver
     * @param loader
     * @param definitions
     */
    private void registerIncludeExclude(final BcelWeaver weaver, final ClassLoader loader, final List definitions) {
        for (Iterator iterator = definitions.iterator(); iterator.hasNext();) {
            Definition definition = (Definition) iterator.next();
            for (Iterator iterator1 = definition.getIncludePatterns().iterator(); iterator1.hasNext();) {
                String include = (String) iterator1.next();
                TypePattern includePattern = new PatternParser(include).parseTypePattern();
                m_includeTypePattern.add(includePattern);
            }
            for (Iterator iterator1 = definition.getExcludePatterns().iterator(); iterator1.hasNext();) {
                String exclude = (String) iterator1.next();
                TypePattern excludePattern = new PatternParser(exclude).parseTypePattern();
                m_excludeTypePattern.add(excludePattern);
            }
        }
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
        }
    }

    protected boolean accept(String className) {
        // avoid ResolvedType if not needed
        if (m_excludeTypePattern.isEmpty() && m_includeTypePattern.isEmpty()) {
            return true;
        }
        //TODO AV - optimize for className.startWith only
        ResolvedType classInfo = weaver.getWorld().resolve(UnresolvedType.forName(className), true);
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

    private boolean acceptAspect(String aspectClassName) {
        // avoid ResolvedType if not needed
        if (m_aspectExcludeTypePattern.isEmpty()) {
            return true;
        }
        //TODO AV - optimize for className.startWith only
        ResolvedType classInfo = weaver.getWorld().resolve(UnresolvedType.forName(aspectClassName), true);
        //exclude are "AND"ed
        for (Iterator iterator = m_aspectExcludeTypePattern.iterator(); iterator.hasNext();) {
            TypePattern typePattern = (TypePattern) iterator.next();
            if (typePattern.matchesStatically(classInfo)) {
                // exclude match - skip
                return false;
            }
        }
        return true;
    }

    public boolean shouldDump(String className) {
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
		if(namespace==null) return "";
		else return new String(namespace);
	}

    /**
     * Check to see if any classes are stored in the generated classes cache.
     * Then flush the cache if it is not empty
     * @return true if a class has been generated and is stored in the cache
     */
    public boolean generatedClassesExist(){
    	if(generatedClasses.size()>0) {
    		return true;
    	}
    	return false;
    }
    
    /**
     * Flush the generated classes cache
     */
    public void flushGeneratedClasses(){
    	generatedClasses = new HashMap();
    }
    

    /**
     * Read in an aspect from the disk and return its bytecode as a String
     * @param name	the name of the aspect to read in
     * @return the bytecode representation of the aspect
     */
    private String readAspect(String name, ClassLoader loader){
    	try {
    		String result = "";
        	InputStream is = loader.getResourceAsStream(name.replace('.','/')+".class");
			int b = is.read();
			while(b!=-1){
				result = result + b;
				b=is.read();
			}
			is.close();
	    	return result;
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}catch (NullPointerException e) {
			//probably tried to read in a "non aspect @missing@" aspect
			System.err.println("ClassLoaderWeavingAdaptor.readAspect() name: "+name+"  Exception: "+e);
			return "";
		}
    }
    
}
