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
 * ******************************************************************/

 
 
package org.aspectj.ajde.ui.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import org.aspectj.ajde.Ajde;

/**
 * @deprecated	this class should be removed from the source tree
 */
public class BrowserViewProperties {
    public static final String FILE_NAME = "/.ajbrowser";
    private final String VALUE_SEP = ";";
    private Properties properties = new Properties();

    public BrowserViewProperties() {
        try {
            if (new File(getPropertiesFilePath()).exists()) {
                properties.load(new FileInputStream(getPropertiesFilePath()));
            }
        } catch (IOException ioe) {
            Ajde.getDefault().getErrorHandler().handleError("Could not read properties", ioe);
        }
    }
	
    public List getStructureViewOrdering() {
        return getPropertyValues("structureView.ordering");
    }

    public void setStructureViewOrdering(List ordering) {
        storeProperty("structureView.ordering", ordering);
    }

//    /**
//     * Returns default values if no associations are saved.
//     */
//    public List getActiveAssociations() {
//        List associations = getPropertyValues("structureView.associations");
//        if (associations.size() == 0) {
//            associations.add(Advice.METHOD_CALL_SITE_RELATION.toString());
//            associations.add(Advice.METHOD_RELATION.toString());
//            associations.add(Advice.CONSTRUCTOR_CALL_SITE_RELATION.toString());
//            associations.add(Advice.CONSTRUCTOR_RELATION.toString());
//            associations.add(Advice.FIELD_ACCESS_RELATION.toString());
//            associations.add(Advice.INITIALIZER_RELATION.toString());
//            associations.add(Advice.HANDLER_RELATION.toString());
//            associations.add(Advice.INTRODUCTION_RELATION.toString());
//            associations.add(Introduction.INTRODUCES_RELATION.toString());
//            associations.add(Inheritance.IMPLEMENTS_RELATION.toString());
//            associations.add(Inheritance.INHERITS_RELATION.toString());
//            associations.add(Inheritance.INHERITS_MEMBERS_RELATION.toString());
//            associations.add(Reference.USES_POINTCUT_RELATION.toString());
//            associations.add(Reference.IMPORTS_RELATION.toString());
//        }
//        return associations;
//    }

    public void setActiveAssociations(List associations) {
        storeProperty("structureView.associations", associations);
    }

    public void setActiveFilteredMemberKinds(List associations) {
        storeProperty("structureView.filtering.memberKinds", associations);
    }

    public String getActiveHierarchy() {
       return getProperty("structureView.hierarchy");
    }

    public void setActiveHierarchy(String hierarchy) {
        storeProperty("structureView.hierarchy", hierarchy);
    }

    public List getActiveVisibility() {
        return getPropertyValues("structureView.filtering.accessibility");
    }

    public void setActiveVisiblity(List visibility) {
        storeProperty("structureView.filtering.accessibility", visibility);
    }

    public List getActiveModifiers() {
        return getPropertyValues("structureView.filtering.modifiers");
    }

    public List getActiveFilteredMemberKinds() {
        return getPropertyValues("structureView.filtering.memberKinds");
    }

    public String getActiveGranularity() {
        return getProperty("structureView.granularity");
    }

    public void setActiveGranularity(String granularity) {
    storeProperty("structureView.granularity", granularity);
    }

    public void setActiveModifiers(List modifiers) {
        storeProperty("structureView.filtering.modifiers", modifiers);
    }

    protected String getProperty(String name) {
        return properties.getProperty(name);
    }

    protected List getPropertyValues(String name) {
        List values = new ArrayList();
        String valuesString = properties.getProperty(name);
        if (valuesString != null && !valuesString.trim().equals("")) {
            StringTokenizer st = new StringTokenizer(valuesString, VALUE_SEP);
            while (st.hasMoreTokens()) {
                values.add(st.nextToken());
            }
        }
        return values;
    }

    private void storeProperty(String name, String value) {
        properties.setProperty(name, value);
        saveProperties();
    }

    private void storeProperty(String name, List values) {
        String valuesString = "";
        for (Iterator it = values.iterator(); it.hasNext(); ) {
            valuesString += (String)it.next() + ';';
        }
        properties.setProperty(name, valuesString);
        saveProperties();
    }

    private void saveProperties() {
        try {
            properties.store(new FileOutputStream(getPropertiesFilePath()), "AJDE Settings");
        } catch (IOException ioe) {
            Ajde.getDefault().getErrorHandler().handleError("Could not write properties", ioe);
        }
    }

    protected String getPropertiesFilePath() {
        String path = System.getProperty("user.home");
        if (path == null) {
            path = ".";
        }
        return path + FILE_NAME;
    }
}
