/*******************************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * initial implementation              Alexandre Vasseur
 *******************************************************************************/
package org.aspectj.weaver.bcel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.aspectj.apache.bcel.Constants;
import org.aspectj.apache.bcel.classfile.Attribute;
import org.aspectj.apache.bcel.classfile.Constant;
import org.aspectj.apache.bcel.classfile.ConstantUtf8;
import org.aspectj.apache.bcel.classfile.Field;
import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.classfile.LocalVariable;
import org.aspectj.apache.bcel.classfile.LocalVariableTable;
import org.aspectj.apache.bcel.classfile.Method;
import org.aspectj.apache.bcel.classfile.annotation.Annotation;
import org.aspectj.apache.bcel.classfile.annotation.ClassElementValue;
import org.aspectj.apache.bcel.classfile.annotation.ElementNameValuePair;
import org.aspectj.apache.bcel.classfile.annotation.RuntimeAnnotations;
import org.aspectj.apache.bcel.classfile.annotation.RuntimeVisibleAnnotations;
import org.aspectj.apache.bcel.generic.Type;
import org.aspectj.asm.AsmManager;
import org.aspectj.asm.IHierarchy;
import org.aspectj.asm.IProgramElement;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessageHandler;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.bridge.Message;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.weaver.Advice;
import org.aspectj.weaver.AdviceKind;
import org.aspectj.weaver.AjAttribute;
import org.aspectj.weaver.AjcMemberMaker;
import org.aspectj.weaver.IHasPosition;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.MethodDelegateTypeMunger;
import org.aspectj.weaver.NameMangler;
import org.aspectj.weaver.ReferenceType;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedPointcutDefinition;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.WeaverMessages;
import org.aspectj.weaver.patterns.Bindings;
import org.aspectj.weaver.patterns.DeclareErrorOrWarning;
import org.aspectj.weaver.patterns.DeclareParents;
import org.aspectj.weaver.patterns.DeclarePrecedence;
import org.aspectj.weaver.patterns.FormalBinding;
import org.aspectj.weaver.patterns.IScope;
import org.aspectj.weaver.patterns.ParserException;
import org.aspectj.weaver.patterns.PatternParser;
import org.aspectj.weaver.patterns.PerCflow;
import org.aspectj.weaver.patterns.PerClause;
import org.aspectj.weaver.patterns.PerFromSuper;
import org.aspectj.weaver.patterns.PerObject;
import org.aspectj.weaver.patterns.PerSingleton;
import org.aspectj.weaver.patterns.PerTypeWithin;
import org.aspectj.weaver.patterns.Pointcut;
import org.aspectj.weaver.patterns.SimpleScope;
import org.aspectj.weaver.patterns.TypePattern;

/**
 * Annotation defined aspect reader.
 * <p/>
 * It reads the Java 5 annotations and turns them into AjAttributes
 *
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class AtAjAttributes {

    private final static List EMPTY_LIST = new ArrayList();
    private final static String[] EMPTY_STRINGS = new String[0];
    private final static String VALUE = "value";
    private final static String ARGNAMES = "argNames";
    private final static String POINTCUT = "pointcut";
    private final static String THROWING = "throwing";
    private final static String RETURNING = "returning";
    private final static String STRING_DESC = "Ljava/lang/String;";

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
        final ResolvedType enclosingType;

        final ISourceContext context;
        final IMessageHandler handler;

        public AjAttributeStruct(ResolvedType type, ISourceContext sourceContext, IMessageHandler messageHandler) {
            enclosingType = type;
            context = sourceContext;
            handler = messageHandler;
        }
    }

    /**
     * A struct when we read @AJ on method
     *
     * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
     */
    private static class AjAttributeMethodStruct extends AjAttributeStruct {

    	// argument names used for formal binding
        private String[] m_argumentNamesLazy = null;
        public String unparsedArgumentNames = null; // Set only if discovered as argNames attribute of annotation

        final Method method;
        final BcelMethod bMethod;

        public AjAttributeMethodStruct(Method method, BcelMethod bMethod, ResolvedType type, ISourceContext sourceContext, IMessageHandler messageHandler) {
            super(type, sourceContext, messageHandler);
            this.method = method;
            this.bMethod = bMethod;
        }

        public String[] getArgumentNames() {
            if (m_argumentNamesLazy == null) {
                m_argumentNamesLazy = getMethodArgumentNames(method,unparsedArgumentNames,this);
            }
            return m_argumentNamesLazy;
        }
    }

    /**
     * A struct when we read @AJ on field
     *
     * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
     */
    private static class AjAttributeFieldStruct extends AjAttributeStruct {

        final Field field;
        final BcelField bField;

        public AjAttributeFieldStruct(Field field, BcelField bField, ResolvedType type, ISourceContext sourceContext, IMessageHandler messageHandler) {
            super(type, sourceContext, messageHandler);
            this.field = field;
            this.bField = bField;
        }
    }

    /**
     * Annotations are RuntimeVisible only. This allow us to not visit RuntimeInvisible ones.
     *
     * @param attribute
     * @return true if runtime visible annotation
     */
    public static boolean acceptAttribute(Attribute attribute) {
        return (attribute instanceof RuntimeVisibleAnnotations);
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
    public static List readAj5ClassAttributes(JavaClass javaClass, ReferenceType type, ISourceContext context, IMessageHandler msgHandler, boolean isCodeStyleAspect) {
    	boolean containsPointcut = false;
        //FIXME AV - 1.5 feature limitation, kick after implemented
    	boolean ignoreThisClass = javaClass.getClassName().charAt(0)=='o' && javaClass.getClassName().startsWith("org.aspectj.lang.annotation");
    	if (ignoreThisClass) return EMPTY_LIST;
    	try {
    		boolean containsAnnotationClassReference = false;
            Constant[] cpool = javaClass.getConstantPool().getConstantPool();
            for (int i = 0; i < cpool.length; i++) {
                Constant constant = cpool[i];
                if (constant != null && constant.getTag() == Constants.CONSTANT_Utf8) {
                    String constantValue = ((ConstantUtf8)constant).getBytes();
                    if (constantValue.length()>28 && constantValue.charAt(1)=='o') {
                    	if (constantValue.startsWith("Lorg/aspectj/lang/annotation")) {
                    		containsAnnotationClassReference=true;
                    		if ("Lorg/aspectj/lang/annotation/DeclareAnnotation;".equals(constantValue)) {
	                            msgHandler.handleMessage(
	                                    new Message(
	                                            "Found @DeclareAnnotation while current release does not support it (see '" + type.getName() + "')",
	                                            IMessage.WARNING,
	                                            null,
	                                            type.getSourceLocation()
	                                    )
	                            );
	                        }
	                        if ("Lorg/aspectj/lang/annotation/Pointcut;".equals(constantValue)) {
	                        	containsPointcut=true;
	                        }
                    	}
                    }
                }
            }
            if (!containsAnnotationClassReference) return EMPTY_LIST;
        } catch (Throwable t) {
            ;
        }


        AjAttributeStruct struct = new AjAttributeStruct(type, context, msgHandler);
        Attribute[] attributes = javaClass.getAttributes();
        boolean hasAtAspectAnnotation = false;
        boolean hasAtPrecedenceAnnotation = false;

        for (int i = 0; i < attributes.length; i++) {
            Attribute attribute = attributes[i];
            if (acceptAttribute(attribute)) {
                RuntimeAnnotations rvs = (RuntimeAnnotations) attribute;
                // we don't need to look for several attribute occurence since it cannot happen as per JSR175
                if (!isCodeStyleAspect && !javaClass.isInterface()) {
                    hasAtAspectAnnotation = handleAspectAnnotation(rvs, struct);
                    //TODO AV - if put outside the if isCodeStyleAspect then we would enable mix style
                    hasAtPrecedenceAnnotation = handlePrecedenceAnnotation(rvs, struct);
                }
                // there can only be one RuntimeVisible bytecode attribute
                break;
            }
        }

        // basic semantic check
        if (hasAtPrecedenceAnnotation && !hasAtAspectAnnotation) {
            msgHandler.handleMessage(
                    new Message(
                            "Found @DeclarePrecedence on a non @Aspect type '" + type.getName() + "'",
                            IMessage.WARNING,
                            null,
                            type.getSourceLocation()
                    )
            );
            // bypass what we have read
            return EMPTY_LIST;
        }

        // the following block will not detect @Pointcut in non @Aspect types for optimization purpose
        if (!hasAtAspectAnnotation && !containsPointcut) {
            return EMPTY_LIST;
        }

        //FIXME AV - turn on when ajcMightHaveAspect
//        if (hasAtAspectAnnotation && type.isInterface()) {
//            msgHandler.handleMessage(
//                    new Message(
//                            "Found @Aspect on an interface type '" + type.getName() + "'",
//                            IMessage.WARNING,
//                            null,
//                            type.getSourceLocation()
//                    )
//            );
//            // bypass what we have read
//            return EMPTY_LIST;
//        }

        // semantic check: @Aspect must be public
        // FIXME AV - do we really want to enforce that?
//        if (hasAtAspectAnnotation && !javaClass.isPublic()) {
//            msgHandler.handleMessage(
//                    new Message(
//                            "Found @Aspect annotation on a non public class '" + javaClass.getClassName() + "'",
//                            IMessage.ERROR,
//                            null,
//                            type.getSourceLocation()
//                    )
//            );
//            return EMPTY_LIST;
//        }

        // code style pointcuts are class attributes
        // we need to gather the @AJ pointcut right now and not at method level annotation extraction time
        // in order to be able to resolve the pointcut references later on
        // we don't need to look in super class, the pointcut reference in the grammar will do it
        
        for (int i = 0; i < javaClass.getMethods().length; i++) {
            Method method = javaClass.getMethods()[i];
            if (method.getName().startsWith(NameMangler.PREFIX)) continue;  // already dealt with by ajc...
            //FIXME alex optimize, this method struct will gets recreated for advice extraction
            AjAttributeMethodStruct mstruct = null;
            boolean processedPointcut = false;
            Attribute[] mattributes = method.getAttributes();
            for (int j = 0; j < mattributes.length; j++) {
                Attribute mattribute = mattributes[j];
                if (acceptAttribute(mattribute)) {
                	if (mstruct==null) mstruct = new AjAttributeMethodStruct(method, null, type, context, msgHandler);//FIXME AVASM
                    processedPointcut = handlePointcutAnnotation((RuntimeAnnotations) mattribute, mstruct);
                    // there can only be one RuntimeVisible bytecode attribute
                    break;
                }
            }
            if (processedPointcut) {
	            // FIXME asc should check we aren't adding multiple versions... will do once I get the tests passing again...
	            struct.ajAttributes.add(new AjAttribute.WeaverVersionInfo());
	            struct.ajAttributes.addAll(mstruct.ajAttributes);
            }
        }


        // code style declare error / warning / implements / parents are field attributes
        for (int i = 0; i < javaClass.getFields().length; i++) {
            Field field = javaClass.getFields()[i];
            if (field.getName().startsWith(NameMangler.PREFIX)) continue;  // already dealt with by ajc...
            //FIXME alex optimize, this method struct will gets recreated for advice extraction
            AjAttributeFieldStruct fstruct = new AjAttributeFieldStruct(field, null, type, context, msgHandler);
            Attribute[] fattributes = field.getAttributes();

            for (int j = 0; j < fattributes.length; j++) {
                Attribute fattribute = fattributes[j];
                if (acceptAttribute(fattribute)) {
                    RuntimeAnnotations frvs = (RuntimeAnnotations) fattribute;
                    if (handleDeclareErrorOrWarningAnnotation(frvs, fstruct)
                            || handleDeclareParentsAnnotation(frvs, fstruct)) {
                        // semantic check - must be in an @Aspect [remove if previous block bypassed in advance]
                        if (!type.isAnnotationStyleAspect()) {
                            msgHandler.handleMessage(
                                    new Message(
                                            "Found @AspectJ annotations in a non @Aspect type '" + type.getName() + "'",
                                            IMessage.WARNING,
                                            null,
                                            type.getSourceLocation()
                                    )
                            );
                            ;// go ahead
                        }
                    }
                    // there can only be one RuntimeVisible bytecode attribute
                    break;
                }
            }
            struct.ajAttributes.addAll(fstruct.ajAttributes);
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
    public static List readAj5MethodAttributes(Method method, BcelMethod bMethod, ResolvedType type, ResolvedPointcutDefinition preResolvedPointcut, ISourceContext context, IMessageHandler msgHandler) {
        if (method.getName().startsWith(NameMangler.PREFIX)) return Collections.EMPTY_LIST;  // already dealt with by ajc...

        AjAttributeMethodStruct struct = new AjAttributeMethodStruct(method, bMethod, type, context, msgHandler);
        Attribute[] attributes = method.getAttributes();

        // we remember if we found one @AJ annotation for minimal semantic error reporting
        // the real reporting beeing done thru AJDT and the compiler mapping @AJ to AjAtttribute
        // or thru APT
        //
        // Note: we could actually skip the whole thing if type is not itself an @Aspect
        // but then we would not see any warning. We do bypass for pointcut but not for advice since it would
        // be too silent.
        boolean hasAtAspectJAnnotation = false;
        boolean hasAtAspectJAnnotationMustReturnVoid = false;
        for (int i = 0; i < attributes.length; i++) {
            Attribute attribute = attributes[i];
            try {
                if (acceptAttribute(attribute)) {
                    RuntimeAnnotations rvs = (RuntimeAnnotations) attribute;
                    hasAtAspectJAnnotationMustReturnVoid = hasAtAspectJAnnotationMustReturnVoid || handleBeforeAnnotation(
                            rvs, struct, preResolvedPointcut
                    );
                    hasAtAspectJAnnotationMustReturnVoid = hasAtAspectJAnnotationMustReturnVoid || handleAfterAnnotation(
                            rvs, struct, preResolvedPointcut
                    );
                    hasAtAspectJAnnotationMustReturnVoid = hasAtAspectJAnnotationMustReturnVoid || handleAfterReturningAnnotation(
                            rvs, struct, preResolvedPointcut, bMethod
                    );
                    hasAtAspectJAnnotationMustReturnVoid = hasAtAspectJAnnotationMustReturnVoid || handleAfterThrowingAnnotation(
                            rvs, struct, preResolvedPointcut, bMethod
                    );
                    hasAtAspectJAnnotation = hasAtAspectJAnnotation || handleAroundAnnotation(
                            rvs, struct, preResolvedPointcut
                    );
                    // there can only be one RuntimeVisible bytecode attribute
                    break;
                }
            } catch (ReturningFormalNotDeclaredInAdviceSignatureException e) {
                msgHandler.handleMessage(
                    new Message(
                            WeaverMessages.format(WeaverMessages.RETURNING_FORMAL_NOT_DECLARED_IN_ADVICE,e.getFormalName()),
                            IMessage.ERROR,
                            null,
                            bMethod.getSourceLocation())
                );
            } catch (ThrownFormalNotDeclaredInAdviceSignatureException e) {
                msgHandler.handleMessage(
                        new Message(
                                WeaverMessages.format(WeaverMessages.THROWN_FORMAL_NOT_DECLARED_IN_ADVICE,e.getFormalName()),
                                IMessage.ERROR,
                                null,
                                bMethod.getSourceLocation())
                    );			}
        }
        hasAtAspectJAnnotation = hasAtAspectJAnnotation || hasAtAspectJAnnotationMustReturnVoid;

        // semantic check - must be in an @Aspect [remove if previous block bypassed in advance]
        if (hasAtAspectJAnnotation && !type.isAnnotationStyleAspect()) {
            msgHandler.handleMessage(
                    new Message(
                            "Found @AspectJ annotations in a non @Aspect type '" + type.getName() + "'",
                            IMessage.WARNING,
                            null,
                            type.getSourceLocation()
                    )
            );
            ;// go ahead
        }
        // semantic check - advice must be public
        if (hasAtAspectJAnnotation && !struct.method.isPublic()) {
            msgHandler.handleMessage(
                    new Message(
                            "Found @AspectJ annotation on a non public advice '" + methodToString(struct.method) + "'",
                            IMessage.ERROR,
                            null,
                            type.getSourceLocation()
                    )
            );
            ;// go ahead
        }
        
        // semantic check - advice must not be static
        if (hasAtAspectJAnnotation && struct.method.isStatic()) {
            msgHandler.handleMessage(MessageUtil.error("Advice cannot be declared static '" + methodToString(struct.method) + "'",type.getSourceLocation()));
//                    new Message(
//                            "Advice cannot be declared static '" + methodToString(struct.method) + "'",
//                            IMessage.ERROR,
//                            null,
//                            type.getSourceLocation()
//                    )
//            );
            ;// go ahead
        }
        
        // semantic check for non around advice must return void
        if (hasAtAspectJAnnotationMustReturnVoid && !Type.VOID.equals(struct.method.getReturnType())) {
            msgHandler.handleMessage(
                    new Message(
                            "Found @AspectJ annotation on a non around advice not returning void '" + methodToString(
                                    struct.method
                            ) + "'",
                            IMessage.ERROR,
                            null,
                            type.getSourceLocation()
                    )
            );
            ;// go ahead
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
    public static List readAj5FieldAttributes(Field field, BcelField bField, ResolvedType type, ISourceContext context, IMessageHandler msgHandler) {
        // Note: field annotation are for ITD and DEOW - processed at class level directly
        return Collections.EMPTY_LIST;
    }


    /**
     * Read @Aspect
     *
     * @param runtimeAnnotations
     * @param struct
     * @return true if found
     */
    private static boolean handleAspectAnnotation(RuntimeAnnotations runtimeAnnotations, AjAttributeStruct struct) {
        Annotation aspect = getAnnotation(runtimeAnnotations, AjcMemberMaker.ASPECT_ANNOTATION);
        if (aspect != null) {
            // semantic check for inheritance (only one level up)
            boolean extendsAspect = false;
            if (!"java.lang.Object".equals(struct.enclosingType.getSuperclass().getName())) {
                if (!struct.enclosingType.getSuperclass().isAbstract() && struct.enclosingType.getSuperclass().isAspect()) {
                    reportError("cannot extend a concrete aspect", struct);
                    return false;
                }
                extendsAspect = struct.enclosingType.getSuperclass().isAspect();
            }

            ElementNameValuePair aspectPerClause = getAnnotationElement(aspect, VALUE);
            final PerClause perClause;
            if (aspectPerClause == null) {
                // empty value means singleton unless inherited
                if (!extendsAspect) {
                    perClause = new PerSingleton();
                } else {
                    perClause = new PerFromSuper(struct.enclosingType.getSuperclass().getPerClause().getKind());
                }
            } else {
                String perX = aspectPerClause.getValue().stringifyValue();
                if (perX == null || perX.length() <= 0) {
                    perClause = new PerSingleton();
                } else {
                    perClause = parsePerClausePointcut(perX, struct);
                }
            }
            if (perClause == null) {
                // could not parse it, ignore the aspect
                return false;
            } else {
                perClause.setLocation(struct.context, -1,-1);//struct.context.getOffset(), struct.context.getOffset()+1);//FIXME AVASM
                // FIXME asc see related comment way about about the version...
                struct.ajAttributes.add(new AjAttribute.WeaverVersionInfo());
                AjAttribute.Aspect aspectAttribute = new AjAttribute.Aspect(perClause);
                struct.ajAttributes.add(aspectAttribute);
                FormalBinding[] bindings = new org.aspectj.weaver.patterns.FormalBinding[0];
                final IScope binding;
                binding = new BindingScope(
                            struct.enclosingType,
                            struct.context,
                            bindings
                        );

//                // we can't resolve here since the perclause typically refers to pointcuts
//                // defined in the aspect that we haven't told the BcelObjectType about yet.
//
//                perClause.resolve(binding);

                // so we prepare to do it later...
                aspectAttribute.setResolutionScope(binding);
                return true;
            }
        }
        return false;
    }

    /**
     * Read a perClause, returns null on failure and issue messages
     *
     * @param perClauseString like "pertarget(.....)"
     * @param struct    for which we are parsing the per clause
     * @return a PerClause instance
     */
    private static PerClause parsePerClausePointcut(String perClauseString, AjAttributeStruct struct) {
        final String pointcutString;
        Pointcut pointcut = null;
        TypePattern typePattern = null;
        final PerClause perClause;
        if (perClauseString.startsWith(PerClause.KindAnnotationPrefix.PERCFLOW.getName())) {
            pointcutString = PerClause.KindAnnotationPrefix.PERCFLOW.extractPointcut(perClauseString);
            pointcut = parsePointcut(pointcutString, struct, false);
            perClause = new PerCflow(pointcut, false);
        } else if (perClauseString.startsWith(PerClause.KindAnnotationPrefix.PERCFLOWBELOW.getName())) {
            pointcutString = PerClause.KindAnnotationPrefix.PERCFLOWBELOW.extractPointcut(perClauseString);
            pointcut = parsePointcut(pointcutString, struct, false);
            perClause = new PerCflow(pointcut, true);
        } else if (perClauseString.startsWith(PerClause.KindAnnotationPrefix.PERTARGET.getName())) {
            pointcutString = PerClause.KindAnnotationPrefix.PERTARGET.extractPointcut(perClauseString);
            pointcut = parsePointcut(pointcutString, struct, false);
            perClause = new PerObject(pointcut, false);
        } else if (perClauseString.startsWith(PerClause.KindAnnotationPrefix.PERTHIS.getName())) {
            pointcutString = PerClause.KindAnnotationPrefix.PERTHIS.extractPointcut(perClauseString);
            pointcut = parsePointcut(pointcutString, struct, false);
            perClause = new PerObject(pointcut, true);
        } else if (perClauseString.startsWith(PerClause.KindAnnotationPrefix.PERTYPEWITHIN.getName())) {
            pointcutString = PerClause.KindAnnotationPrefix.PERTYPEWITHIN.extractPointcut(perClauseString);
            typePattern = parseTypePattern(pointcutString, struct);
            perClause = new PerTypeWithin(typePattern);
        } else if (perClauseString.equalsIgnoreCase(PerClause.SINGLETON.getName() + "()")) {
            perClause = new PerSingleton();
        } else {
            // could not parse the @AJ perclause - fallback to singleton and issue an error
            reportError("@Aspect per clause cannot be read '" + perClauseString + "'", struct);
            return null;
        }

        if (!PerClause.SINGLETON.equals(perClause.getKind())
                && !PerClause.PERTYPEWITHIN.equals(perClause.getKind())
                && pointcut == null) {
            // we could not parse the pointcut
            return null;
        }
        if (PerClause.PERTYPEWITHIN.equals(perClause.getKind()) && typePattern == null) {
            // we could not parse the type pattern
            return null;
        }
        return perClause;
    }

    /**
     * Read @DeclarePrecedence
     *
     * @param runtimeAnnotations
     * @param struct
     * @return true if found
     */
    private static boolean handlePrecedenceAnnotation(RuntimeAnnotations runtimeAnnotations, AjAttributeStruct struct) {
        Annotation aspect = getAnnotation(runtimeAnnotations, AjcMemberMaker.DECLAREPRECEDENCE_ANNOTATION);
        if (aspect != null) {
            ElementNameValuePair precedence = getAnnotationElement(aspect, VALUE);
            if (precedence != null) {
                String precedencePattern = precedence.getValue().stringifyValue();
                PatternParser parser = new PatternParser(precedencePattern);
                DeclarePrecedence ajPrecedence = parser.parseDominates();
                struct.ajAttributes.add(new AjAttribute.DeclareAttribute(ajPrecedence));
                return true;
            }
        }
        return false;
    }

//    /**
//     * Read @DeclareImplements
//     *
//     * @param runtimeAnnotations
//     * @param struct
//     * @return true if found
//     */
//    private static boolean handleDeclareImplementsAnnotation(RuntimeAnnotations runtimeAnnotations, AjAttributeFieldStruct struct) {//, ResolvedPointcutDefinition preResolvedPointcut) {
//        Annotation deci = getAnnotation(runtimeAnnotations, AjcMemberMaker.DECLAREIMPLEMENTS_ANNOTATION);
//        if (deci != null) {
//            ElementNameValuePair deciPatternNVP = getAnnotationElement(deci, VALUE);
//            String deciPattern = deciPatternNVP.getValue().stringifyValue();
//            if (deciPattern != null) {
//                TypePattern typePattern = parseTypePattern(deciPattern, struct);
//                ResolvedType fieldType = UnresolvedType.forSignature(struct.field.getSignature()).resolve(struct.enclosingType.getWorld());
//                if (fieldType.isPrimitiveType()) {
//                    return false;
//                } else if (fieldType.isInterface()) {
//                    TypePattern parent = new ExactTypePattern(UnresolvedType.forSignature(struct.field.getSignature()), false, false);
//                    parent.resolve(struct.enclosingType.getWorld());
//                    List parents = new ArrayList(1);
//                    parents.add(parent);
//                    //TODO kick ISourceLocation sl = struct.bField.getSourceLocation();    ??
//                    struct.ajAttributes.add(
//                            new AjAttribute.DeclareAttribute(
//                                    new DeclareParents(
//                                        typePattern,
//                                        parents,
//                                        false
//                                    )
//                            )
//                    );
//                    return true;
//                } else {
//                    reportError("@DeclareImplements: can only be used on field whose type is an interface", struct);
//                    return false;
//                }
//            }
//        }
//        return false;
//    }

    /**
     * Read @DeclareParents
     *
     * @param runtimeAnnotations
     * @param struct
     * @return true if found
     */
    private static boolean handleDeclareParentsAnnotation(RuntimeAnnotations runtimeAnnotations, AjAttributeFieldStruct struct) {//, ResolvedPointcutDefinition preResolvedPointcut) {
        Annotation decp = getAnnotation(runtimeAnnotations, AjcMemberMaker.DECLAREPARENTS_ANNOTATION);
        if (decp != null) {
            ElementNameValuePair decpPatternNVP = getAnnotationElement(decp, VALUE);
            String decpPattern = decpPatternNVP.getValue().stringifyValue();
            if (decpPattern != null) {
                TypePattern typePattern = parseTypePattern(decpPattern, struct);
                ResolvedType fieldType = UnresolvedType.forSignature(struct.field.getSignature()).resolve(struct.enclosingType.getWorld());
                if (fieldType.isInterface()) {
                    TypePattern parent = parseTypePattern(fieldType.getName(),struct);
                    FormalBinding[] bindings = new org.aspectj.weaver.patterns.FormalBinding[0];
	                IScope binding = new BindingScope(struct.enclosingType,struct.context,bindings);
                    // first add the declare implements like
                    List parents = new ArrayList(1); parents.add(parent);
                    DeclareParents dp = new DeclareParents(typePattern,parents,false);
                    dp.resolve(binding); // resolves the parent and child parts of the decp
                    
                    // resolve this so that we can use it for the MethodDelegateMungers below.
                    // eg. '@Coloured *' will change from a WildTypePattern to an 'AnyWithAnnotationTypePattern' after this resolution
                    typePattern = typePattern.resolveBindings(binding, Bindings.NONE, false, false);
                    //TODO kick ISourceLocation sl = struct.bField.getSourceLocation();    ??
                    // dp.setLocation(dp.getDeclaringType().getSourceContext(), dp.getDeclaringType().getSourceLocation().getOffset(), dp.getDeclaringType().getSourceLocation().getOffset());
                    dp.setLocation(struct.context,-1,-1); // not ideal...
                    struct.ajAttributes.add(new AjAttribute.DeclareAttribute(dp));


                    // do we have a defaultImpl=xxx.class (ie implementation)
                    String defaultImplClassName = null;
                    ElementNameValuePair defaultImplNVP = getAnnotationElement(decp, "defaultImpl");
                    if (defaultImplNVP != null) {
                        ClassElementValue defaultImpl = (ClassElementValue) defaultImplNVP.getValue();
                        defaultImplClassName = UnresolvedType.forSignature(defaultImpl.getClassString()).getName();
                        if (defaultImplClassName.equals("org.aspectj.lang.annotation.DeclareParents")) {
                            defaultImplClassName = null;
                        } else {
                            // check public no arg ctor
                            ResolvedType impl = struct.enclosingType.getWorld().resolve(
                                    defaultImplClassName,
                                    false
                            );
                            ResolvedMember[] mm  = impl.getDeclaredMethods();
                            boolean hasNoCtorOrANoArgOne = true;
                            for (int i = 0; i < mm.length; i++) {
                                ResolvedMember resolvedMember = mm[i];
                                if (resolvedMember.getName().equals("<init>")) {
                                    hasNoCtorOrANoArgOne = false;
                                    if (resolvedMember.getParameterTypes().length == 0
                                        && resolvedMember.isPublic()) {
                                        hasNoCtorOrANoArgOne = true;
                                    }
                                }
                                if (hasNoCtorOrANoArgOne) {
                                    break;
                                }
                            }
                            if (!hasNoCtorOrANoArgOne) {
                                reportError("@DeclareParents: defaultImpl=\""
                                            + defaultImplClassName
                                            + "\" has no public no-arg constructor", struct);
                            }
                            if (!fieldType.isAssignableFrom(impl)) {
                            	reportError("@DeclareParents: defaultImpl=\""+defaultImplClassName+"\" does not implement the interface '"+fieldType.toString()+"'",struct);
                            }
                        }

                    }

                    // then iterate on field interface hierarchy (not object)
                    boolean hasAtLeastOneMethod = false;
                    ResolvedMember[] methods = (ResolvedMember[])fieldType.getMethodsWithoutIterator(true, false).toArray(new ResolvedMember[0]);
                    for (int i = 0; i < methods.length; i++) {
                        ResolvedMember method = (ResolvedMember)methods[i];
                        if (method.isAbstract()) {
                        // moved to be detected at weave time if the target doesnt implement the methods
//                            if (defaultImplClassName == null) {
//                                // non marker interface with no default impl provided
//                                reportError("@DeclareParents: used with a non marker interface and no defaultImpl=\"...\" provided", struct);
//                                return false;
//                            }
                            hasAtLeastOneMethod = true;
                            MethodDelegateTypeMunger mdtm = 
                                new MethodDelegateTypeMunger(
                                    method,
                                    struct.enclosingType,
                                    defaultImplClassName,
                                    typePattern
                                );
                            mdtm.setSourceLocation(struct.enclosingType.getSourceLocation());
                            struct.ajAttributes.add(new AjAttribute.TypeMunger(mdtm));
                        }
                    }
                    // successfull so far, we thus need a bcel type munger to have
                    // a field hosting the mixin in the target type
                    if (hasAtLeastOneMethod && defaultImplClassName!=null) {
                        struct.ajAttributes.add(
                                new AjAttribute.TypeMunger(
                                        new MethodDelegateTypeMunger.FieldHostTypeMunger(
                                                AjcMemberMaker.itdAtDeclareParentsField(
                                                        null,//prototyped
                                                        fieldType,
                                                        struct.enclosingType
                                                ),
                                                struct.enclosingType,
                                                typePattern
                                        )
                                )
                        );
                    }

                    return true;
                } else {
                    reportError("@DeclareParents: can only be used on a field whose type is an interface", struct);
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * Read @Before
     *
     * @param runtimeAnnotations
     * @param struct
     * @return true if found
     */
    private static boolean handleBeforeAnnotation(RuntimeAnnotations runtimeAnnotations, AjAttributeMethodStruct struct, ResolvedPointcutDefinition preResolvedPointcut) {
        Annotation before = getAnnotation(runtimeAnnotations, AjcMemberMaker.BEFORE_ANNOTATION);
        if (before != null) {
            ElementNameValuePair beforeAdvice = getAnnotationElement(before, VALUE);
            if (beforeAdvice != null) {
                // this/target/args binding
            	String argumentNames = getArgNamesValue(before);
                if (argumentNames!=null) {
                	struct.unparsedArgumentNames = argumentNames;
                }
                FormalBinding[] bindings = new org.aspectj.weaver.patterns.FormalBinding[0];
                try {
                    bindings = extractBindings(struct);
                } catch (UnreadableDebugInfoException unreadableDebugInfoException) {
                    return false;
                }
                IScope binding = new BindingScope(
                        struct.enclosingType,
                        struct.context,
                        bindings
                );

                // joinpoint, staticJoinpoint binding
                int extraArgument = extractExtraArgument(struct.method);

                Pointcut pc = null;
                if (preResolvedPointcut != null) {
                    pc = preResolvedPointcut.getPointcut();
                    //pc.resolve(binding);
                } else {
                    pc = parsePointcut(beforeAdvice.getValue().stringifyValue(), struct, false);
                    if (pc == null) return false;//parse error
                    pc = pc.resolve(binding);
                }
                setIgnoreUnboundBindingNames(pc, bindings);

                ISourceLocation sl = struct.context.makeSourceLocation(struct.bMethod.getDeclarationLineNumber(), struct.bMethod.getDeclarationOffset());
                struct.ajAttributes.add(
                        new AjAttribute.AdviceAttribute(
                                AdviceKind.Before,
                                pc,
                                extraArgument,
                                sl.getOffset(),
                                sl.getOffset()+1,//FIXME AVASM
                                struct.context
                        )
                );
                return true;
            }
        }
        return false;
    }

    /**
     * Read @After
     *
     * @param runtimeAnnotations
     * @param struct
     * @return true if found
     */
    private static boolean handleAfterAnnotation(RuntimeAnnotations runtimeAnnotations, AjAttributeMethodStruct struct, ResolvedPointcutDefinition preResolvedPointcut) {
        Annotation after = getAnnotation(runtimeAnnotations, AjcMemberMaker.AFTER_ANNOTATION);
        if (after != null) {
            ElementNameValuePair afterAdvice = getAnnotationElement(after, VALUE);
            if (afterAdvice != null) {
                // this/target/args binding
                FormalBinding[] bindings = new org.aspectj.weaver.patterns.FormalBinding[0];
                String argumentNames = getArgNamesValue(after);
                if (argumentNames!=null) {
                	struct.unparsedArgumentNames = argumentNames;
                }
                try {
                    bindings = extractBindings(struct);
                } catch (UnreadableDebugInfoException unreadableDebugInfoException) {
                    return false;
                }
                IScope binding = new BindingScope(
                        struct.enclosingType,
                        struct.context,
                        bindings
                );

                // joinpoint, staticJoinpoint binding
                int extraArgument = extractExtraArgument(struct.method);

                Pointcut pc = null;
                if (preResolvedPointcut != null) {
                    pc = preResolvedPointcut.getPointcut();
                } else {
                    pc = parsePointcut(afterAdvice.getValue().stringifyValue(), struct, false);
                    if (pc == null) return false;//parse error
                    pc.resolve(binding);
                }
                setIgnoreUnboundBindingNames(pc, bindings);

                ISourceLocation sl = struct.context.makeSourceLocation(struct.bMethod.getDeclarationLineNumber(), struct.bMethod.getDeclarationOffset());
                struct.ajAttributes.add(
                        new AjAttribute.AdviceAttribute(
                                AdviceKind.After,
                                pc,
                                extraArgument,
                                sl.getOffset(),
                                sl.getOffset()+1,//FIXME AVASM
                                struct.context
                        )
                );
                return true;
            }
        }
        return false;
    }

    /**
     * Read @AfterReturning
     *
     * @param runtimeAnnotations
     * @param struct
     * @return true if found
     */
    private static boolean handleAfterReturningAnnotation(
            RuntimeAnnotations runtimeAnnotations,
            AjAttributeMethodStruct struct,
            ResolvedPointcutDefinition preResolvedPointcut,
            BcelMethod owningMethod)
    throws ReturningFormalNotDeclaredInAdviceSignatureException
    {
        Annotation after = getAnnotation(runtimeAnnotations, AjcMemberMaker.AFTERRETURNING_ANNOTATION);
        if (after != null) {
            ElementNameValuePair annValue = getAnnotationElement(after, VALUE);
            ElementNameValuePair annPointcut = getAnnotationElement(after, POINTCUT);
            ElementNameValuePair annReturned = getAnnotationElement(after, RETURNING);

            // extract the pointcut and returned type/binding - do some checks
            String pointcut = null;
            String returned = null;
            if ((annValue != null && annPointcut != null) || (annValue == null && annPointcut == null)) {
                reportError("@AfterReturning: either 'value' or 'poincut' must be provided, not both", struct);
                return false;
            }
            if (annValue != null) {
                pointcut = annValue.getValue().stringifyValue();
            } else {
                pointcut = annPointcut.getValue().stringifyValue();
            }
            if (isNullOrEmpty(pointcut)) {
                reportError("@AfterReturning: either 'value' or 'poincut' must be provided, not both", struct);
                return false;
            }
            if (annReturned != null) {
                returned = annReturned.getValue().stringifyValue();
                if (isNullOrEmpty(returned)) {
                    returned = null;
                } else {
                       // check that thrownFormal exists as the last parameter in the advice
                    String[] pNames = owningMethod.getParameterNames();
                    if (pNames == null || pNames.length == 0 || !Arrays.asList(pNames).contains(returned)) {
                        throw new ReturningFormalNotDeclaredInAdviceSignatureException(returned);
                    }
                }
            }
            String argumentNames = getArgNamesValue(after);
            if (argumentNames!=null) {
            	struct.unparsedArgumentNames = argumentNames;
            }
            // this/target/args binding
            // exclude the return binding from the pointcut binding since it is an extraArg binding
            FormalBinding[] bindings = new org.aspectj.weaver.patterns.FormalBinding[0];
            try {
                bindings = (returned == null ? extractBindings(struct) : extractBindings(struct, returned));
            } catch (UnreadableDebugInfoException unreadableDebugInfoException) {
                return false;
            }
            IScope binding = new BindingScope(
                    struct.enclosingType,
                    struct.context,
                    bindings
            );

            // joinpoint, staticJoinpoint binding
            int extraArgument = extractExtraArgument(struct.method);

            // return binding
            if (returned != null) {
                extraArgument |= Advice.ExtraArgument;
            }

            Pointcut pc = null;
            if (preResolvedPointcut != null) {
                pc = preResolvedPointcut.getPointcut();
            } else {
                pc = parsePointcut(pointcut, struct, false);
                if (pc == null) return false;//parse error
                pc.resolve(binding);
            }
            setIgnoreUnboundBindingNames(pc, bindings);

            ISourceLocation sl = struct.context.makeSourceLocation(struct.bMethod.getDeclarationLineNumber(), struct.bMethod.getDeclarationOffset());
            struct.ajAttributes.add(
                    new AjAttribute.AdviceAttribute(
                            AdviceKind.AfterReturning,
                            pc,
                            extraArgument,
                            sl.getOffset(),
                            sl.getOffset()+1,//FIXME AVASM
                            struct.context
                    )
            );
            return true;
        }
        return false;
    }

    /**
     * Read @AfterThrowing
     *
     * @param runtimeAnnotations
     * @param struct
     * @return true if found
     */
    private static boolean handleAfterThrowingAnnotation(
            RuntimeAnnotations runtimeAnnotations,
            AjAttributeMethodStruct struct,
            ResolvedPointcutDefinition preResolvedPointcut,
            BcelMethod owningMethod)
    throws ThrownFormalNotDeclaredInAdviceSignatureException
    {
        Annotation after = getAnnotation(runtimeAnnotations, AjcMemberMaker.AFTERTHROWING_ANNOTATION);
        if (after != null) {
            ElementNameValuePair annValue = getAnnotationElement(after, VALUE);
            ElementNameValuePair annPointcut = getAnnotationElement(after, POINTCUT);
            ElementNameValuePair annThrown = getAnnotationElement(after, THROWING);

            // extract the pointcut and throwned type/binding - do some checks
            String pointcut = null;
            String thrownFormal = null;
            if ((annValue != null && annPointcut != null) || (annValue == null && annPointcut == null)) {
                reportError("@AfterThrowing: either 'value' or 'poincut' must be provided, not both", struct);
                return false;
            }
            if (annValue != null) {
                pointcut = annValue.getValue().stringifyValue();
            } else {
                pointcut = annPointcut.getValue().stringifyValue();
            }
            if (isNullOrEmpty(pointcut)) {
                reportError("@AfterThrowing: either 'value' or 'poincut' must be provided, not both", struct);
                return false;
            }
            if (annThrown != null) {
                thrownFormal = annThrown.getValue().stringifyValue();
                if (isNullOrEmpty(thrownFormal)) {
                    thrownFormal = null;
                } else {
                    // check that thrownFormal exists as the last parameter in the advice
                    String[] pNames = owningMethod.getParameterNames();
                    if (pNames == null || pNames.length == 0 || !Arrays.asList(pNames).contains(thrownFormal)) {
                        throw new ThrownFormalNotDeclaredInAdviceSignatureException(thrownFormal);
                    }
                }
            }
            String argumentNames = getArgNamesValue(after);
            if (argumentNames!=null) {
            	struct.unparsedArgumentNames = argumentNames;
            }
            // this/target/args binding
            // exclude the throwned binding from the pointcut binding since it is an extraArg binding
            FormalBinding[] bindings = new org.aspectj.weaver.patterns.FormalBinding[0];
            try {
                bindings = (thrownFormal == null ? extractBindings(struct) : extractBindings(struct, thrownFormal));
            } catch (UnreadableDebugInfoException unreadableDebugInfoException) {
                return false;
            }
            IScope binding = new BindingScope(
                    struct.enclosingType,
                    struct.context,
                    bindings
            );

            // joinpoint, staticJoinpoint binding
            int extraArgument = extractExtraArgument(struct.method);

            // return binding
            if (thrownFormal != null) {
                extraArgument |= Advice.ExtraArgument;
            }

            Pointcut pc = null;
            if (preResolvedPointcut != null) {
                pc = preResolvedPointcut.getPointcut();
            } else {
                pc = parsePointcut(pointcut, struct, false);
                if (pc == null) return false;//parse error
                pc.resolve(binding);
            }
            setIgnoreUnboundBindingNames(pc, bindings);

            ISourceLocation sl = struct.context.makeSourceLocation(struct.bMethod.getDeclarationLineNumber(), struct.bMethod.getDeclarationOffset());
            struct.ajAttributes.add(
                    new AjAttribute.AdviceAttribute(
                            AdviceKind.AfterThrowing,
                            pc,
                            extraArgument,
                            sl.getOffset(),
                            sl.getOffset()+1,//FIXME AVASM
                            struct.context
                    )
            );
            return true;
        }
        return false;
    }

    /**
     * Read @Around
     *
     * @param runtimeAnnotations
     * @param struct
     * @return true if found
     */
    private static boolean handleAroundAnnotation(RuntimeAnnotations runtimeAnnotations, AjAttributeMethodStruct struct, ResolvedPointcutDefinition preResolvedPointcut) {
        Annotation around = getAnnotation(runtimeAnnotations, AjcMemberMaker.AROUND_ANNOTATION);
        if (around != null) {
            ElementNameValuePair aroundAdvice = getAnnotationElement(around, VALUE);
            if (aroundAdvice != null) {
                // this/target/args binding 
            	String argumentNames = getArgNamesValue(around);
                if (argumentNames!=null) {
                	struct.unparsedArgumentNames = argumentNames;
                }
                FormalBinding[] bindings = new org.aspectj.weaver.patterns.FormalBinding[0];
                try {
                    bindings = extractBindings(struct);
                } catch (UnreadableDebugInfoException unreadableDebugInfoException) {
                    return false;
                }
                IScope binding = new BindingScope(
                        struct.enclosingType,
                        struct.context,
                        bindings
                );

                // joinpoint, staticJoinpoint binding
                int extraArgument = extractExtraArgument(struct.method);

                Pointcut pc = null;
                if (preResolvedPointcut != null) {
                    pc = preResolvedPointcut.getPointcut();
                } else {
                    pc = parsePointcut(aroundAdvice.getValue().stringifyValue(), struct, false);
                    if (pc == null) return false;//parse error
                    pc.resolve(binding);
                }
                setIgnoreUnboundBindingNames(pc, bindings);

                ISourceLocation sl = struct.context.makeSourceLocation(struct.bMethod.getDeclarationLineNumber(), struct.bMethod.getDeclarationOffset());
                struct.ajAttributes.add(
                        new AjAttribute.AdviceAttribute(
                                AdviceKind.Around,
                                pc,
                                extraArgument,
                                sl.getOffset(),
                                sl.getOffset()+1,//FIXME AVASM
                                struct.context
                        )
                );
                return true;
            }
        }
        return false;
    }

    /**
     * Read @Pointcut and handle the resolving in a lazy way to deal with pointcut references
     *
     * @param runtimeAnnotations
     * @param struct
     * @return true if a pointcut was handled
     */
    private static boolean handlePointcutAnnotation(RuntimeAnnotations runtimeAnnotations, AjAttributeMethodStruct struct) {
        Annotation pointcut = getAnnotation(runtimeAnnotations, AjcMemberMaker.POINTCUT_ANNOTATION);
        if (pointcut==null) return false;
        ElementNameValuePair pointcutExpr = getAnnotationElement(pointcut, VALUE);

        
        // semantic check: the method must return void, or be "public static boolean" for if() support
        if (!(Type.VOID.equals(struct.method.getReturnType())
              || (Type.BOOLEAN.equals(struct.method.getReturnType()) && struct.method.isStatic() && struct.method.isPublic()))) {
            reportWarning("Found @Pointcut on a method not returning 'void' or not 'public static boolean'", struct);
            ;//no need to stop
        }

        // semantic check: the method must not throw anything
        if (struct.method.getExceptionTable() != null) {
            reportWarning("Found @Pointcut on a method throwing exception", struct);
            ;// no need to stop
        }

        String argumentNames = getArgNamesValue(pointcut);
        if (argumentNames!=null) {
        	struct.unparsedArgumentNames = argumentNames;
        }
        // this/target/args binding
        final IScope binding;
        try {
        	if (struct.method.isAbstract()) {
        	    binding = null;
        	} else {
                binding = new BindingScope(
                    struct.enclosingType,
                    struct.context,
                    extractBindings(struct)
                );
        	}
        } catch (UnreadableDebugInfoException e) {
            return false;
        }

        UnresolvedType[] argumentTypes = new UnresolvedType[struct.method.getArgumentTypes().length];
        for (int i = 0; i < argumentTypes.length; i++) {
            argumentTypes[i] = UnresolvedType.forSignature(struct.method.getArgumentTypes()[i].getSignature());
        }

        Pointcut pc = null;
        if (struct.method.isAbstract()) {
            if ((pointcutExpr != null && isNullOrEmpty(pointcutExpr.getValue().stringifyValue()))
                || pointcutExpr == null) {
                // abstract pointcut
                // leave pc = null
            } else {
                reportError("Found defined @Pointcut on an abstract method", struct);
                return false;//stop
            }
        } else {
        	if (pointcutExpr==null || (pointcutExpr != null && isNullOrEmpty(pointcutExpr.getValue().stringifyValue()))) {
        		// the matches nothing pointcut (125475/125480) - perhaps not as cleanly supported as it could be.
        	} else {
              if (pointcutExpr != null) {
                // use a LazyResolvedPointcutDefinition so that the pointcut is resolved lazily
                // since for it to be resolved, we will need other pointcuts to be registered as well
                pc = parsePointcut(pointcutExpr.getValue().stringifyValue(), struct, true);
                if (pc == null) return false;//parse error
                pc.setLocation(struct.context, -1, -1);//FIXME AVASM !! bMethod is null here..
              } else {
                reportError("Found undefined @Pointcut on a non-abstract method", struct);
                return false;
              }
        	}
        }
        // do not resolve binding now but lazily
        struct.ajAttributes.add(
                new AjAttribute.PointcutDeclarationAttribute(
                        new LazyResolvedPointcutDefinition(
                                struct.enclosingType,
                                struct.method.getModifiers(),
                                struct.method.getName(),
                                argumentTypes,
                                UnresolvedType.forSignature(struct.method.getReturnType().getSignature()),
                                pc,//can be null for abstract pointcut
                                binding // can be null for abstract pointcut
                        )
                )
        );
        return true;
    }

    /**
     * Read @DeclareError, @DeclareWarning
     *
     * @param runtimeAnnotations
     * @param struct
     * @return true if found
     */
    private static boolean handleDeclareErrorOrWarningAnnotation(RuntimeAnnotations runtimeAnnotations, AjAttributeFieldStruct struct) {
        Annotation error = getAnnotation(runtimeAnnotations, AjcMemberMaker.DECLAREERROR_ANNOTATION);
        boolean hasError = false;
        if (error != null) {
            ElementNameValuePair declareError = getAnnotationElement(error, VALUE);
            if (declareError != null) {
                if (!STRING_DESC.equals(struct.field.getSignature()) || struct.field.getConstantValue() == null) {
                    reportError("@DeclareError used on a non String constant field", struct);
                    return false;
                }
                Pointcut pc = parsePointcut(declareError.getValue().stringifyValue(), struct, false);
                if (pc == null) {
                    hasError = false;//cannot parse pointcut
                } else {                	 
                    DeclareErrorOrWarning deow = new DeclareErrorOrWarning(true, pc, struct.field.getConstantValue().toString());
                    setDeclareErrorOrWarningLocation(deow,struct);
                    struct.ajAttributes.add(new AjAttribute.DeclareAttribute(deow));
                    hasError = true;
                }
            }
        }
        Annotation warning = getAnnotation(runtimeAnnotations, AjcMemberMaker.DECLAREWARNING_ANNOTATION);
        boolean hasWarning = false;
        if (warning != null) {
            ElementNameValuePair declareWarning = getAnnotationElement(warning, VALUE);
            if (declareWarning != null) {
                if (!STRING_DESC.equals(struct.field.getSignature()) || struct.field.getConstantValue() == null) {
                    reportError("@DeclareWarning used on a non String constant field", struct);
                    return false;
                }
                Pointcut pc = parsePointcut(declareWarning.getValue().stringifyValue(), struct, false);
                if (pc == null) {
                    hasWarning = false;//cannot parse pointcut
                } else {
                    DeclareErrorOrWarning deow = new DeclareErrorOrWarning(false, pc, struct.field.getConstantValue().toString());
                    setDeclareErrorOrWarningLocation(deow,struct);
                    struct.ajAttributes.add(new AjAttribute.DeclareAttribute(deow));
                    return hasWarning = true;
                }
            }
        }
        return hasError || hasWarning;
    }

    /**
     * Sets the location for the declare error / warning using the corresponding 
     * IProgramElement in the structure model. This will only fix bug 120356 if
     * compiled with -emacssym, however, it does mean that the cross references 
     * view in AJDT will show the correct information.
     * 
     * Other possibilities for fix: 
     *  1. using the information in ajcDeclareSoft (if this is set correctly) 
     *     which will fix the problem if compiled with ajc but not if compiled 
     *     with javac.
     *  2. creating an AjAttribute called FieldDeclarationLineNumberAttribute 
     *     (much like MethodDeclarationLineNumberAttribute) which we can ask 
     *     for the offset. This will again only fix bug 120356 when compiled 
     *     with ajc.
     * 
     * @param deow
     * @param struct
     */
    private static void setDeclareErrorOrWarningLocation(DeclareErrorOrWarning deow, AjAttributeFieldStruct struct) {
        IHierarchy top = AsmManager.getDefault().getHierarchy();
        if (top.getRoot() != null) {
        	IProgramElement ipe = top.findElementForLabel(top.getRoot(),
      			  IProgramElement.Kind.FIELD,struct.field.getName());
        	if (ipe != null && ipe.getSourceLocation() != null) {
    			ISourceLocation sourceLocation = ipe.getSourceLocation();
    			int start = sourceLocation.getOffset();
    			int end = start + struct.field.getName().length();
    			deow.setLocation(struct.context,start,end);
    			return;
    		}
		}
        deow.setLocation(struct.context, -1, -1);												
    }
    
    /**
     * Returns a readable representation of a method.
     * Method.toString() is not suitable.
     *
     * @param method
     * @return a readable representation of a method
     */
    private static String methodToString(Method method) {
        StringBuffer sb = new StringBuffer();
        sb.append(method.getName());
        sb.append(method.getSignature());
        return sb.toString();
    }

    /**
     * Returns a readable representation of a field.
     * Field.toString() is not suitable.
     *
     * @param field
     * @return a readable representation of a field
     */
    private static String fieldToString(Field field) {
        StringBuffer sb = new StringBuffer();
        sb.append(field.getName()).append(' ');
        sb.append(field.getSignature());
        return sb.toString();
    }

    /**
     * Build the bindings for a given method (pointcut / advice)
     *
     * @param struct
     * @return null if no debug info is available
     */
    private static FormalBinding[] extractBindings(AjAttributeMethodStruct struct)
            throws UnreadableDebugInfoException {
        Method method = struct.method;
        String[] argumentNames = struct.getArgumentNames();

        // assert debug info was here
        if (argumentNames.length != method.getArgumentTypes().length) {
            reportError("Cannot read debug info for @Aspect to handle formal binding in pointcuts (please compile with 'javac -g' or '<javac debug='true'.../>' in Ant)", struct);
            throw new UnreadableDebugInfoException();
        }

        List bindings = new ArrayList();
        for (int i = 0; i < argumentNames.length; i++) {
            String argumentName = argumentNames[i];
            UnresolvedType argumentType = UnresolvedType.forSignature(method.getArgumentTypes()[i].getSignature());

            // do not bind JoinPoint / StaticJoinPoint / EnclosingStaticJoinPoint
            // TODO solve me : this means that the JP/SJP/ESJP cannot appear as binding
            // f.e. when applying advice on advice etc
            if ((AjcMemberMaker.TYPEX_JOINPOINT.equals(argumentType)
                    || AjcMemberMaker.TYPEX_PROCEEDINGJOINPOINT.equals(argumentType)
                    || AjcMemberMaker.TYPEX_STATICJOINPOINT.equals(argumentType)
                    || AjcMemberMaker.TYPEX_ENCLOSINGSTATICJOINPOINT.equals(argumentType)
                    || AjcMemberMaker.AROUND_CLOSURE_TYPE.equals(argumentType))) {
                //continue;// skip
                bindings.add(new FormalBinding.ImplicitFormalBinding(argumentType, argumentName, i));
            } else {
                bindings.add(new FormalBinding(argumentType, argumentName, i));
            }
        }

        return (FormalBinding[]) bindings.toArray(new FormalBinding[]{});
    }

    //FIXME alex deal with exclude index
    private static FormalBinding[] extractBindings(AjAttributeMethodStruct struct, String excludeFormal)
            throws UnreadableDebugInfoException {
        FormalBinding[] bindings = extractBindings(struct);
        int excludeIndex = -1;
        for (int i = 0; i < bindings.length; i++) {
            FormalBinding binding = bindings[i];
            if (binding.getName().equals(excludeFormal)) {
                excludeIndex = i;
                bindings[i] = new FormalBinding.ImplicitFormalBinding(
                        binding.getType(), binding.getName(), binding.getIndex()
                );
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
     * @return extra arg flag
     */
    private static int extractExtraArgument(Method method) {
        Type[] methodArgs = method.getArgumentTypes();
        String[] sigs = new String[methodArgs.length];
        for (int i = 0; i < methodArgs.length; i++) {
            sigs[i] = methodArgs[i].getSignature();
        }
        return extractExtraArgument(sigs);
    }

    /**
     * Compute the flag for the xxxJoinPoint extra argument
     *
     * @param argumentSignatures
     * @return  extra arg flag
     */
    public static int extractExtraArgument(String[] argumentSignatures) {
        int extraArgument = 0;
        for (int i = 0; i < argumentSignatures.length; i++) {
            if (AjcMemberMaker.TYPEX_JOINPOINT.getSignature().equals(argumentSignatures[i])) {
                extraArgument |= Advice.ThisJoinPoint;
            } else if (AjcMemberMaker.TYPEX_PROCEEDINGJOINPOINT.getSignature().equals(argumentSignatures[i])) {
                extraArgument |= Advice.ThisJoinPoint;
            } else if (AjcMemberMaker.TYPEX_STATICJOINPOINT.getSignature().equals(argumentSignatures[i])) {
                extraArgument |= Advice.ThisJoinPointStaticPart;
            } else if (AjcMemberMaker.TYPEX_ENCLOSINGSTATICJOINPOINT.getSignature().equals(argumentSignatures[i])) {
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
     * @return annotation
     */
    private static Annotation getAnnotation(RuntimeAnnotations rvs, UnresolvedType annotationType) {
        final String annotationTypeName = annotationType.getName();
        for (Iterator iterator = rvs.getAnnotations().iterator(); iterator.hasNext();) {
            Annotation rv = (Annotation) iterator.next();
            if (annotationTypeName.equals(rv.getTypeName())) {
                return rv;
            }
        }
        return null;
    }

    /**
     * Returns the value of a given element of an annotation or null if not found
     * Caution: Does not handles default value.
     *
     * @param annotation
     * @param elementName
     * @return annotation NVP
     */
    private static ElementNameValuePair getAnnotationElement(Annotation annotation, String elementName) {
        for (Iterator iterator1 = annotation.getValues().iterator(); iterator1.hasNext();) {
            ElementNameValuePair element = (ElementNameValuePair) iterator1.next();
            if (elementName.equals(element.getNameString())) {
                return element;
            }
        }
        return null;
    }
    
    /**
     * Return the argNames set for an annotation or null if it is not specified.
     */
    private static String getArgNamesValue(Annotation anno) {
    	 for (Iterator iterator1 = anno.getValues().iterator(); iterator1.hasNext();) {
             ElementNameValuePair element = (ElementNameValuePair) iterator1.next();
             if (ARGNAMES.equals(element.getNameString())) {
                 return element.getValue().stringifyValue();
             }
         }
         return null;
    }
    
    private static String lastbit(String fqname) {
    	int i = fqname.lastIndexOf(".");
    	if (i==-1) return fqname; else return fqname.substring(i+1);
    }

    /**
     * Extract the method argument names.  First we try the debug info attached
     * to the method (the LocalVariableTable) - if we cannot find that we look
     * to use the argNames value that may have been supplied on the associated
     * annotation.  If that fails we just don't know and return an empty string.
     *
     * @param method
     * @param argNamesFromAnnotation 
     * @param methodStruct 
     * @return method argument names
     */
    private static String[] getMethodArgumentNames(Method method, String argNamesFromAnnotation, AjAttributeMethodStruct methodStruct) {
        if (method.getArgumentTypes().length == 0) {
            return EMPTY_STRINGS;
        }

        final int startAtStackIndex = method.isStatic() ? 0 : 1;
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
        } else {
        	// No debug info, do we have an annotation value we can rely on?
        	if (argNamesFromAnnotation!=null) {
        		StringTokenizer st = new StringTokenizer(argNamesFromAnnotation," ,");
        		List args = new ArrayList();
        		while (st.hasMoreTokens()) { args.add(st.nextToken());}
        		if (args.size()!=method.getArgumentTypes().length) {
        			StringBuffer shortString = new StringBuffer().append(lastbit(method.getReturnType().toString())).append(" ").append(method.getName());
        			if (method.getArgumentTypes().length>0) {
        				shortString.append("(");
        				for (int i =0; i<method.getArgumentTypes().length;i++) {
        					shortString.append(lastbit(method.getArgumentTypes()[i].toString()));
        					if ((i+1)<method.getArgumentTypes().length) shortString.append(",");
        					
        				}
        				shortString.append(")");
        		    }
        			reportError("argNames annotation value does not specify the right number of argument names for the method '"+shortString.toString()+"'",methodStruct);
                    return EMPTY_STRINGS;
        		}
        		return (String[])args.toArray(new String[]{});
        	}
        }

        if (arguments.size() != method.getArgumentTypes().length) {
            return EMPTY_STRINGS;
        }

        // sort by index
        Collections.sort(
                arguments, new Comparator() {
                    public int compare(Object o, Object o1) {
                        MethodArgument mo = (MethodArgument) o;
                        MethodArgument mo1 = (MethodArgument) o1;
                        if (mo.indexOnStack == mo1.indexOnStack) {
                            return 0;
                        } else if (mo.indexOnStack > mo1.indexOnStack) {
                            return 1;
                        } else {
                            return -1;
                        }
                    }
                }
        );
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
        private ResolvedType m_enclosingType;
        private ISourceContext m_sourceContext;

        public BindingScope(ResolvedType type, ISourceContext sourceContext, FormalBinding[] bindings) {
            super(type.getWorld(), bindings);
            m_enclosingType = type;
            m_sourceContext = sourceContext;
        }

        public ResolvedType getEnclosingType() {
            return m_enclosingType;
        }

        public ISourceLocation makeSourceLocation(IHasPosition location) {
            return m_sourceContext.makeSourceLocation(location);
        }
        
        public UnresolvedType lookupType(String name, IHasPosition location) {
        	// bug 126560  
        	if (m_enclosingType != null) {
        		// add the package we're in to the list of imported
            	// prefixes so that we can find types in the same package 
			String pkgName = m_enclosingType.getPackageName();
			if (pkgName != null && !pkgName.equals("")) {
				String[] currentImports = getImportedPrefixes();
				String[] newImports = new String[currentImports.length + 1];
				for (int i = 0; i < currentImports.length; i++) {
					newImports[i] = currentImports[i];
				}
				newImports[currentImports.length] = pkgName.concat(".");
				setImportedPrefixes(newImports);
			}
		}
        	return super.lookupType(name,location);
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

        public LazyResolvedPointcutDefinition(UnresolvedType declaringType, int modifiers, String name,
                                              UnresolvedType[] parameterTypes, UnresolvedType returnType,
                                              Pointcut pointcut, IScope binding) {
            super(declaringType, modifiers, name, parameterTypes, returnType, null);
            m_pointcutUnresolved = pointcut;
            m_binding = binding;
        }

        public Pointcut getPointcut() {
            if (m_lazyPointcut == null) {
                m_lazyPointcut = m_pointcutUnresolved.resolve(m_binding);
                m_lazyPointcut.copyLocationFrom(m_pointcutUnresolved);
            }
            return m_lazyPointcut;
        }
    }

    /**
     * Helper to test empty strings
     *
     * @param s
     * @return true if empty or null
     */
    private static boolean isNullOrEmpty(String s) {
        return (s == null || s.length() <= 0);
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
        pointcut.m_ignoreUnboundBindingForNames = (String[]) ignores.toArray(new String[ignores.size()]);
    }

    /**
     * A check exception when we cannot read debug info (needed for formal binding)
     */
    private static class UnreadableDebugInfoException extends Exception {
    }

    /**
     * Report an error
     *
     * @param message
     * @param location
     */
    private static void reportError(String message, AjAttributeStruct location) {
        if (!location.handler.isIgnoring(IMessage.ERROR)) {
            location.handler.handleMessage(
                    new Message(
                            message,
                            location.enclosingType.getSourceLocation(),
                            true
                    )
            );
        }
    }

    /**
     * Report a warning
     *
     * @param message
     * @param location
     */
    private static void reportWarning(String message, AjAttributeStruct location) {
        if (!location.handler.isIgnoring(IMessage.WARNING)) {
            location.handler.handleMessage(
                    new Message(
                            message,
                            location.enclosingType.getSourceLocation(),
                            false
                    )
            );
        }
    }

    /**
     * Parse the given pointcut, return null on failure and issue an error
     *
     * @param pointcutString
     * @param struct
     * @param allowIf
     * @return pointcut, unresolved
     */
    private static Pointcut parsePointcut(String pointcutString, AjAttributeStruct struct, boolean allowIf) {
        try {
        	PatternParser parser = new PatternParser(pointcutString, struct.context);
            Pointcut pointcut = parser.parsePointcut();
            parser.checkEof();
            pointcut.check(null,struct.enclosingType.getWorld());
            if (!allowIf && pointcutString.indexOf("if()") >= 0 && hasIf(pointcut)) {
                reportError("if() pointcut is not allowed at this pointcut location '" + pointcutString +"'", struct);
                return null;
            }
            pointcut.setLocation(struct.context, -1, -1);//FIXME -1,-1 is not good enough
            return pointcut;
        } catch (ParserException e) {
            reportError("Invalid pointcut '" + pointcutString + "': " + e.toString() +
            		(e.getLocation()==null?"":" at position "+e.getLocation().getStart()), struct);
            return null;
        }
    }
    

    private static boolean hasIf(Pointcut pointcut) {
        IfFinder visitor = new IfFinder();
        pointcut.accept(visitor, null);
        return visitor.hasIf;
    }

    /**
     * Parse the given type pattern, return null on failure and issue an error
     *
     * @param patternString
     * @param location
     * @return type pattern
     */
    private static TypePattern parseTypePattern(String patternString, AjAttributeStruct location) {
        try {
            TypePattern typePattern = new PatternParser(patternString).parseTypePattern();
            typePattern.setLocation(location.context, -1, -1);//FIXME -1,-1 is not good enough
            return typePattern;
        } catch (ParserException e) {
            reportError("Invalid type pattern'" + patternString + "' : " + e.getLocation(), location);
            return null;
        }
    }

    static class ThrownFormalNotDeclaredInAdviceSignatureException extends Exception {

        private String formalName;

        public ThrownFormalNotDeclaredInAdviceSignatureException(String formalName) {
            this.formalName = formalName;
        }

        public String getFormalName() { return formalName; }
    }

    static class ReturningFormalNotDeclaredInAdviceSignatureException extends Exception {

        private String formalName;

        public ReturningFormalNotDeclaredInAdviceSignatureException(String formalName) {
            this.formalName = formalName;
        }

        public String getFormalName() { return formalName; }
    }
}
