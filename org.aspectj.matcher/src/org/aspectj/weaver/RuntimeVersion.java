/* *******************************************************************
 * Copyright (c) 2018 Contributors
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * ******************************************************************/
package org.aspectj.weaver;

/**
 * Captures important runtime versions. Typically new versions are added here if something
 * changes in the runtime and the code generation may be able to do something different
 * (more optimal) for a later runtime.
 * 
 * @author Andy Clement
 */
public enum RuntimeVersion {
	
	V1_2("1.2"), V1_5("1.5"), V1_6_10("1.6.10"), V1_9("1.9");
	
	private String[] aliases = null;

	RuntimeVersion(String... aliases) {
		this.aliases = aliases;
	}
	
	public static RuntimeVersion getVersionFor(String version) {
		for (RuntimeVersion candidateVersion: values()) {
			if (candidateVersion.name().equals(version)) {
				return candidateVersion;
			}
			if (candidateVersion.aliases != null) {
				for (String alias: candidateVersion.aliases) {
					if (alias.equals(version)) {
						return candidateVersion;
					}
				}
			}
		}
		return null;
	}

	public boolean isThisVersionOrLater(RuntimeVersion version) {
		return this.compareTo(version) >= 0;
	}
}
