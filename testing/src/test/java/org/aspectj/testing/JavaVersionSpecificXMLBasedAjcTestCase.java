/* *******************************************************************
 * Copyright (c) 2024 Contributors
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 * ******************************************************************/
package org.aspectj.testing;

import static org.aspectj.util.LangUtil.isVMGreaterOrEqual;
import static org.aspectj.util.LangUtil.isVMLessOrEqual;

/**
 * A test case which only runs on specific Java versions
 *
 * @author Alexander Kriegisch
 */
public abstract class JavaVersionSpecificXMLBasedAjcTestCase extends XMLBasedAjcTestCase {
	private final int minimumJavaVersion;
	private final int maximumJavaVersion;

	protected JavaVersionSpecificXMLBasedAjcTestCase(int minimumJavaVersion) {
		this(minimumJavaVersion, Integer.MAX_VALUE);
	}

	protected JavaVersionSpecificXMLBasedAjcTestCase(int minimumJavaVersion, int maximumJavaVersion) {
		this.minimumJavaVersion = minimumJavaVersion;
		this.maximumJavaVersion = maximumJavaVersion;
	}

	@Override
	public void setUp() throws Exception {
		boolean withinBounds = isVMGreaterOrEqual(minimumJavaVersion) && isVMLessOrEqual(maximumJavaVersion);
		if (!withinBounds) {
			String errorMessage = "These tests must run on Java version ";
			if (maximumJavaVersion == Integer.MAX_VALUE)
				errorMessage += minimumJavaVersion + " or greater";
			else if (maximumJavaVersion == minimumJavaVersion)
				errorMessage += minimumJavaVersion + " only";
			else
				errorMessage += "range " + minimumJavaVersion + " to " + maximumJavaVersion;
			throw new IllegalStateException(errorMessage);
		}
		super.setUp();
	}

}
