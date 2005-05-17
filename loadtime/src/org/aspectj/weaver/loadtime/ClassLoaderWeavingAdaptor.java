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
package org.aspectj.weaver.loadtime;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import org.aspectj.asm.IRelationship;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.bridge.Message;
import org.aspectj.weaver.ICrossReferenceHandler;
import org.aspectj.weaver.ResolvedTypeX;
import org.aspectj.weaver.TypeX;
import org.aspectj.weaver.World;
import org.aspectj.weaver.bcel.BcelWeaver;
import org.aspectj.weaver.bcel.BcelWorld;
import org.aspectj.weaver.loadtime.definition.Definition;
import org.aspectj.weaver.loadtime.definition.DocumentParser;
import org.aspectj.weaver.patterns.PatternParser;
import org.aspectj.weaver.patterns.TypePattern;
import org.aspectj.weaver.tools.GeneratedClassHandler;
import org.aspectj.weaver.tools.WeavingAdaptor;

/**
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class ClassLoaderWeavingAdaptor extends WeavingAdaptor {

    //ATAJ LTW include/exclude
    private List m_includeTypePattern = new ArrayList();
    private List m_excludeTypePattern = new ArrayList();
    private List m_aspectExcludeTypePattern = new ArrayList();
    public void addIncludeTypePattern(TypePattern typePattern) {
        m_includeTypePattern.add(typePattern);
    }
    public void addExcludeTypePattern(TypePattern typePattern) {
        m_excludeTypePattern.add(typePattern);
    }
    public void addAspectExcludeTypePattern(TypePattern typePattern) {
        m_aspectExcludeTypePattern.add(typePattern);
    }

    public ClassLoaderWeavingAdaptor(final ClassLoader loader) {
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
                    Aj.__dump(name, bytes);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
                Aj.defineClass(loader, name, bytes);// could be done lazily using the hook
            }
        };

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
            //TODO av underoptimized: we will parse each XML once per CL that see it
            Enumeration xmls = loader.getResources("/META-INF/aop.xml");
            List definitions = new ArrayList();

            //TODO av dev mode needed ? TBD -Daj5.def=...
            if (loader != null && loader != ClassLoader.getSystemClassLoader().getParent()) {
                String file = System.getProperty("aj5.def", null);
                if (file != null) {
                    definitions.add(DocumentParser.parse((new File(file)).toURL()));
                }
            }

            while (xmls.hasMoreElements()) {
                URL xml = (URL) xmls.nextElement();
                definitions.add(DocumentParser.parse(xml));
            }
            
            // still go thru if definitions is empty since we will configure
            // the default message handler in there
            registerOptions(weaver, loader, definitions);
            registerAspectExclude(weaver, loader, definitions);
            registerAspects(weaver, loader, definitions);
            registerIncludeExclude(weaver, loader, definitions);
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
        weaver.setReweavableMode(weaverOption.reWeavable, false);
        world.setXnoInline(weaverOption.noInline);
        world.setBehaveInJava5Way(weaverOption.java5);
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

    protected boolean accept(String className) {
        // avoid ResolvedType if not needed
        if (m_excludeTypePattern.isEmpty() && m_includeTypePattern.isEmpty()) {
            return true;
        }
        //TODO AV - optimize for className.startWith only
        ResolvedTypeX classInfo = weaver.getWorld().getCoreType(TypeX.forName(className));
        //exclude
        for (Iterator iterator = m_excludeTypePattern.iterator(); iterator.hasNext();) {
            TypePattern typePattern = (TypePattern) iterator.next();
            if (typePattern.matchesStatically(classInfo)) {
                // exclude match - skip
                return false;
            }
        }
        for (Iterator iterator = m_includeTypePattern.iterator(); iterator.hasNext();) {
            TypePattern typePattern = (TypePattern) iterator.next();
            if (! typePattern.matchesStatically(classInfo)) {
                // include does not match - skip
                return false;
            }
        }
        return true;
    }

    private boolean acceptAspect(String aspectClassName) {
        // avoid ResolvedType if not needed
        if (m_aspectExcludeTypePattern.isEmpty()) {
            return true;
        }
        //TODO AV - optimize for className.startWith only
        ResolvedTypeX classInfo = weaver.getWorld().getCoreType(TypeX.forName(aspectClassName));
        //exclude
        for (Iterator iterator = m_aspectExcludeTypePattern.iterator(); iterator.hasNext();) {
            TypePattern typePattern = (TypePattern) iterator.next();
            if (typePattern.matchesStatically(classInfo)) {
                // exclude match - skip
                return false;
            }
        }
        return true;
    }

}
