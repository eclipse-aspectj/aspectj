/* -*- Mode: JDE; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 *
 * This file is part of the debugger and core tools for the AspectJ(tm)
 * programming language; see http://aspectj.org
 *
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * either http://www.mozilla.org/MPL/ or http://aspectj.org/MPL/.
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is AspectJ.
 *
 * The Initial Developer of the Original Code is Xerox Corporation. Portions
 * created by Xerox Corporation are Copyright (C) 1999-2002 Xerox Corporation.
 * All Rights Reserved.
 */
package org.aspectj.ajdoc;

/**
 * Represents an of clause describing an aspect.
 * This declaration also contains constants to be used to
 * identify different OfClauseDocs.
 *
 * @author Jeff Palm
 */
public interface OfClauseDoc {

    /**
     * A typesafe-enum describing the possible kinds
     * of OfClauseDocs.
     */
    public final static class Kind {

        /** Internal representation. */
        private final String kind;

        /**
         * Don't allow any other's to call this.
         *
         * @param kind The internal String representation.
         */
        private Kind(String kind) { this.kind = kind; }

        /**
         * Represents an 'of eachcflow(..)' clause.
         */
        public final static Kind EACH_CFLOW = new Kind("echocflow(..)");

        /**
         * Represents an 'of eachJVM()' clause.
         */
        public final static Kind EACH_JVM = new Kind("eachJVM()");
        
        /**
         * Represents an 'of eachobject(..)' clause.
         */
        public final static Kind EACH_OBJECT = new Kind("eachObject()");

        /**
         * Returns a short representation of the kind.
         *
         * @return a short representation of the kind.
         */
        public String toString() { return kind; }
    }

    /**
     * Returns the appropriate constant defined in Kind.
     *
     * @return the appropriate constant defined in Kind.
     * @see    Kind
     */
    public Kind kind();
}
