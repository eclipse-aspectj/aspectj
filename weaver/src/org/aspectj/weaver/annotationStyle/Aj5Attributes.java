/*******************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/
package org.aspectj.weaver.annotationStyle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.aspectj.apache.bcel.classfile.Attribute;
import org.aspectj.apache.bcel.classfile.Field;
import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.classfile.LocalVariable;
import org.aspectj.apache.bcel.classfile.LocalVariableTable;
import org.aspectj.apache.bcel.classfile.Method;
import org.aspectj.apache.bcel.classfile.annotation.Annotation;
import org.aspectj.apache.bcel.classfile.annotation.ArrayElementValue;
import org.aspectj.apache.bcel.classfile.annotation.ClassElementValue;
import org.aspectj.apache.bcel.classfile.annotation.ElementNameValuePair;
import org.aspectj.apache.bcel.classfile.annotation.RuntimeAnnotations;
import org.aspectj.apache.bcel.generic.Type;
import org.aspectj.bridge.IMessageHandler;
import org.aspectj.lang.JoinPoint;
import org.aspectj.weaver.Advice;
import org.aspectj.weaver.AdviceKind;
import org.aspectj.weaver.AjAttribute;
import org.aspectj.weaver.AjcMemberMaker;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.ResolvedPointcutDefinition;
import org.aspectj.weaver.ResolvedTypeX;
import org.aspectj.weaver.TypeX;
import org.aspectj.weaver.patterns.DeclarePrecedence;
import org.aspectj.weaver.patterns.FormalBinding;
import org.aspectj.weaver.patterns.IScope;
import org.aspectj.weaver.patterns.PatternParser;
import org.aspectj.weaver.patterns.PerClause;
import org.aspectj.weaver.patterns.PerSingleton;
import org.aspectj.weaver.patterns.Pointcut;
import org.aspectj.weaver.patterns.SimpleScope;

/**
 * Annotation defined aspect reader.
 * It reads the Java 5 annotations and turns them into AjAttributes
 *
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class Aj5Attributes {

    private final static List EMPTY_LIST = new ArrayList();
    private final static String[] EMPTY_STRINGS = new String[0];

    private final static TypeX TYPEX_JOINPOINT = TypeX.forName(JoinPoint.class.getName().replace('/','.'));
    private final static TypeX TYPEX_STATICJOINPOINT = TypeX.forName(JoinPoint.StaticPart.class.getName().replace('/','.'));
    private final static TypeX TYPEX_ENCLOSINGSTATICJOINPOINT = TypeX.forName(JoinPoint.EnclosingStaticPart.class.getName().replace('/','.'));

    /**
     * A struct that allows to add extra arguments without always breaking the API
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
        return (attribute instanceof RuntimeAnnotations);//TODO RV or RIV for @AJ
    }

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

    public static List readAj5MethodAttributes(Method method, ResolvedTypeX type, ISourceContext context,IMessageHandler msgHandler) {
    	// ALEX Comment from Andy. Can we speed this up?? I don't want to impact normal compilation ...
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

    public static List readAj5FieldAttributes(Field field, ResolvedTypeX type, ISourceContext context,IMessageHandler msgHandler) {
        return EMPTY_LIST;
    }

    private static void handleAspectAnnotation(RuntimeAnnotations runtimeAnnotations, AjAttributeStruct struct) {
        Annotation aspect = getAnnotation(runtimeAnnotations, "org.aspectj.lang.annotation.Aspect");
        if (aspect != null) {
            ElementNameValuePair aspectPerClause = getAnnotationElement(aspect, "value");
            if (aspectPerClause != null) {
                if (PerClause.SINGLETON.getName().equals(aspectPerClause.getValue().stringifyValue())) {
                    PerClause clause = new PerSingleton();
                    clause.setLocation(struct.context, -1, -1);
                    struct.ajAttributes.add(new AjAttribute.Aspect(clause));
                }
            }
        }
    }

    private static void handlePrecedenceAnnotation(RuntimeAnnotations runtimeAnnotations, AjAttributeStruct struct) {
        Annotation aspect = getAnnotation(runtimeAnnotations, "org.aspectj.lang.annotation.DeclarePrecedence");
        if (aspect != null) {
            ElementNameValuePair precedence = getAnnotationElement(aspect, "value");
            if (precedence != null) {
                //we have class[] element type
                StringBuffer precedencePattern = new StringBuffer();
                ArrayElementValue precedenceValue = (ArrayElementValue) precedence.getValue();
                for (int i = 0; i < precedenceValue.getElementValuesArray().length; i++) {
                    ClassElementValue value = (ClassElementValue) precedenceValue.getElementValuesArray()[i];
                    if (i > 0) {
                        precedencePattern.append(',');
                    }
                    String signature = value.stringifyValue();
                    if ("Lorg/aspectj/lang/annotation/DeclarePrecedence$ANY;".equals(signature)) {
                        precedencePattern.append('*');
                    } else {
                        precedencePattern.append(TypeX.forSignature(signature).getClassName());
                    }
                }
                PatternParser parser = new PatternParser(precedencePattern.toString());
                DeclarePrecedence ajPrecedence = parser.parseDominates();
                struct.ajAttributes.add(new AjAttribute.DeclareAttribute(ajPrecedence));
            }
        }
    }

    private static void handleBeforeAnnotation(RuntimeAnnotations runtimeAnnotations, AjAttributeMethodStruct struct) {
        Annotation before = getAnnotation(runtimeAnnotations, "org.aspectj.lang.annotation.Before");
        if (before != null) {
            ElementNameValuePair beforeAdvice = getAnnotationElement(before, "value");
            if (beforeAdvice != null) {
                // this/target/args binding
                IScope binding = new BindingScope(
                        struct.enclosingType,
                        extractBindings(struct)
                );

                // joinpoint, staticJoinpoint binding
                int extraArgument = extractExtraArgument(struct.method);

                struct.ajAttributes.add(new AjAttribute.AdviceAttribute(
                        AdviceKind.Before,
                        Pointcut.fromString(beforeAdvice.getValue().stringifyValue()).resolve(binding),
                        extraArgument,
                        -1,
                        -1,
                        struct.context
                        )
                );
            }
        }
    }

    private static void handleAfterAnnotation(RuntimeAnnotations runtimeAnnotations, AjAttributeMethodStruct struct) {
        Annotation after = getAnnotation(runtimeAnnotations, "org.aspectj.lang.annotation.After");
        if (after != null) {
            ElementNameValuePair afterAdvice = getAnnotationElement(after, "value");
            if (afterAdvice != null) {
                // this/target/args binding
                IScope binding = new BindingScope(
                        struct.enclosingType,
                        extractBindings(struct)
                );

                // joinpoint, staticJoinpoint binding
                int extraArgument = extractExtraArgument(struct.method);

                struct.ajAttributes.add(new AjAttribute.AdviceAttribute(
                        AdviceKind.After,
                        Pointcut.fromString(afterAdvice.getValue().stringifyValue()).resolve(binding),
                        extraArgument,
                        -1,
                        -1,
                        struct.context
                        )
                );
            }
        }
    }

    private static void handleAfterReturningAnnotation(RuntimeAnnotations runtimeAnnotations, AjAttributeMethodStruct struct) {
        Annotation after = getAnnotation(runtimeAnnotations, "org.aspectj.lang.annotation.AfterReturning");
        if (after != null) {
            ElementNameValuePair annValue = getAnnotationElement(after, "value");
            ElementNameValuePair annPointcut = getAnnotationElement(after, "pointcut");
            ElementNameValuePair annReturned = getAnnotationElement(after, "returned");

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
            IScope binding = new BindingScope(
                    struct.enclosingType,
                    (returned==null?extractBindings(struct):extractBindings(struct, returned))
            );

            // joinpoint, staticJoinpoint binding
            int extraArgument = extractExtraArgument(struct.method);

            // return binding
            if (returned != null) {
                extraArgument |= Advice.ExtraArgument;
            }

            struct.ajAttributes.add(new AjAttribute.AdviceAttribute(
                    AdviceKind.AfterReturning,
                    Pointcut.fromString(pointcut).resolve(binding),
                    extraArgument,
                    -1,
                    -1,
                    struct.context
                    )
            );
        }
    }

    private static void handleAfterThrowingAnnotation(RuntimeAnnotations runtimeAnnotations, AjAttributeMethodStruct struct) {
        Annotation after = getAnnotation(runtimeAnnotations, "org.aspectj.lang.annotation.AfterThrowing");
        if (after != null) {
            ElementNameValuePair annValue = getAnnotationElement(after, "value");
            ElementNameValuePair annPointcut = getAnnotationElement(after, "pointcut");
            ElementNameValuePair annThrowned = getAnnotationElement(after, "throwned");

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
            IScope binding = new BindingScope(
                    struct.enclosingType,
                    (throwned==null?extractBindings(struct):extractBindings(struct, throwned))
            );

            // joinpoint, staticJoinpoint binding
            int extraArgument = extractExtraArgument(struct.method);

            // return binding
            if (throwned != null) {
                extraArgument |= Advice.ExtraArgument;
            }

            struct.ajAttributes.add(new AjAttribute.AdviceAttribute(
                    AdviceKind.AfterThrowing,
                    Pointcut.fromString(pointcut).resolve(binding),
                    extraArgument,
                    -1,
                    -1,
                    struct.context
                    )
            );
        }
    }

    private static void handleAroundAnnotation(RuntimeAnnotations runtimeAnnotations, AjAttributeMethodStruct struct) {
        Annotation around = getAnnotation(runtimeAnnotations, "org.aspectj.lang.annotation.Around");
        if (around != null) {
            ElementNameValuePair aroundAdvice = getAnnotationElement(around, "value");
            if (aroundAdvice != null) {
                // this/target/args binding
                IScope binding = new BindingScope(
                        struct.enclosingType,
                        extractBindings(struct)
                );

                // joinpoint, staticJoinpoint binding
                int extraArgument = extractExtraArgument(struct.method);

                struct.ajAttributes.add(new AjAttribute.AdviceAttribute(
                        AdviceKind.Around,
                        Pointcut.fromString(aroundAdvice.getValue().stringifyValue()).resolve(binding),
                        extraArgument,
                        -1,
                        -1,
                        struct.context
                        )
                );
            }
        }
    }

    private static void handlePointcutAnnotation(RuntimeAnnotations runtimeAnnotations, AjAttributeMethodStruct struct) {
    	// ALEX Comment from Andy - those annotation names should be constants somewhere else Aj5MemberMaker?
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
                || TYPEX_STATICJOINPOINT.equals(argumentType)
                || TYPEX_ENCLOSINGSTATICJOINPOINT.equals(argumentType)
                || AjcMemberMaker.AROUND_CLOSURE_TYPE.equals(argumentType))) {
                continue;// skip
            } else {
                bindings.add(new FormalBinding(argumentType, argumentName, i));
            }
        }

        return (FormalBinding[]) bindings.toArray(new FormalBinding[]{});
    }

    private static FormalBinding[] extractBindings(AjAttributeMethodStruct struct, String excludeFormal) {
        FormalBinding[] bindings = extractBindings(struct);
        int excludeIndex = -1;
        for (int i = 0; i < bindings.length; i++) {
            FormalBinding binding = bindings[i];
            if (binding.getName().equals(excludeFormal)) {
                excludeIndex = i;
                break;
            }
        }

        if (excludeIndex >= 0) {
            FormalBinding[] bindingsFiltered = new FormalBinding[bindings.length-1];
            int k = 0;
            for (int i = 0; i < bindings.length; i++) {
                if (i == excludeIndex) {
                    ;
                } else {
                    bindingsFiltered[k] = new FormalBinding(bindings[i].getType(), bindings[i].getName(), k);
                    k++;
                }
            }
            return bindingsFiltered;
        } else {
            return bindings;
        }
    }


    /**
     * Compute the flag for the JoinPoint / StaticJoinPoint extra argument
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
     * BindingScope knows the enclosingType, which is needed for pointcut reference resolution
     */
    static class BindingScope extends SimpleScope {
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
     * pointcut referenced
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

    private static boolean isNullOrEmpty(String s) {
        return (s==null || s.length()<=0);
    }
}
