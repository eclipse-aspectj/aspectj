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
public class InheritanceAssociation implements Association {
    public static final String NAME = "Inheritance";
    public static final Relation INHERITS_RELATION = new Relation("inherits", "is inherited by", NAME, true, true);
    public static final Relation IMPLEMENTS_RELATION = new Relation("implements", "is implemented by", NAME, true, true);
    public static final Relation INHERITS_MEMBERS_RELATION = new Relation("inherits members", NAME, false);
    private List relations = new ArrayList();

    public InheritanceAssociation() {
        relations.add(INHERITS_RELATION);
        relations.add(IMPLEMENTS_RELATION);
        relations.add(INHERITS_MEMBERS_RELATION);
    }

    public List getRelations() {
        return relations;
    }

    public List getRelationNodes() {
        List relations = new ArrayList();
//        if (astObject instanceof TypeDec) {
//            TypeDec typeDec = (TypeDec)astObject;
//            boolean isInterface = (astObject instanceof InterfaceDec);
//            List superTypes = getTypeLinks(typeDec.getType().getDirectSuperTypes(), true, isInterface);
//            List subTypes = getTypeLinks(typeDec.getType().getDirectSubTypes(), true, isInterface);
//            if (!superTypes.isEmpty()) relations.add(new RelationNode(INHERITS_RELATION, INHERITS_RELATION.getForwardNavigationName(), superTypes));
//            if (!subTypes.isEmpty()) relations.add(new RelationNode(INHERITS_RELATION, INHERITS_RELATION.getBackNavigationName(), subTypes));
//
//            List implementedInterfaces = getTypeLinks(typeDec.getType().getDirectSuperTypes(), false, isInterface);
//            List implementors = getTypeLinks(typeDec.getType().getDirectSubTypes(), false, isInterface);
//            if (!implementedInterfaces.isEmpty()) relations.add(new RelationNode(IMPLEMENTS_RELATION, IMPLEMENTS_RELATION.getForwardNavigationName(), implementedInterfaces));
//            if (!implementors.isEmpty()) relations.add(new RelationNode(IMPLEMENTS_RELATION, IMPLEMENTS_RELATION.getBackNavigationName(), implementors));
//
//            List inheritedMembers = new ArrayList(getMemberLinks(typeDec.getType().getInheritedMethods()));
//            if (!inheritedMembers.isEmpty()) relations.add(new RelationNode(INHERITS_MEMBERS_RELATION, INHERITS_MEMBERS_RELATION.getForwardNavigationName(), inheritedMembers));
//        }
        return relations;
    }

//    private List getTypeLinks(Collection types, boolean isInheritance, boolean isInterface) {
//        List links = new ArrayList();
//        if (types != null) {
//            for (Iterator it = types.iterator(); it.hasNext(); ) {
//                NameType nameType = (NameType)it.next();
//                if (!nameType.getId().equals("Object")) {
//                    if (isInheritance && ((isInterface && nameType.isInterface()) || (!isInterface && !nameType.isInterface()))
//                        || !isInheritance && (!isInterface && nameType.isInterface())) {
//                        Dec dec = nameType.getCorrespondingDec();
//                        links.add(StructureNodeFactory.makeLink(dec, false));
//                    }
//                }
//            }
//        }
//        return links;
//    }

//    private List getMemberLinks(Collection members) {
//        List links = new ArrayList();
//        if (members != null) {
//            for (Iterator it = members.iterator(); it.hasNext(); ) {
//                Object object = it.next();
//                if (object instanceof Method) {
//                    Method method = (Method)object;
//                    if (!method.getDeclaringType().getId().equals("Object")) {
//                        links.add(StructureNodeFactory.makeLink(method.getCorrespondingDec(), false));
//                    }
//                } else if (object instanceof Field) {
//                    Field field = (Field)object;
//                    if (!field.getDeclaringType().getId().equals("Object")) {
//                        links.add(StructureNodeFactory.makeLink(field.getCorrespondingDec(), false));
//                    }
//                }
//            }
//        }
//        return links;
//    }

    public String getName() {
        return NAME;
    }
}
