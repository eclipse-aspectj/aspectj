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

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

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

        m_parent = m_world.resolve(m_concreteAspect.extend, true);
        // handle inner classes
        if (m_parent.equals(ResolvedType.MISSING)) {
            // fallback on inner class lookup mechanism
            String fixedName = m_concreteAspect.extend;
            int hasDot = fixedName.lastIndexOf('.');
            while (hasDot > 0) {
                char[] fixedNameChars = fixedName.toCharArray();
                fixedNameChars[hasDot] = '$';
                fixedName = new String(fixedNameChars);
                hasDot = fixedName.lastIndexOf('.');
                m_parent = m_world.resolve(UnresolvedType.forName(fixedName), true);
                if (!m_parent.equals(ResolvedType.MISSING)) {
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

        // must be undefined so far
        ResolvedType current = m_world.resolve(m_concreteAspect.name, true);
        if (!current.isMissing()) {
            reportError("Attempt to concretize but choosen aspect name already defined:" + stringify());
            return false;
        }

        // must have all abstractions defined
        List elligibleAbstractions = new ArrayList();
        Iterator methods = m_parent.getMethods();
        while (methods.hasNext()) {
            ResolvedMember method = (ResolvedMember) methods.next();
            if (method.isAbstract()) {
                if ("()V".equals(method.getSignature())) {
                    elligibleAbstractions.add(method.getName());
                } else {
                    reportError("Abstract method '" + method.getName() + "' cannot be concretized as a pointcut (illegal signature, must have no arguments, must return void): " + stringify());
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
        // public class xxxName extends xxxExtends {
        //    @Pointcut(xxxExpression-n)
        //    private void xxxName-n() {}
        // }

        // @Aspect public class ...
        LazyClassGen cg = new LazyClassGen(
                m_concreteAspect.name.replace('.', '/'),
                m_parent.getName(),
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
                m_parent.getName().replace('.', '/'),
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
                    Modifier.PUBLIC,
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
        BcelPerClauseAspectAdder perClauseMunger = new BcelPerClauseAspectAdder(
                ResolvedType.forName(m_concreteAspect.name).resolve(m_world),
                m_perClause.getKind()
        );
        perClauseMunger.forceMunge(cg);

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
