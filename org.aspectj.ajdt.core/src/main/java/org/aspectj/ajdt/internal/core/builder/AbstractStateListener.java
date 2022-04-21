/**
 * Copyright (c) 2005 IBM and other contributors
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *     Andy Clement     initial implementation
 * ******************************************************************/
package org.aspectj.ajdt.internal.core.builder;

import java.io.File;
import java.util.List;

/**
 * Subtypes can override whatever they want...
 *
 * @author AndyClement
 *
 */
public abstract class AbstractStateListener implements IStateListener {

	public void detectedClassChangeInThisDir(File f) {	}

	public void aboutToCompareClasspaths(List<String> oldClasspath, List<String> newClasspath) {	}

	public void pathChangeDetected() {	}

	public void detectedAspectDeleted(File f) {	}

	public void buildSuccessful(boolean wasFullBuild) {	}

	public void recordDecision(String decision) {}

	public void recordInformation(String info) {}

}
