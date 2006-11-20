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

import org.aspectj.apache.bcel.Constants;
import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.generic.InstructionConstants;
import org.aspectj.apache.bcel.generic.InstructionList;
import org.aspectj.apache.bcel.generic.ObjectType;
import org.aspectj.apache.bcel.generic.Type;
import org.aspectj.apache.bcel.generic.annotation.AnnotationGen;
import org.aspectj.apache.bcel.generic.annotation.ElementNameValuePairGen;
import org.aspectj.apache.bcel.generic.annotation.ElementValueGen;
import org.aspectj.apache.bcel.generic.annotation.SimpleElementValueGen;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.Message;
import org.aspectj.weaver.AnnotationX;
import org.aspectj.weaver.ReferenceType;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.World;
import org.aspectj.weaver.bcel.BcelPerClauseAspectAdder;
import org.aspectj.weaver.bcel.BcelWorld;
import org.aspectj.weaver.bcel.LazyClassGen;
import org.aspectj.weaver.bcel.LazyMethodGen;
import org.aspectj.weaver.loadtime.definition.Definition;
import org.aspectj.weaver.patterns.PerClause;
import org.aspectj.weaver.patterns.PerSingleton;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Generates bytecode for concrete-aspect
 * <p/>
 * The concrete aspect is @AspectJ code generated. As it is build during aop.xml definitions registration
 * we perform the type munging for perclause ie aspectOf artifact directly, instead of waiting for it
 * to go thru the weaver (that we are in the middle of configuring).
 *
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class ConcreteAspectCodeGen {

    private final static String[] EMPTY_STRINGS = new String[0];
    private final static Type[] EMPTY_TYPES = new Type[0];

    /**
     * Concrete aspect definition we build for
     */
    private final Definition.ConcreteAspect m_concreteAspect;

    /**
     * World for which we build for
     */
    private final World m_world;

    /**
     * Set to true when all is checks are verified
     */
    private boolean m_isValid = false;

    /**
     * The parent aspect, not concretized
     */
    private ResolvedType m_parent;

    /**
     * Aspect perClause, used for direct munging of aspectOf artifacts
     */
    private PerClause m_perClause;

    /**
     * Create a new compiler for a concrete aspect
     *
     * @param concreteAspect
     * @param world
     */
    ConcreteAspectCodeGen(Definition.ConcreteAspect concreteAspect, World world) {
        m_concreteAspect = concreteAspect;
        m_world = world;
    }

    /**
     * Checks that concrete aspect is valid
     *
     * @return true if ok, false otherwise
     */
    public boolean validate() {
        if (!(m_world instanceof BcelWorld)) {
            reportError("Internal error: world must be of type BcelWorld");
            return false;
        }

        // name must be undefined so far
        // TODO only convert the name to signature once, probably earlier than this
        ResolvedType current = m_world.lookupBySignature(UnresolvedType.forName(m_concreteAspect.name).getSignature());
        
        if (current!=null && !current.isMissing()) {
            reportError("Attempt to concretize but chosen aspect name already defined: " + stringify());
            return false;
        }

        // it can happen that extends is null, for precedence only declaration
        if (m_concreteAspect.extend == null && m_concreteAspect.precedence != null) {
            if (m_concreteAspect.pointcuts.isEmpty()) {
                m_isValid = true;
                m_perClause = new PerSingleton();
                m_parent = null;
                return true;// no need to checks more in that special case
            } else {
                reportError("Attempt to use nested pointcuts without extends clause: "+stringify());
                return false;
            }
        }

        m_parent = m_world.resolve(m_concreteAspect.extend, true);
        // handle inner classes
        if (m_parent.isMissing()) {
            // fallback on inner class lookup mechanism
            String fixedName = m_concreteAspect.extend;
            int hasDot = fixedName.lastIndexOf('.');
            while (hasDot > 0) {
                char[] fixedNameChars = fixedName.toCharArray();
                fixedNameChars[hasDot] = '$';
                fixedName = new String(fixedNameChars);
                hasDot = fixedName.lastIndexOf('.');
                m_parent = m_world.resolve(UnresolvedType.forName(fixedName), true);
                if (!m_parent.isMissing()) {
                    break;
                }
            }
        }
        if (m_parent.isMissing()) {
            reportError("Cannot find m_parent aspect for: " + stringify());
            return false;
        }

        // extends must be abstract
        if (!m_parent.isAbstract()) {
            reportError("Attempt to concretize a non-abstract aspect: " + stringify());
            return false;
        }

        // m_parent must be aspect
        if (!m_parent.isAspect()) {
            reportError("Attempt to concretize a non aspect: " + stringify());
            return false;
        }

        // must have all abstractions defined
        List elligibleAbstractions = new ArrayList();
        
        Collection abstractMethods = getOutstandingAbstractMethods(m_parent);
        for (Iterator iter = abstractMethods.iterator(); iter.hasNext();) {
			ResolvedMember method = (ResolvedMember) iter.next();
			if ("()V".equals(method.getSignature())) {
			 String n = method.getName();
            	 if (n.startsWith("ajc$pointcut")) { // Allow for the abstract pointcut being from a code style aspect compiled with -1.5 (see test for 128744)
            		n = n.substring(14);
            		n = n.substring(0,n.indexOf("$"));
            		elligibleAbstractions.add(n);         
            	 } else if (hasPointcutAnnotation(method)) {
         			elligibleAbstractions.add(method.getName());
            	 } else {
             	 // error, an outstanding abstract method that can't be concretized in XML
            		 reportError("Abstract method '" + method.toString() + "' cannot be concretized in XML: " + stringify());
                  return false;
            	 }
            } else {
            	  if (method.getName().startsWith("ajc$pointcut") || hasPointcutAnnotation(method)) {
            		// it may be a pointcut but it doesn't meet the requirements for XML concretization
            		reportError("Abstract method '" + method.toString() + "' cannot be concretized as a pointcut (illegal signature, must have no arguments, must return void): " + stringify());
                 return false;
            	  } else {
            		// error, an outstanding abstract method that can't be concretized in XML
                 reportError("Abstract method '" + method.toString() + "' cannot be concretized in XML: " + stringify());
                 return false;
            	  }
            }
		}
        List pointcutNames = new ArrayList();
        for (Iterator it = m_concreteAspect.pointcuts.iterator(); it.hasNext();) {
            Definition.Pointcut abstractPc = (Definition.Pointcut) it.next();
            pointcutNames.add(abstractPc.name);
        }
        for (Iterator it = elligibleAbstractions.iterator(); it.hasNext();) {
            String elligiblePc = (String) it.next();
            if (!pointcutNames.contains(elligiblePc)) {
                reportError("Abstract pointcut '" + elligiblePc + "' not configured: " + stringify());
                return false;
            }
        }

        m_perClause = m_parent.getPerClause();
        m_isValid = true;
        return m_isValid;
    }

    private Collection getOutstandingAbstractMethods(ResolvedType type) {
    		Map collector = new HashMap();
    		// let's get to the top of the hierarchy and then walk down ... recording abstract methods then removing
    		// them if they get defined further down the hierarchy
    		getOutstandingAbstractMethodsHelper(type,collector);
		return collector.values();
	}
    
    // We are trying to determine abstract methods left over at the bottom of a hierarchy that have not been
    // concretized.
    private void getOutstandingAbstractMethodsHelper(ResolvedType type,Map collector) {
    	  if (type==null) return;
    	  // Get to the top
    	  if (type!=null && !type.equals(ResolvedType.OBJECT)) {
    		  if (type.getSuperclass()!=null)
    		    getOutstandingAbstractMethodsHelper(type.getSuperclass(),collector);
    	  }
    	  ResolvedMember[] rms = type.getDeclaredMethods();
    	  if (rms!=null) {
	    	  for (int i = 0; i < rms.length; i++) {
				ResolvedMember member = rms[i];
				String key = member.getName()+member.getSignature();
				if (member.isAbstract()) {
					collector.put(key,member);
				} else {
					collector.remove(key);
				}
		  }
    	  }
    }

	/**
     * Rebuild the XML snip that defines this concrete aspect, for log error purpose
     *
     * @return string repr.
     */
    private String stringify() {
        StringBuffer sb = new StringBuffer("<concrete-aspect name='");
        sb.append(m_concreteAspect.name);
        sb.append("' extends='");
        sb.append(m_concreteAspect.extend);
        sb.append("'/> in aop.xml");
        return sb.toString();
    }
    
    private boolean hasPointcutAnnotation(ResolvedMember member) {
    	  AnnotationX[] as = member.getAnnotations();
    	  if (as==null || as.length==0) return false;
    	  for (int i = 0; i < as.length; i++) {
			if (as[i].getTypeSignature().equals("Lorg/aspectj/lang/annotation/Pointcut;")) {
				return true;
			}
		}
    	  return false;
    }

    public String getClassName () {
    	return m_concreteAspect.name;
    }
    
    /**
     * Build the bytecode for the concrete aspect
     *
     * @return concrete aspect bytecode
     */
    public byte[] getBytes() {
        if (!m_isValid) {
            throw new RuntimeException("Must validate first");
        }

        //TODO AV - abstract away from BCEL...
        // @Aspect //inherit clause from m_parent
        // @DeclarePrecedence("....") // if any
        // public class xxxName [extends xxxExtends] {
        //    [@Pointcut(xxxExpression-n)
        //    public void xxxName-n() {}]
        // }

        // @Aspect public class ...
        LazyClassGen cg = new LazyClassGen(
                m_concreteAspect.name.replace('.', '/'),
                (m_parent==null)?"java/lang/Object":m_parent.getName().replace('.', '/'),
                null,//TODO AV - we could point to the aop.xml that defines it and use JSR-45
                Modifier.PUBLIC + Constants.ACC_SUPER,
                EMPTY_STRINGS,
                m_world
        );
        AnnotationGen ag = new AnnotationGen(
                new ObjectType("org/aspectj/lang/annotation/Aspect"),
                Collections.EMPTY_LIST,
                true,
                cg.getConstantPoolGen()
        );
        cg.addAnnotation(ag.getAnnotation());
        if (m_concreteAspect.precedence != null) {
            SimpleElementValueGen svg = new SimpleElementValueGen(
                    ElementValueGen.STRING,
                    cg.getConstantPoolGen(),
                    m_concreteAspect.precedence
            );
            List elems = new ArrayList();
            elems.add(new ElementNameValuePairGen("value", svg, cg.getConstantPoolGen()));
            AnnotationGen agprec = new AnnotationGen(
                    new ObjectType("org/aspectj/lang/annotation/DeclarePrecedence"),
                    elems,
                    true,
                    cg.getConstantPoolGen()
            );
            cg.addAnnotation(agprec.getAnnotation());
        }

        // default constructor
        LazyMethodGen init = new LazyMethodGen(
                Modifier.PUBLIC,
                Type.VOID,
                "<init>",
                EMPTY_TYPES,
                EMPTY_STRINGS,
                cg
        );
        InstructionList cbody = init.getBody();
        cbody.append(InstructionConstants.ALOAD_0);
        cbody.append(cg.getFactory().createInvoke(
                (m_parent==null)?"java/lang/Object":m_parent.getName().replace('.', '/'),
                "<init>",
                Type.VOID,
                EMPTY_TYPES,
                Constants.INVOKESPECIAL
        ));
        cbody.append(InstructionConstants.RETURN);
        cg.addMethodGen(init);

        for (Iterator it = m_concreteAspect.pointcuts.iterator(); it.hasNext();) {
            Definition.Pointcut abstractPc = (Definition.Pointcut) it.next();

            LazyMethodGen mg = new LazyMethodGen(
                    Modifier.PUBLIC,//TODO AV - respect visibility instead of opening up?
                    Type.VOID,
                    abstractPc.name,
                    EMPTY_TYPES,
                    EMPTY_STRINGS,
                    cg
            );
            SimpleElementValueGen svg = new SimpleElementValueGen(
                    ElementValueGen.STRING,
                    cg.getConstantPoolGen(),
                    abstractPc.expression
            );
            List elems = new ArrayList();
            elems.add(new ElementNameValuePairGen("value", svg, cg.getConstantPoolGen()));
            AnnotationGen mag = new AnnotationGen(
                    new ObjectType("org/aspectj/lang/annotation/Pointcut"),
                    elems,
                    true,
                    cg.getConstantPoolGen()
            );
            AnnotationX max = new AnnotationX(mag.getAnnotation(), m_world);
            mg.addAnnotation(max);

            InstructionList body = mg.getBody();
            body.append(InstructionConstants.RETURN);

            cg.addMethodGen(mg);
        }

        // handle the perClause
        ReferenceType rt = new ReferenceType(ResolvedType.forName(m_concreteAspect.name).getSignature(),m_world);
        BcelPerClauseAspectAdder perClauseMunger = new BcelPerClauseAspectAdder(rt,m_perClause.getKind());
        perClauseMunger.forceMunge(cg, false);

        //TODO AV - unsafe cast
        // register the fresh new class into the world repository as it does not exist on the classpath anywhere
        JavaClass jc = cg.getJavaClass((BcelWorld) m_world);
        ((BcelWorld) m_world).addSourceObjectType(jc);

        return jc.getBytes();
    }

    /**
     * Error reporting
     *
     * @param message
     */
    private void reportError(String message) {
        m_world.getMessageHandler().handleMessage(new Message(message, IMessage.ERROR, null, null));
    }
}
