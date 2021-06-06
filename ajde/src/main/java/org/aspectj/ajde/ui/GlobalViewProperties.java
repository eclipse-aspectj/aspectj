/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation,
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *     Xerox/PARC     initial implementation
 * ******************************************************************/


package org.aspectj.ajde.ui;

/**
 * @author Mik Kersten
 */
public class GlobalViewProperties extends StructureViewProperties {

    private StructureViewProperties.Granularity granularity = StructureViewProperties.Granularity.DECLARED_ELEMENTS;
    private StructureViewProperties.Hierarchy hierarchy = StructureViewProperties.Hierarchy.DECLARATION;

	public GlobalViewProperties(StructureViewProperties.Hierarchy hierarchy) {
		this.hierarchy = hierarchy;
	}

	public void setGranularity(StructureViewProperties.Granularity granularity) {
        this.granularity = granularity;
    }

    public StructureViewProperties.Granularity getGranularity() {
        return granularity;
    }

    public void setHierarchy(StructureViewProperties.Hierarchy hierarchy) {
    	this.hierarchy = hierarchy;
    }

	public StructureViewProperties.Hierarchy getHierarchy() {
		return hierarchy;
	}

    public String getName() {
        return hierarchy.toString();
    }
}

