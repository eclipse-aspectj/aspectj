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


/**
 * @author Mik Kersten
 */
public class LinkNode extends StructureNode {
    
    private ProgramElementNode programElementNode = null;

    /**
     * Used during de-serialization.
     */
    public LinkNode() { }

	/**
	 * @param	node	can not be null
	 */
    public LinkNode(ProgramElementNode node) {
        super(node.getSignature().toString(), "internal", null);
        this.programElementNode = node;
    }

    public ProgramElementNode getProgramElementNode() {
        return programElementNode;
    }

    public String toString() {
        String name = "";
        if (programElementNode.getProgramElementKind().equals(ProgramElementNode.Kind.ADVICE) ||
            programElementNode.getProgramElementKind().equals(ProgramElementNode.Kind.INTRODUCTION) ||
            programElementNode.getProgramElementKind().equals(ProgramElementNode.Kind.CODE)) {
            name = programElementNode.parent.toString() + ": " + programElementNode.getName();
			StructureNode grandparent = programElementNode.parent.parent;
            if ( grandparent instanceof ProgramElementNode ) {
            	ProgramElementNode pe_grandparent = (ProgramElementNode)grandparent;
            	if ( pe_grandparent.getProgramElementKind().equals(ProgramElementNode.Kind.CLASS) ||
					 pe_grandparent.getProgramElementKind().equals(ProgramElementNode.Kind.INTERFACE)
            	    ) {
            		name = pe_grandparent.toString() + "." + name;	
            	}
            }
            
        } else if (programElementNode.isMemberKind()) {
            name = programElementNode.parent.toString() + '.' + programElementNode.getName(); 
        } else {
            name = programElementNode.toString();
        }
        return name;
    }
}
