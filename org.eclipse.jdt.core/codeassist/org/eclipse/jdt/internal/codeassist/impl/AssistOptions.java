/*******************************************************************************
 * Copyright (c) 2000, 2001, 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.internal.codeassist.impl;

import java.util.Map;

public class AssistOptions {
	/**
	 * Option IDs
	 */
	public static final String OPTION_PerformVisibilityCheck =
		"org.eclipse.jdt.core.codeComplete.visibilityCheck"; 	//$NON-NLS-1$
	public static final String OPTION_ForceImplicitQualification =
		"org.eclipse.jdt.core.codeComplete.forceImplicitQualification"; 	//$NON-NLS-1$
	public static final String ENABLED = "enabled"; //$NON-NLS-1$
	public static final String DISABLED = "disabled"; //$NON-NLS-1$

	public boolean checkVisibility = false;
	public boolean forceImplicitQualification = false;

	/** 
	 * Initializing the assist options with default settings
	 */
	public AssistOptions() {
	}

	/** 
	 * Initializing the assist options with external settings
	 */
	public AssistOptions(Map settings) {
		if (settings == null)
			return;

		// filter options which are related to the assist component
		Object[] entries = settings.entrySet().toArray();
		for (int i = 0, max = entries.length; i < max; i++) {
			Map.Entry entry = (Map.Entry) entries[i];
			if (!(entry.getKey() instanceof String))
				continue;
			if (!(entry.getValue() instanceof String))
				continue;
			String optionID = (String) entry.getKey();
			String optionValue = (String) entry.getValue();

			if (optionID.equals(OPTION_PerformVisibilityCheck)) {
				if (optionValue.equals(ENABLED)) {
					this.checkVisibility = true;
				} else
					if (optionValue.equals(DISABLED)) {
						this.checkVisibility = false;
					}
				continue;
			} else if (optionID.equals(OPTION_ForceImplicitQualification)) {
				if (optionValue.equals(ENABLED)) {
					this.forceImplicitQualification = true;
				} else
					if (optionValue.equals(DISABLED)) {
						this.forceImplicitQualification = false;
					}
				continue;
			} 
		}
	}
}