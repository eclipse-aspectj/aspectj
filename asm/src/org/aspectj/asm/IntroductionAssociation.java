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
public class IntroductionAssociation implements Association {
    public static final String NAME = "Introduction";
    public static final Relation INTRODUCES_RELATION = new Relation("declares member on", "inter-type declared members", NAME, true, false);
    private List relations = new ArrayList();

    public IntroductionAssociation() {
        relations.add(INTRODUCES_RELATION);
    }

    public List getRelations() {
        return relations;
    }

    public List getRelationNodes() {
        List relations = new ArrayList();
        List introduces = new ArrayList();
//        Set forwardCorrs = StructureModelManager.correspondences.getAffects(astObject);
//        Set backCorrs = StructureModelManager.correspondences.getAffectedBy(astObject);
//        if (astObject instanceof IntroducedDec) {
//            for (Iterator it = forwardCorrs.iterator(); it.hasNext(); ) {
//                ASTObject node = (ASTObject)it.next();
//                LinkNode link = StructureNodeFactory.makeLink(node, false);
//                if (node instanceof TypeDec) {
//                    introduces.add(link);
//                }
//            }
//            if (!introduces.isEmpty()) relations.add(new RelationNode(INTRODUCES_RELATION, INTRODUCES_RELATION.getForwardNavigationName(), introduces));
//        } else {
//            for (Iterator it = backCorrs.iterator(); it.hasNext(); ) {
//                ASTObject node = (ASTObject)it.next();
//                if (astObject instanceof TypeDec) {
//                    if (node instanceof IntroducedDec) {
//                        introduces.add(StructureNodeFactory.makeLink(node, false));
//                    }
//                }
//            }
//            if (!introduces.isEmpty()) relations.add(new RelationNode(INTRODUCES_RELATION, INTRODUCES_RELATION.getBackNavigationName(), introduces));
//        }
        return relations;
    }

    public String getName() {
        return NAME;
    }
}
