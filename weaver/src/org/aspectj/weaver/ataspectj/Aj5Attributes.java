/*******************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/
package org.aspectj.weaver.ataspectj;

import org.aspectj.apache.bcel.classfile.Attribute;
import org.aspectj.apache.bcel.classfile.LocalVariableTable;
import org.aspectj.apache.bcel.classfile.LocalVariable;
import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.classfile.Method;
import org.aspectj.apache.bcel.classfile.Field;
import org.aspectj.apache.bcel.classfile.Signature;
import org.aspectj.apache.bcel.classfile.annotation.Annotation;
import org.aspectj.apache.bcel.classfile.annotation.ElementNameValuePair;
import org.aspectj.apache.bcel.classfile.annotation.RuntimeAnnotations;
import org.aspectj.apache.bcel.classfile.annotation.ArrayElementValue;
import org.aspectj.apache.bcel.classfile.annotation.ClassElementValue;
import org.aspectj.apache.bcel.generic.Type;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.AjAttribute;
import org.aspectj.weaver.AdviceKind;
import org.aspectj.weaver.Advice;
import org.aspectj.weaver.TypeX;
import org.aspectj.weaver.ResolvedTypeX;
import org.aspectj.weaver.ResolvedPointcutDefinition;
import org.aspectj.weaver.AjcMemberMaker;
import org.aspectj.weaver.patterns.PerClause;
import org.aspectj.weaver.patterns.PerSingleton;
import org.aspectj.weaver.patterns.Pointcut;
import org.aspectj.weaver.patterns.SimpleScope;
import org.aspectj.weaver.patterns.FormalBinding;
import org.aspectj.weaver.patterns.IScope;
import org.aspectj.weaver.patterns.PatternParser;
import org.aspectj.weaver.patterns.DeclarePrecedence;
import org.aspectj.weaver.patterns.PerCflow;
import org.aspectj.weaver.patterns.PerObject;
import org.aspectj.weaver.patterns.PerTypeWithin;
import org.aspectj.weaver.patterns.TypePattern;
import org.aspectj.bridge.IMessageHandler;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collections;
import java.util.Comparator;

/**
 * Annotation defined aspect reader.
 * <p/>
 * It reads the Java 5 annotations and turns them into AjAttributes
 *
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class Aj5Attributes {

    private final static List EMPTY_LIST = new ArrayList();
    private final static String[] EMPTY_STRINGS = new String[0];

    public final static TypeX TYPEX_JOINPOINT = TypeX.forName(JoinPoint.class.getName().replace('/','.'));
    public final static TypeX TYPEX_PROCEEDINGJOINPOINT = TypeX.forName(ProceedingJoinPoint.class.getName().replace('/','.'));
    public final static TypeX TYPEX_STATICJOINPOINT = TypeX.forName(JoinPoint.StaticPart.class.getName().replace('/','.'));
    public final static TypeX TYPEX_ENCLOSINGSTATICJOINPOINT = TypeX.forName(JoinPoint.EnclosingStaticPart.class.getName().replace('/','.'));

    /**
     * A struct that allows to add extra arguments without always breaking the API
     *
     * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
     */
    private static class AjAttributeStruct {

        /**
         * The list of AjAttribute.XXX that we are populating from the @AJ read
         */
        List ajAttributes = new ArrayList();

        /**
         * The resolved type (class) for which we are reading @AJ for (be it class, method, field annotations)
         */
        ResolvedTypeX enclosingType;

        ISourceContext context;
        IMessageHandler handler;

        public AjAttributeStruct(ResolvedTypeX type) {
            enclosingType = type;
        }
    }

    /**
     * A struct when we read @AJ on method
     *
     * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
     */
    private static class AjAttributeMethodStruct extends AjAttributeStruct {

        /**
         * Argument names as they appear in the SOURCE code, ordered, and lazyly populated
         * Used to do formal binding
         */
        private String[] m_argumentNamesLazy = null;

        Method method;

        public AjAttributeMethodStruct(Method method, ResolvedTypeX type) {
            super(type);
            this.method = method;
        }

        public String[] getArgumentNames() {
            if (m_argumentNamesLazy == null) {
                m_argumentNamesLazy = getMethodArgumentNamesAsInSource(method);
            }
            return m_argumentNamesLazy;
        }
    }

    /**
     * Annotations are RV or RIV for now we don't care
     *
     * @param attribute
     * @return
     */
    public static boolean acceptAttribute(Attribute attribute) {
        return (attribute instanceof RuntimeAnnotations);
    }

    /**
     * Extract class level annotations and turn them into AjAttributes.
     *
     * @param javaClass
     * @param type
     * @param context
     * @param msgHandler
     * @return list of AjAttributes
     */
    public static List readAj5ClassAttributes(JavaClass javaClass, ResolvedTypeX type, ISourceContext context,IMessageHandler msgHandler) {
        AjAttributeStruct struct = new AjAttributeStruct(type);
        Attribute[] attributes = javaClass.getAttributes();
        for (int i = 0; i < attributes.length; i++) {
            Attribute attribute = attributes[i];
            if (acceptAttribute(attribute)) {
                RuntimeAnnotations rvs = (RuntimeAnnotations) attribute;
                handleAspectAnnotation(rvs, struct);
                handlePrecedenceAnnotation(rvs, struct);
            }
        }

        // code style pointcuts are class attributes
        // we need to gather the @AJ pointcut right now and not at method level annotation extraction time
        // in order to be able to resolve the pointcut references later on
        //TODO loop over class super class
        //TODO can that be too slow ?
        for (int m = 0; m < javaClass.getMethods().length; m++) {
            Method method = javaClass.getMethods()[m];
            AjAttributeMethodStruct mstruct = new AjAttributeMethodStruct(method, type);
            Attribute[] mattributes = method.getAttributes();

            for (int i = 0; i < mattributes.length; i++) {
                Attribute mattribute = mattributes[i];
                if (acceptAttribute(mattribute)) {
                    RuntimeAnnotations mrvs = (RuntimeAnnotations) mattribute;
                    handlePointcutAnnotation(mrvs, mstruct);
                }
            }
            struct.ajAttributes.addAll(mstruct.ajAttributes);
        }

        return struct.ajAttributes;
    }

    /**
     * Extract method level annotations and turn them into AjAttributes.
     *
     * @param method
     * @param type
     * @param context
     * @param msgHandler
     * @return list of AjAttributes
     */
    public static List readAj5MethodAttributes(Method method, ResolvedTypeX type, ISourceContext context,IMessageHandler msgHandler) {
        AjAttributeMethodStruct struct = new AjAttributeMethodStruct(method, type);
        Attribute[] attributes = method.getAttributes();

        for (int i = 0; i < attributes.length; i++) {
            Attribute attribute = attributes[i];
            if (acceptAttribute(attribute)) {
                RuntimeAnnotations rvs = (RuntimeAnnotations) attribute;
                handleBeforeAnnotation(rvs, struct);
                handleAfterAnnotation(rvs, struct);
                handleAfterReturningAnnotation(rvs, struct);
                handleAfterThrowingAnnotation(rvs, struct);
                handleAroundAnnotation(rvs, struct);
            }
        }
        return struct.ajAttributes;
    }

    /**
     * Extract field level annotations and turn them into AjAttributes.
     *
     * @param field
     * @param type
     * @param context
     * @param msgHandler
     * @return list of AjAttributes, always empty for now
     */
    public static List readAj5FieldAttributes(Field field, ResolvedTypeX type, ISourceContext context,IMessageHandler msgHandler) {
        return EMPTY_LIST;
    }

    /**
     * Read @Aspect
     *
     * @param runtimeAnnotations
     * @param struct
     */
    private static void handleAspectAnnotation(RuntimeAnnotations runtimeAnnotations, AjAttributeStruct struct) {
        Annotation aspect = getAnnotation(runtimeAnnotations, "org.aspectj.lang.annotation.Aspect");
        if (aspect != null) {
            ElementNameValuePair aspectPerClause = getAnnotationElement(aspect, "value");
            if (aspectPerClause == null) {
                // defaults to singleton
                PerClause clause = new PerSingleton();
                clause.setLocation(struct.context, -1, -1);
                struct.ajAttributes.add(new AjAttribute.Aspect(clause));
            } else {
                String perX = aspectPerClause.getValue().stringifyValue();
                final PerClause clause;
                if (perX == null || perX.length()<=0) {
                     clause = new PerSingleton();
                } else {
                    clause = readPerClausePointcut(perX);
                }
                clause.setLocation(struct.context, -1, -1);
                struct.ajAttributes.add(new AjAttribute.Aspect(clause));
            }
        }
    }

    /**
     * Read a perClause
     *
     * @param perClause like "pertarget(.....)"
     * @return a PerClause instance
     */
    private static PerClause readPerClausePointcut(String perClause) {
        String pointcut;
        if (perClause.startsWith(PerClause.KindAnnotationPrefix.PERCFLOW.getName())) {
            pointcut = PerClause.KindAnnotationPrefix.PERCFLOW.extractPointcut(perClause);
            return new PerCflow(Pointcut.fromString(pointcut), false);
        } else if (perClause.startsWith(PerClause.KindAnnotationPrefix.PERCFLOWBELOW.getName())) {
            pointcut = PerClause.KindAnnotationPrefix.PERCFLOWBELOW.extractPointcut(perClause);
            return new PerCflow(Pointcut.fromString(pointcut), true);
        } else if (perClause.startsWith(PerClause.KindAnnotationPrefix.PERTARGET.getName())) {
            pointcut = PerClause.KindAnnotationPrefix.PERTARGET.extractPointcut(perClause);
            return new PerObject(Pointcut.fromString(pointcut), false);
        } else if (perClause.startsWith(PerClause.KindAnnotationPrefix.PERTHIS.getName())) {
            pointcut = PerClause.KindAnnotationPrefix.PERTHIS.extractPointcut(perClause);
            return new PerObject(Pointcut.fromString(pointcut), true);
        } else if (perClause.startsWith(PerClause.KindAnnotationPrefix.PERTYPEWITHIN.getName())) {
            pointcut = PerClause.KindAnnotationPrefix.PERTYPEWITHIN.extractPointcut(perClause);
            return new PerTypeWithin(new PatternParser(pointcut).parseTypePattern());
        }
        throw new RuntimeException("cannot read perclause " + perClause);
    }

    /**
     * Read @DeclarePrecedence
     *
     * @param runtimeAnnotations
     * @param struct
     */
    private static void handlePrecedenceAnnotation(RuntimeAnnotations runtimeAnnotations, AjAttributeStruct struct) {
        Annotation aspect = getAnnotation(runtimeAnnotations, "org.aspectj.lang.annotation.DeclarePrecedence");
        if (aspect != null) {
            ElementNameValuePair precedence = getAnnotationElement(aspect, "value");
            if (precedence != null) {
                String precedencePattern = precedence.getValue().stringifyValue();
                PatternParser parser = new PatternParser(precedencePattern);
                DeclarePrecedence ajPrecedence = parser.parseDominates();
                struct.ajAttributes.add(new AjAttribute.DeclareAttribute(ajPrecedence));
            }
        }
    }

    /**
     * Read @Before
     *
     * @param runtimeAnnotations
     * @param struct
     */
    private static void handleBeforeAnnotation(RuntimeAnnotations runtimeAnnotations, AjAttributeMethodStruct struct) {
        Annotation before = getAnnotation(runtimeAnnotations, "org.aspectj.lang.annotation.Before");
        if (before != null) {
            ElementNameValuePair beforeAdvice = getAnnotationElement(before, "value");
            if (beforeAdvice != null) {
                // this/target/args binding
                FormalBinding[] bindings = extractBindings(struct);
                IScope binding = new BindingScope(
                        struct.enclosingType,
                        bindings
                );

                // joinpoint, staticJoinpoint binding
                int extraArgument = extractExtraArgument(struct.method);

                Pointcut pc = Pointcut.fromString(beforeAdvice.getValue().stringifyValue()).resolve(binding);
                setIgnoreUnboundBindingNames(pc, bindings);

                struct.ajAttributes.add(new AjAttribute.AdviceAttribute(
                        AdviceKind.Before,
                        pc,
                        extraArgument,
                        -1,
                        -1,
                        struct.context
                        )
                );
            }
        }
    }

    /**
     * Read @After
     *
     * @param runtimeAnnotations
     * @param struct
     */
    private static void handleAfterAnnotation(RuntimeAnnotations runtimeAnnotations, AjAttributeMethodStruct struct) {
        Annotation after = getAnnotation(runtimeAnnotations, "org.aspectj.lang.annotation.After");
        if (after != null) {
            ElementNameValuePair afterAdvice = getAnnotationElement(after, "value");
            if (afterAdvice != null) {
                // this/target/args binding
                FormalBinding[] bindings = extractBindings(struct);
                IScope binding = new BindingScope(
                        struct.enclosingType,
                        bindings
                );

                // joinpoint, staticJoinpoint binding
                int extraArgument = extractExtraArgument(struct.method);

                Pointcut pc = Pointcut.fromString(afterAdvice.getValue().stringifyValue()).resolve(binding);
                setIgnoreUnboundBindingNames(pc, bindings);

                struct.ajAttributes.add(new AjAttribute.AdviceAttribute(
                        AdviceKind.After,
                        pc,
                        extraArgument,
                        -1,
                        -1,
                        struct.context
                        )
                );
            }
        }
    }

    /**
     * Read @AfterReturning
     *
     * @param runtimeAnnotations
     * @param struct
     */
    private static void handleAfterReturningAnnotation(RuntimeAnnotations runtimeAnnotations, AjAttributeMethodStruct struct) {
        Annotation after = getAnnotation(runtimeAnnotations, "org.aspectj.lang.annotation.AfterReturning");
        if (after != null) {
            ElementNameValuePair annValue = getAnnotationElement(after, "value");
            ElementNameValuePair annPointcut = getAnnotationElement(after, "pointcut");
            ElementNameValuePair annReturned = getAnnotationElement(after, "returning");

            // extract the pointcut and returned type/binding - do some checks
            String pointcut = null;
            String returned = null;
            if ((annValue!=null && annPointcut!=null) || (annValue==null && annPointcut==null)) {
                throw new RuntimeException("AfterReturning at most value or pointcut must be filled");
            }
            if (annValue != null) {
                pointcut = annValue.getValue().stringifyValue();
            } else {
                pointcut = annPointcut.getValue().stringifyValue();
            }
            if (isNullOrEmpty(pointcut)) {
                throw new RuntimeException("AfterReturning pointcut unspecified");
            }
            if (annReturned!=null) {
                returned = annReturned.getValue().stringifyValue();
                if (isNullOrEmpty(returned))
                    returned = null;
            }

            // this/target/args binding
            // exclude the return binding from the pointcut binding since it is an extraArg binding
            FormalBinding[] bindings = (returned==null?extractBindings(struct):extractBindings(struct, returned));
            IScope binding = new BindingScope(
                    struct.enclosingType,
                    bindings
            );

            // joinpoint, staticJoinpoint binding
            int extraArgument = extractExtraArgument(struct.method);

            // return binding
            if (returned != null) {
                extraArgument |= Advice.ExtraArgument;
            }

            Pointcut pc = Pointcut.fromString(pointcut).resolve(binding);
            setIgnoreUnboundBindingNames(pc, bindings);

            struct.ajAttributes.add(new AjAttribute.AdviceAttribute(
                    AdviceKind.AfterReturning,
                    pc,
                    extraArgument,
                    -1,
                    -1,
                    struct.context
                    )
            );
        }
    }

    /**
     * Read @AfterThrowing
     *
     * @param runtimeAnnotations
     * @param struct
     */
    private static void handleAfterThrowingAnnotation(RuntimeAnnotations runtimeAnnotations, AjAttributeMethodStruct struct) {
        Annotation after = getAnnotation(runtimeAnnotations, "org.aspectj.lang.annotation.AfterThrowing");
        if (after != null) {
            ElementNameValuePair annValue = getAnnotationElement(after, "value");
            ElementNameValuePair annPointcut = getAnnotationElement(after, "pointcut");
            ElementNameValuePair annThrowned = getAnnotationElement(after, "throwing");

            // extract the pointcut and throwned type/binding - do some checks
            String pointcut = null;
            String throwned = null;
            if ((annValue!=null && annPointcut!=null) || (annValue==null && annPointcut==null)) {
                throw new RuntimeException("AfterReturning at most value or pointcut must be filled");
            }
            if (annValue != null) {
                pointcut = annValue.getValue().stringifyValue();
            } else {
                pointcut = annPointcut.getValue().stringifyValue();
            }
            if (isNullOrEmpty(pointcut)) {
                throw new RuntimeException("AfterReturning pointcut unspecified");
            }
            if (annThrowned!=null) {
                throwned = annThrowned.getValue().stringifyValue();
                if (isNullOrEmpty(throwned))
                    throwned = null;
            }

            // this/target/args binding
            // exclude the throwned binding from the pointcut binding since it is an extraArg binding
            FormalBinding[] bindings = (throwned==null?extractBindings(struct):extractBindings(struct, throwned));
            IScope binding = new BindingScope(
                    struct.enclosingType,
                    bindings
            );

            // joinpoint, staticJoinpoint binding
            int extraArgument = extractExtraArgument(struct.method);

            // return binding
            if (throwned != null) {
                extraArgument |= Advice.ExtraArgument;
            }

            Pointcut pc = Pointcut.fromString(pointcut).resolve(binding);
            setIgnoreUnboundBindingNames(pc, bindings);

            struct.ajAttributes.add(new AjAttribute.AdviceAttribute(
                    AdviceKind.AfterThrowing,
                    pc,
                    extraArgument,
                    -1,
                    -1,
                    struct.context
                    )
            );
        }
    }

    /**
     * Read @Around
     *
     * @param runtimeAnnotations
     * @param struct
     */
    private static void handleAroundAnnotation(RuntimeAnnotations runtimeAnnotations, AjAttributeMethodStruct struct) {
        Annotation around = getAnnotation(runtimeAnnotations, "org.aspectj.lang.annotation.Around");
        if (around != null) {
            ElementNameValuePair aroundAdvice = getAnnotationElement(around, "value");
            if (aroundAdvice != null) {
                // this/target/args binding
                FormalBinding[] bindings = extractBindings(struct);
                IScope binding = new BindingScope(
                        struct.enclosingType,
                        bindings
                );

                // joinpoint, staticJoinpoint binding
                int extraArgument = extractExtraArgument(struct.method);

                Pointcut pc = Pointcut.fromString(aroundAdvice.getValue().stringifyValue()).resolve(binding);
                setIgnoreUnboundBindingNames(pc, bindings);

                struct.ajAttributes.add(new AjAttribute.AdviceAttribute(
                        AdviceKind.Around,
                        pc,
                        extraArgument,
                        -1,
                        -1,
                        struct.context
                        )
                );
            }
        }
    }

    /**
     * Read @Pointcut and handle the resolving in a lazy way to deal with pointcut references
     *
     * @param runtimeAnnotations
     * @param struct
     */
    private static void handlePointcutAnnotation(RuntimeAnnotations runtimeAnnotations, AjAttributeMethodStruct struct) {
        Annotation pointcut = getAnnotation(runtimeAnnotations, "org.aspectj.lang.annotation.Pointcut");
        if (pointcut != null) {
            ElementNameValuePair pointcutExpr = getAnnotationElement(pointcut, "value");
            if (pointcutExpr != null) {
                // this/target/args binding
                IScope binding = new BindingScope(
                        struct.enclosingType,
                        extractBindings(struct)
                );

                TypeX[] argumentTypes = new TypeX[struct.method.getArgumentTypes().length];
                for (int i = 0; i < argumentTypes.length; i++) {
                    argumentTypes[i] = TypeX.forSignature(struct.method.getArgumentTypes()[i].getSignature());
                }

                // use a LazyResolvedPointcutDefinition so that the pointcut is resolved lazily
                // since for it to be resolved, we will need other pointcuts to be registered as well
                struct.ajAttributes.add(new AjAttribute.PointcutDeclarationAttribute(
                        new LazyResolvedPointcutDefinition(
                                struct.enclosingType,
                                struct.method.getModifiers(),
                                struct.method.getName(),
                                argumentTypes,
                                Pointcut.fromString(pointcutExpr.getValue().stringifyValue()),
                                binding
                        )
                ));
            }
        }
    }

    /**
     * Build the bindings for a given method (pointcut / advice)
     *
     * @param struct
     * @return
     */
    private static FormalBinding[] extractBindings(AjAttributeMethodStruct struct) {
        Method method = struct.method;
        String[] argumentNames = struct.getArgumentNames();

        // assert debug info was here
        if (argumentNames.length != method.getArgumentTypes().length) {
            throw new RuntimeException("cannot access debug info " + method);
        }

        List bindings = new ArrayList();
        for (int i = 0; i < argumentNames.length; i++) {
            String argumentName = argumentNames[i];
            TypeX argumentType = TypeX.forSignature(method.getArgumentTypes()[i].getSignature());

            // do not bind JoinPoint / StaticJoinPoint / EnclosingStaticJoinPoint
            // TODO solve me : this means that the JP/SJP/ESJP cannot appear as binding
            // f.e. when applying advice on advice etc
            if ((TYPEX_JOINPOINT.equals(argumentType)
                || TYPEX_PROCEEDINGJOINPOINT.equals(argumentType)
                || TYPEX_STATICJOINPOINT.equals(argumentType)
                || TYPEX_ENCLOSINGSTATICJOINPOINT.equals(argumentType)
                || AjcMemberMaker.AROUND_CLOSURE_TYPE.equals(argumentType))) {
                //continue;// skip
                bindings.add(new FormalBinding.ImplicitFormalBinding(argumentType, argumentName, i));
            } else {
                bindings.add(new FormalBinding(argumentType, argumentName, i));
            }
        }

        return (FormalBinding[]) bindings.toArray(new FormalBinding[]{});
    }

    //FIXME deal with exclude index
    private static FormalBinding[] extractBindings(AjAttributeMethodStruct struct, String excludeFormal) {
        FormalBinding[] bindings = extractBindings(struct);
        int excludeIndex = -1;
        for (int i = 0; i < bindings.length; i++) {
            FormalBinding binding = bindings[i];
            if (binding.getName().equals(excludeFormal)) {
                excludeIndex = i;
                bindings[i] = new FormalBinding.ImplicitFormalBinding(binding.getType(), binding.getName(), binding.getIndex());
                break;
            }
        }
        return bindings;
//
//        if (excludeIndex >= 0) {
//            FormalBinding[] bindingsFiltered = new FormalBinding[bindings.length-1];
//            int k = 0;
//            for (int i = 0; i < bindings.length; i++) {
//                if (i == excludeIndex) {
//                    ;
//                } else {
//                    bindingsFiltered[k] = new FormalBinding(bindings[i].getType(), bindings[i].getName(), k);
//                    k++;
//                }
//            }
//            return bindingsFiltered;
//        } else {
//            return bindings;
//        }
    }


    /**
     * Compute the flag for the xxxJoinPoint extra argument
     *
     * @param method
     * @return
     */
    private static int extractExtraArgument(Method method) {
        int extraArgument = 0;
        Type[] methodArgs = method.getArgumentTypes();
        for (int i = 0; i < methodArgs.length; i++) {
            String methodArg = methodArgs[i].getSignature();
            if (TYPEX_JOINPOINT.getSignature().equals(methodArg)) {
                extraArgument |= Advice.ThisJoinPoint;
            } else if (TYPEX_PROCEEDINGJOINPOINT.getSignature().equals(methodArg)) {
                extraArgument |= Advice.ThisJoinPoint;
            } else if (TYPEX_STATICJOINPOINT.getSignature().equals(methodArg)) {
                extraArgument |= Advice.ThisJoinPointStaticPart;
            } else if (TYPEX_ENCLOSINGSTATICJOINPOINT.getSignature().equals(methodArg)) {
                extraArgument |= Advice.ThisEnclosingJoinPointStaticPart;
            }
        }
        return extraArgument;
    }

    /**
     * Returns the runtime (RV/RIV) annotation of type annotationType or null if no such annotation
     *
     * @param rvs
     * @param annotationType
     * @return
     */
    private static Annotation getAnnotation(RuntimeAnnotations rvs, String annotationType) {
        for (Iterator iterator = rvs.getAnnotations().iterator(); iterator.hasNext();) {
            Annotation rv = (Annotation) iterator.next();
            if (annotationType.equals(rv.getTypeName())) {
                return rv;
            }
        }
        return null;
    }

    /**
     * Returns the value of a given element of an annotation or null if not found
     * Does not handles default value
     *
     * @param annotation
     * @param elementName
     * @return
     */
    private static ElementNameValuePair getAnnotationElement(Annotation annotation, String elementName) {
        //TODO does not handles default values which are annotation of elements in the annotation class
        for (Iterator iterator1 = annotation.getValues().iterator(); iterator1.hasNext();) {
            ElementNameValuePair element = (ElementNameValuePair) iterator1.next();
            if (elementName.equals(element.getNameString())) {
                return element;
            }
        }
        return null;
    }

    /**
     * Extract the method argument names as in source from debug info
     * returns an empty array upon inconsistency
     *
     * @param method
     * @return
     */
    private static String[] getMethodArgumentNamesAsInSource(Method method) {
        if (method.getArgumentTypes().length == 0) {
            return EMPTY_STRINGS;
        }

        final int startAtStackIndex = method.isStatic()?0:1;
        final List arguments = new ArrayList();
        LocalVariableTable lt = (LocalVariableTable) method.getLocalVariableTable();
        if (lt != null) {
            for (int j = 0; j < lt.getLocalVariableTable().length; j++) {
                LocalVariable localVariable = lt.getLocalVariableTable()[j];
                if (localVariable.getStartPC() == 0) {
                    if (localVariable.getIndex() >= startAtStackIndex) {
                        arguments.add(new MethodArgument(localVariable.getName(), localVariable.getIndex()));
                    }
                }
            }
        }

        if (arguments.size() != method.getArgumentTypes().length) {
            //throw new RuntimeException("cannot access debug info on " + method);
            return EMPTY_STRINGS;
        }

        // sort by index
        Collections.sort(arguments, new Comparator() {
            public int compare(Object o, Object o1) {
                MethodArgument mo = (MethodArgument)o;
                MethodArgument mo1 = (MethodArgument) o1;
                if (mo.indexOnStack == mo1.indexOnStack) {
                    return 0;
                } else if (mo.indexOnStack > mo1.indexOnStack) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });
        String[] argumentNames = new String[arguments.size()];
        int i = 0;
        for (Iterator iterator = arguments.iterator(); iterator.hasNext(); i++) {
            MethodArgument methodArgument = (MethodArgument) iterator.next();
            argumentNames[i] = methodArgument.name;
        }
        return argumentNames;
    }

    /**
     * A method argument, used for sorting by indexOnStack (ie order in signature)
     *
     * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
     */
    private static class MethodArgument {
        String name;
        int indexOnStack;
        public MethodArgument(String name, int indexOnStack) {
            this.name = name;
            this.indexOnStack = indexOnStack;
        }
    }

    /**
     * BindingScope that knows the enclosingType, which is needed for pointcut reference resolution
     *
     * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
     */
    public static class BindingScope extends SimpleScope {
        private ResolvedTypeX m_enclosingType;

        public BindingScope(ResolvedTypeX type, FormalBinding[] bindings) {
            super(type.getWorld(), bindings);
            m_enclosingType = type;
        }

        public ResolvedTypeX getEnclosingType() {
            return m_enclosingType;
        }
    }

    /**
     * LazyResolvedPointcutDefinition lazyly resolve the pointcut so that we have time to register all
     * pointcut referenced before pointcut resolution happens
     *
     * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
     */
    public static class LazyResolvedPointcutDefinition extends ResolvedPointcutDefinition {
        private Pointcut m_pointcutUnresolved;
        private IScope m_binding;

        private Pointcut m_lazyPointcut = null;

        public LazyResolvedPointcutDefinition(TypeX declaringType, int modifiers, String name, TypeX[] parameterTypes,
                                              Pointcut pointcut, IScope binding) {
            super(declaringType, modifiers, name, parameterTypes, null);
            m_pointcutUnresolved = pointcut;
            m_binding = binding;
        }

        public Pointcut getPointcut() {
            if (m_lazyPointcut == null) {
                m_lazyPointcut = m_pointcutUnresolved.resolve(m_binding);
            }
            return m_lazyPointcut;
        }
    }

    /**
     * Helper to test empty strings
     * @param s
     * @return
     */
    private static boolean isNullOrEmpty(String s) {
        return (s==null || s.length()<=0);
    }

    /**
     * Set the pointcut bindings for which to ignore unbound issues, so that we can implicitly bind
     * xxxJoinPoint for @AJ advices
     *
     * @param pointcut
     * @param bindings
     */
    private static void setIgnoreUnboundBindingNames(Pointcut pointcut, FormalBinding[] bindings) {
        // register ImplicitBindings as to be ignored since unbound
        // TODO is it likely to fail in a bad way if f.e. this(jp) etc ?
        List ignores = new ArrayList();
        for (int i = 0; i < bindings.length; i++) {
            FormalBinding formalBinding = bindings[i];
            if (formalBinding instanceof FormalBinding.ImplicitFormalBinding) {
                ignores.add(formalBinding.getName());
            }
        }
        pointcut.m_ignoreUnboundBindingForNames = (String[])ignores.toArray(new String[ignores.size()]);
    }
}
