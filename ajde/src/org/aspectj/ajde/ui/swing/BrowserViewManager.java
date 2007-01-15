/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation
 *     Helen Hawkins  Converted to new interface (bug 148190)  
 * ******************************************************************/


package org.aspectj.ajde.ui.swing;

import java.util.ArrayList;

import org.aspectj.ajde.Ajde;
import org.aspectj.ajde.ui.GlobalStructureView;
import org.aspectj.ajde.ui.GlobalViewProperties;
import org.aspectj.ajde.ui.StructureViewProperties;

/**
 * Responsible for displaying and controlling the configuration and output of a
 * master and slave structure view.
 *
 * @author Mik Kersten
 */
public class BrowserViewManager {

	private StructureViewPanel browserPanel = null;
//    private boolean globalMode = true;
//    private boolean splitViewMode = false;
//    private IconRegistry icons;
//  
//    private Stack backHistory = new Stack();
//    private Stack forwardHistory = new Stack();
//    private IProgramElement currNode = null;

	private final GlobalStructureView DECLARATION_VIEW;
	private final GlobalStructureView CROSSCUTTING_VIEW;
	private final GlobalStructureView INHERITANCE_VIEW;

    private final GlobalViewProperties DECLARATION_VIEW_PROPERTIES;
    private final GlobalViewProperties CROSSCUTTING_VIEW_PROPERTIES;
    private final GlobalViewProperties INHERITANCE_VIEW_PROPERTIES;

    public BrowserViewManager() {
		java.util.List views = new ArrayList();
		views.add(DECLARATION_VIEW);
		views.add(CROSSCUTTING_VIEW);
		views.add(INHERITANCE_VIEW);
		browserPanel = new StructureViewPanel(views);
    }

    public StructureViewPanel getBrowserPanel() {
        return browserPanel;
    }

    public void extractAndInsertSignatures(java.util.List signatures, boolean calls) {
        PointcutWizard pointcutWizard = new PointcutWizard(signatures);
        pointcutWizard.setVisible(true);
        pointcutWizard.setLocation(Ajde.getDefault().getRootFrame().getX()+100, Ajde.getDefault().getRootFrame().getY()+100);
    }
    
    {   
    	DECLARATION_VIEW_PROPERTIES = new GlobalViewProperties(StructureViewProperties.Hierarchy.DECLARATION);
   		CROSSCUTTING_VIEW_PROPERTIES = new GlobalViewProperties(StructureViewProperties.Hierarchy.CROSSCUTTING);
    	INHERITANCE_VIEW_PROPERTIES = new GlobalViewProperties(StructureViewProperties.Hierarchy.INHERITANCE);  
    
//        CROSSCUTTING_VIEW_PROPERTIES.addRelation(IRelationship.Kind.ADVICE);
//		CROSSCUTTING_VIEW_PROPERTIES.addRelation(IRelationship.Kind.ADVICE);
//		CROSSCUTTING_VIEW_PROPERTIES.addRelation(IRelationship.Kind.ADVICE);
//        CROSSCUTTING_VIEW_PROPERTIES.addRelation(AdviceAssociation.METHOD_CALL_SITE_RELATION);
//        CROSSCUTTING_VIEW_PROPERTIES.addRelation(AdviceAssociation.CONSTRUCTOR_RELATION);
//        CROSSCUTTING_VIEW_PROPERTIES.addRelation(AdviceAssociation.CONSTRUCTOR_CALL_SITE_RELATION);
//        CROSSCUTTING_VIEW_PROPERTIES.addRelation(AdviceAssociation.HANDLER_RELATION);
//        CROSSCUTTING_VIEW_PROPERTIES.addRelation(AdviceAssociation.INITIALIZER_RELATION);
//        CROSSCUTTING_VIEW_PROPERTIES.addRelation(AdviceAssociation.FIELD_ACCESS_RELATION);
//
//        INHERITANCE_VIEW_PROPERTIES.addRelation(InheritanceAssociation.IMPLEMENTS_RELATION);
//        INHERITANCE_VIEW_PROPERTIES.addRelation(InheritanceAssociation.INHERITS_MEMBERS_RELATION);
//        INHERITANCE_VIEW_PROPERTIES.addRelation(InheritanceAssociation.INHERITS_RELATION);
     	
     	DECLARATION_VIEW_PROPERTIES.setRelations(Ajde.getDefault().getStructureViewManager().getAvailableRelations());

        CROSSCUTTING_VIEW = Ajde.getDefault().getStructureViewManager().createGlobalView(CROSSCUTTING_VIEW_PROPERTIES);
        INHERITANCE_VIEW = Ajde.getDefault().getStructureViewManager().createGlobalView(INHERITANCE_VIEW_PROPERTIES);
    	DECLARATION_VIEW = Ajde.getDefault().getStructureViewManager().createGlobalView(DECLARATION_VIEW_PROPERTIES);
    }
}


