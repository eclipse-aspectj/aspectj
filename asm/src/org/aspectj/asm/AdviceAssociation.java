/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.asm;

import java.util.*;
import org.aspectj.asm.*;

/**
 * @author Mik Kersten
 */
public class AdviceAssociation implements Association {
	
    public static final String NAME = "Advice";
    public static final Relation METHOD_RELATION = new Relation("advises methods", "method advised by", NAME, true, false);
    public static final Relation METHOD_CALL_SITE_RELATION = new Relation("advises method call sites", "method call site advised by", NAME, true, false);
    public static final Relation CONSTRUCTOR_RELATION = new Relation("advises constructors", "constructors advised by", NAME, true, false);
    public static final Relation CONSTRUCTOR_CALL_SITE_RELATION = new Relation("advises constructions", "construction advised by", NAME, true, false);
    public static final Relation HANDLER_RELATION = new Relation("advises exception handlers", "exception handler advised by", NAME, true, false);
    public static final Relation INITIALIZER_RELATION = new Relation("advises initializers", "initializers advised by", NAME, true, false);
    public static final Relation FIELD_ACCESS_RELATION = new Relation("advises field access", "field access advised by", NAME, true, false);
    public static final Relation INTRODUCTION_RELATION = new Relation("advises introduction", "introduction advised by", NAME, true, false);

    private List relations = new ArrayList();

    public AdviceAssociation() {
        relations.add(METHOD_RELATION);
        relations.add(METHOD_CALL_SITE_RELATION);
        relations.add(CONSTRUCTOR_RELATION);
        relations.add(CONSTRUCTOR_CALL_SITE_RELATION);
        relations.add(HANDLER_RELATION);
        relations.add(INITIALIZER_RELATION);
        relations.add(FIELD_ACCESS_RELATION);
        relations.add(INTRODUCTION_RELATION);
    }

    public List getRelations() {
        return relations;
    }

    public List getRelationNodes() {
        List relations = new ArrayList();
        List methods = new ArrayList();
        List methodCallSites = new ArrayList();
        List constructors = new ArrayList();
        List constructorCallSites = new ArrayList();
        List handlers = new ArrayList();
        List initializers = new ArrayList();
        List fieldAccesses = new ArrayList();
        List introductions = new ArrayList();
//        Set forwardCorrs = StructureModelManager.correspondences.getAffects(astObject);
//        Set backCorrs = StructureModelManager.correspondences.getAffectedBy(astObject);
//
//        if (astObject instanceof AdviceDec) {
//            for (Iterator it = forwardCorrs.iterator(); it.hasNext(); ) {
//                ASTObject node = (ASTObject)it.next();
//                LinkNode link = StructureNodeFactory.makeLink(node, false);
//                if (node instanceof MethodDec) {
//                    if (((MethodDec)node).isSynthetic()) {
//                        ASTObject resolvedNode = resolveSyntheticMethodToIntroduction((MethodDec)node);
//                        introductions.add(StructureNodeFactory.makeLink(resolvedNode, false));
//                    } else {
//                        methods.add(link);
//                    }
//                } else if (node instanceof CallExpr) {
//                    methodCallSites.add(link);
//                } else if (node instanceof ConstructorDec) {
//                    constructors.add(link);
//                } else if (node instanceof NewInstanceExpr) {
//                    constructorCallSites.add(link);
//                } else if (node instanceof CatchClause) {
//                    handlers.add(link);
//                } else if (node instanceof InitializerDec) {
//                    initializers.add(link);
//                } else if (node instanceof FieldDec) {
//                    fieldAccesses.add(link);
//                } else if (node instanceof BasicAssignExpr || node instanceof FieldAccessExpr) {
//                	fieldAccesses.add(link);	
//                } 
//            }
//            if (!methods.isEmpty()) relations.add(new RelationNode(METHOD_RELATION, METHOD_RELATION.getForwardNavigationName(), methods));
//            if (!methodCallSites.isEmpty()) relations.add(new RelationNode(METHOD_RELATION, METHOD_CALL_SITE_RELATION.getForwardNavigationName(), methodCallSites));
//            if (!constructors.isEmpty()) relations.add(new RelationNode(CONSTRUCTOR_RELATION, CONSTRUCTOR_RELATION.getForwardNavigationName(), constructors));
//            if (!constructorCallSites.isEmpty()) relations.add(new RelationNode(CONSTRUCTOR_CALL_SITE_RELATION, CONSTRUCTOR_CALL_SITE_RELATION.getForwardNavigationName(), constructorCallSites));
//            if (!handlers.isEmpty()) relations.add(new RelationNode(HANDLER_RELATION, HANDLER_RELATION.getForwardNavigationName(), handlers));
//            if (!initializers.isEmpty()) relations.add(new RelationNode(INITIALIZER_RELATION, INITIALIZER_RELATION.getForwardNavigationName(), initializers));
//            if (!fieldAccesses.isEmpty()) relations.add(new RelationNode(FIELD_ACCESS_RELATION, FIELD_ACCESS_RELATION.getForwardNavigationName(), fieldAccesses));
//            if (!introductions.isEmpty()) relations.add(new RelationNode(INTRODUCTION_RELATION, INTRODUCTION_RELATION.getForwardNavigationName(), introductions));
//        } else {
//            if (astObject instanceof IntroducedDec) {
//                Set adviceDecs = resolveAdviceAffectingIntroduction((IntroducedDec)astObject);
//                if (adviceDecs != null) {
//                    for (Iterator adIt = adviceDecs.iterator(); adIt.hasNext(); ) {
//                        introductions.add(StructureNodeFactory.makeLink((ASTObject)adIt.next(), false));
//                    }
//                }
//            }
//
//            for (Iterator it = backCorrs.iterator(); it.hasNext(); ) {
//                ASTObject node = (ASTObject)it.next();
//                if (node instanceof AdviceDec) {
//                    if (astObject instanceof MethodDec) {
//                        methods.add(StructureNodeFactory.makeLink(node, false));
//                    } else if (astObject instanceof CallExpr) {
//                        methodCallSites.add(StructureNodeFactory.makeLink(node, false));
//                    } else if (astObject instanceof ConstructorDec) {
//                        constructors.add(StructureNodeFactory.makeLink(node, false));
//                    } else if (astObject instanceof NewInstanceExpr) {
//                        constructorCallSites.add(StructureNodeFactory.makeLink(node, false));
//                    } else if (astObject instanceof CatchClause) {
//                        handlers.add(StructureNodeFactory.makeLink(node, false));
//                    } else if (astObject instanceof InitializerDec) {
//                        initializers.add(StructureNodeFactory.makeLink(node, false));
//                    } else if (astObject instanceof FieldDec) {
//                        fieldAccesses.add(StructureNodeFactory.makeLink(node, false));
//                    } else if (astObject instanceof BasicAssignExpr 
//                   		|| astObject instanceof FieldAccessExpr) {
//                    	fieldAccesses.add(StructureNodeFactory.makeLink(node, false));
//                    }
//                }
//            }
//            if (!methods.isEmpty()) relations.add(new RelationNode(METHOD_RELATION, METHOD_RELATION.getBackNavigationName(), methods));
//            if (!methodCallSites.isEmpty()) relations.add(new RelationNode(METHOD_CALL_SITE_RELATION, METHOD_CALL_SITE_RELATION.getBackNavigationName(), methodCallSites));
//            if (!constructors.isEmpty()) relations.add(new RelationNode(CONSTRUCTOR_RELATION, CONSTRUCTOR_RELATION.getBackNavigationName(), constructors));
//            if (!constructorCallSites.isEmpty()) relations.add(new RelationNode(CONSTRUCTOR_CALL_SITE_RELATION, CONSTRUCTOR_CALL_SITE_RELATION.getBackNavigationName(), constructorCallSites));
//            if (!handlers.isEmpty()) relations.add(new RelationNode(HANDLER_RELATION, HANDLER_RELATION.getBackNavigationName(), handlers));
//            if (!initializers.isEmpty()) relations.add(new RelationNode(INITIALIZER_RELATION, INITIALIZER_RELATION.getBackNavigationName(), initializers));
//            if (!fieldAccesses.isEmpty()) relations.add(new RelationNode(FIELD_ACCESS_RELATION, FIELD_ACCESS_RELATION.getBackNavigationName(), fieldAccesses));
//            if (!introductions.isEmpty()) relations.add(new RelationNode(INTRODUCTION_RELATION, INTRODUCTION_RELATION.getBackNavigationName(), introductions));
//        }
        return relations;
    }

    public String getName() {
        return NAME;
    }

//    /**
//     * @todo    HACK: this search and hacked name-match should be replace by a fix to the correspondeces db
//     */
//    private ASTObject resolveSyntheticMethodToIntroduction(MethodDec node) {
//        Set backCorrs = StructureModelManager.correspondences.getAffectedBy(node.getDeclaringType().getTypeDec());
//        Method method = node.getMethod();
//        String name = method.getDeclaringType().getName() + '.' + method.getName();
//        for (Iterator it = backCorrs.iterator(); it.hasNext(); ) {
//            Object next = it.next();
//            if (next instanceof IntroducedDec) {
//                IntroducedDec introducedDec = (IntroducedDec)next;
//                if (name.equals(introducedDec.toShortString())) return introducedDec;
//            }
//        }
//        return node;
//    }

//    /**
//     * @todo    HACK: this search and hacked name-match should be replace by a fix to the correspondeces db
//     */
//    private Set resolveAdviceAffectingIntroduction(IntroducedDec node) {
//        Set forwardCorrs = StructureModelManager.correspondences.getAffects(node);
//        String name = node.getId();
//        for (Iterator it = forwardCorrs.iterator(); it.hasNext(); ) {
//            Object next = it.next();
//            if (next instanceof TypeDec) {
//                TypeDec typeDec = (TypeDec)next;
//                List decs = typeDec.getBody().getList();
//                for (Iterator it2 = decs.iterator(); it2.hasNext(); ) {
//                    Dec bodyDec = (Dec)it2.next();
//                    if (bodyDec != null && !(bodyDec instanceof InitializerDec)) {
//                        if (bodyDec instanceof MethodDec && bodyDec.isSynthetic() && name.equals(bodyDec.getName())) {
//                            MethodDec methodDec = (MethodDec)bodyDec;
//                            return StructureModelManager.correspondences.getAffectedBy(methodDec);
//                        }
//                    }
//                }
//            }
//        }
//        return null;
//    }
}

