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
import org.aspectj.ajde.ui.UserPreferencesAdapter;

public class UserPreferencesStore implements UserPreferencesAdapter {
    public static final String FILE_NAME = "/.ajbrowser";
    private final String VALUE_SEP = ";";
    private Properties properties = new Properties();
	
	public UserPreferencesStore() {
        try {
            if (new File(getPropertiesFilePath()).exists()) {
                properties.load(new FileInputStream(getPropertiesFilePath()));
            }
        } catch (IOException ioe) {
            Ajde.getDefault().getErrorHandler().handleError("Could not read properties", ioe);
        }	
	}
	
    public String getProjectPreference(String name) {
        return properties.getProperty(name);
    }

    public List getProjectMultivalPreference(String name) {
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

    public void setProjectPreference(String name, String value) {
        properties.setProperty(name, value);
        saveProperties();
    }

    public void setProjectMultivalPreference(String name, List values) {
        String valuesString = "";
        for (Iterator it = values.iterator(); it.hasNext(); ) {
            valuesString += (String)it.next() + ';';
        }
        properties.setProperty(name, valuesString);
        saveProperties();
    }

    public String getPropertiesFilePath() {
        String path = System.getProperty("user.home");
        if (path == null) {
            path = ".";
        }
        return path + FILE_NAME;
    }
    
	public String getGlobalPreference(String name) {
		return getProjectPreference(name);
	}
	
	public List getGlobalMultivalPreference(String name) {
		return getProjectMultivalPreference(name);
	}
	
	public void setGlobalPreference(String name, String value) {
		setProjectPreference(name, value);
	}
	
	public void setGlobalMultivalPreference(String name, List values) {
		setProjectMultivalPreference(name, values);
	}
	
    public void saveProperties() {
        try {
            properties.store(new FileOutputStream(getPropertiesFilePath()), "AJDE Settings");
        } catch (IOException ioe) {
            Ajde.getDefault().getErrorHandler().handleError("Could not write properties", ioe);
        }
    }
}
