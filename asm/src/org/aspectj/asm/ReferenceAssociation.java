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
public class ReferenceAssociation implements Association {
    public static final String NAME = "Reference";
    public static final Relation USES_POINTCUT_RELATION = new Relation("uses pointcut", "pointcut used by", NAME, true, true);
    public static final Relation IMPORTS_RELATION = new Relation("imports", NAME, false);
    //public static final Relation THROWS_RELATION = new Relation("throws", NAME, false);
    //public static final Relation USES_TYPE_RELATION = new Relation("uses type", NAME, false);

    private List relations = new ArrayList();

    public ReferenceAssociation() {
        relations.add(USES_POINTCUT_RELATION);
        relations.add(IMPORTS_RELATION);
        //relations.add(THROWS_RELATION);
        //relations.add(USES_TYPE_RELATION);
    }

    public List getRelations() {
        return relations;
    }

    public List getRelationNodes() {
        List relations = new ArrayList();
        List pointcutsUsed = new ArrayList();
        List pointcutUsedBy = new ArrayList();
        List throwsTypes = new ArrayList();
        List imports = new ArrayList();
        List usesType = new ArrayList();
//        Set forwardCorrs = StructureModelManager.correspondences.getAffects(astObject);
//        Set backCorrs = StructureModelManager.correspondences.getAffectedBy(astObject);
//
//        if (astObject instanceof AdviceDec) {
//            for (Iterator it = forwardCorrs.iterator(); it.hasNext(); ) {
//                ASTObject node = (ASTObject)it.next();
//                if (node instanceof PointcutDec) {
//                    pointcutsUsed.add(StructureNodeFactory.makeLink(node, false));
//                }
//            }
//        } else if (astObject instanceof PointcutDec) {
//            for (Iterator it = backCorrs.iterator(); it.hasNext(); ) {
//                ASTObject node = (ASTObject)it.next();
//                if (node instanceof PointcutDec || node instanceof AdviceDec) {
//                    pointcutUsedBy.add(StructureNodeFactory.makeLink(node, false));
//                }
//            }
//            for (Iterator it = forwardCorrs.iterator(); it.hasNext(); ) {
//                ASTObject node = (ASTObject)it.next();
//                if (node instanceof PointcutDec) {
//                    pointcutsUsed.add(StructureNodeFactory.makeLink(node, false));
//                }
//            }
//        } else if (astObject instanceof MethodDec) {
//            Method method = ((MethodDec)astObject).getMethod();
//            TypeDs throwsDs = method.getThrows();
//            if (throwsDs != null) {
//                for (Iterator it = throwsDs.iterator(); it.hasNext(); ) {
//                    Object o = it.next();
//                    if (o instanceof ResolvedTypeD) {
//                        ResolvedTypeD resolved = (ResolvedTypeD)o;
//                        if (resolved.getType().getCorrespondingDec() != null) {
//                            throwsTypes.add(StructureNodeFactory.makeLink(resolved.getType().getCorrespondingDec(), false));
//                        }
//                    }
//                }
//            }
//            if (!(method.getReturnType() instanceof PrimitiveType) && method.getReturnType().getCorrespondingDec() != null) {
//                    usesType.add(StructureNodeFactory.makeLink(method.getReturnType().getCorrespondingDec(), false));
//            }
//        } else if (astObject instanceof FieldDec) {
//            Field field = ((FieldDec)astObject).getField();
//            if (!(field.getFieldType() instanceof PrimitiveType) && field.getFieldType().getCorrespondingDec() != null) {
//                usesType.add(StructureNodeFactory.makeLink(field.getFieldType().getCorrespondingDec(), false));
//            }
//        }
//        else if (astObject instanceof CompilationUnit) {
//            CompilationUnit cu = (CompilationUnit)astObject;
//            org.aspectj.compiler.base.ast.Imports cuImports = cu.getImports();
//            for (int i = 0; cuImports != null && i < cuImports.getChildCount(); i++) {
//                Import imp = cuImports.get(i);
//                if (!imp.getStar() && imp != null && imp.getType() != null) {
//                    Type type = imp.getType();
//                    Dec dec = type.getCorrespondingDec();
//                    if (dec != null) imports.add(StructureNodeFactory.makeLink(dec, true));
//                }
//            }
//        }
//
//        if (!pointcutsUsed.isEmpty()) relations.add(new RelationNode(USES_POINTCUT_RELATION, USES_POINTCUT_RELATION.getForwardNavigationName(), pointcutsUsed));
//        if (!pointcutUsedBy.isEmpty()) relations.add(new RelationNode(USES_POINTCUT_RELATION, USES_POINTCUT_RELATION.getBackNavigationName(), pointcutUsedBy));
//        if (!imports.isEmpty()) relations.add(new RelationNode(IMPORTS_RELATION, IMPORTS_RELATION.getForwardNavigationName(), imports));
        return relations;
    }

    public String getName() {
        return NAME;
    }
}


//public class JavadocSeeAlso {
//
//    private static final String DOC =
//        "<b>Relates:</b> a declaration to another by the @seeAlso tag<br>" +
//        "<b>Symmetric: </b> yes";
//
//    public List makeLinks(ASTObject astObject, boolean forwardNavigation) {
//        List linkList = new Vector();
//        org.aspectj.compiler.base.ast.Comment comment = astObject.getComment();
//        try {
//            Object[] os = (Object[])comment.getClass().getMethod("seeTags", new Class[]{}).invoke(comment, new Object[]{});
//            for (int i = 0; i < os.length; i++) {
//                Object o = os[i];
//                Dec docDec = null;
//                TypeDec typeDec = (TypeDec)o.getClass().getMethod("referencedClass", new Class[]{}).invoke(o, new Object[]{});
//                Dec memberDec = (Dec)o.getClass().getMethod("referencedMember", new Class[]{}).invoke(o, new Object[]{});
//                if (memberDec != null) {
//                    docDec = memberDec;
//                } else if (typeDec != null) {
//                    docDec = typeDec;
//                }
//                if (docDec != null) {
//                    linkList.add(StructureNodeFactory.makeLink(docDec, false));
//
//                }
//            }
//        } catch (Throwable t) {
//            // ingore
//        }
//        return linkList;
//    }


