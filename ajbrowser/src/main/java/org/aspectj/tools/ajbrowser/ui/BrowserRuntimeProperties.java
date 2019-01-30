/********************************************************************
 * Copyright (c) 2007 Contributors. All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: IBM Corporation - initial API and implementation 
 * 				 Helen Hawkins   - initial version (bug 148190)
 *******************************************************************/
package org.aspectj.tools.ajbrowser.ui;

import org.aspectj.ajde.IRuntimeProperties;
import org.aspectj.ajde.ui.UserPreferencesAdapter;
import org.aspectj.tools.ajbrowser.core.PreferenceStoreConstants;

/**
 * AjBrowser implementation of IRuntimeProperties which uses the PreferenceStoreConstant
 * to decide which class the user has specified contains the main method
 */
public class BrowserRuntimeProperties implements IRuntimeProperties {

	private UserPreferencesAdapter preferencesAdapter;
	
	public BrowserRuntimeProperties(UserPreferencesAdapter preferencesAdapter) {
		this.preferencesAdapter = preferencesAdapter;
	}
	
	public String getClassToExecute() {
		return preferencesAdapter.getProjectPreference(PreferenceStoreConstants.RUNTIME_MAINCLASS);
	}

	public String getExecutionArgs() {
		// not implemented by ajbrowser
		return null;
	}

}
