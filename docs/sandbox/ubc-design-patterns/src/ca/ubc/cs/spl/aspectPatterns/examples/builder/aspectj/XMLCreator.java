package ca.ubc.cs.spl.aspectPatterns.examples.builder.aspectj;

/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 *
 * This file is part of the design patterns project at UBC
 *
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * either https://www.mozilla.org/MPL/ or https://aspectj.org/MPL/.
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is ca.ubc.cs.spl.aspectPatterns.
 *
 * For more details and the latest version of this code, please see:
 * https://www.cs.ubc.ca/labs/spl/projects/aodps.html
 *
 * Contributor(s):
 */

/**
 * Implements a <i>ConcreteBuilder</i> that builds XML descriptions
 * of complex objects consisting of type, attributes and values.
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 01/26/04
 */

public class XMLCreator implements Creator {

    protected String type = null;
    protected String attribute = null;

    /**
     * Defines a <i>buildPart()</i> operation for type parts.
     *
     * @param newType the type to process
     */

    public void processType(String newType) {
		representation = "<"+newType+">\n";
        type = newType;
    }

    /**
     * Defines a <i>buildPart()</i> operation for attribute parts.
     *
     * @param newAttribute the attribute to process
     */

    public void processAttribute(String newAttribute) {
        checkAttribute();
		representation += ("\t<" + newAttribute + ">");
        this.attribute = newAttribute;
    }

    /**
     * Defines a <i>buildPart()</i> operation for value parts.
     *
     * @param newValue the type to process
     */

    public void processValue(String newValue) {
		representation += newValue;
    }

    /**
     * Checks wether the opening type tag is closed and closes it if not.
     */

    protected void checkType() {
        if (type != null) {
			representation += ("</" + type + ">\n");
            type = null;
        }
    }

    /**
     * Checks wether the opening attribute tag is closed and closes it if not.
     */

    protected void checkAttribute() {
        if (attribute != null) {
			representation += ("</" + attribute + ">\n");
            attribute = null;
        }
    }

    /**
     * Defines the <i>getResult()</i> operation for <i>Builder</i>s.
     */

    public String getRepresentation() {
        checkAttribute();
        checkType();
        return representation;
    }
}
