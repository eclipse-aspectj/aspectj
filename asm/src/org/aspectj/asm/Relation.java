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

import java.io.Serializable;

/**
 * @author Mik Kersten
 */
public class Relation implements Serializable {

    private String forwardNavigationName;
    private String backNavigationName;
    private String associationName;
    private boolean symmetrical;
    private boolean transitive;

    public Relation(String forwardNavigationName,
                    String backNavigationName,
                    String associationName,
                    boolean symmetrical,
                    boolean transitive) {
        this.forwardNavigationName = forwardNavigationName;
        this.backNavigationName = backNavigationName;
        this.associationName = associationName;
        this.symmetrical = symmetrical;
        this.transitive = transitive;
    }

    /**
     * Constructor for asymetrical relations.
     */
    public Relation(String forwardNavigationName,
                    String associationName,
                    boolean transitive) {
        this(forwardNavigationName, "<no back navigation name>", associationName, false, transitive);
    }

    public String getForwardNavigationName() {
        return forwardNavigationName;
    }

    public String getBackNavigationName() {
        return backNavigationName;
    }

    public String getAssociationName() {
        return associationName;
    }

    public boolean isSymmetrical() {
        return symmetrical;
    }

    public boolean isTransitive() {
        return transitive;
    }

	public boolean equals(Object o) {
		if (!(o instanceof Relation)) return false;
		Relation r = (Relation)o;
		return forwardNavigationName.equals(r.getForwardNavigationName())
			&& backNavigationName.equals(r.getBackNavigationName())
			&& associationName.equals(r.getAssociationName())
			&& (symmetrical == r.isSymmetrical())
			&& (transitive == r.isTransitive());
	}

    public String toString() {
        if (symmetrical) {
            return forwardNavigationName + " / " + backNavigationName;
        } else {
            return forwardNavigationName;
        }
    }
}
