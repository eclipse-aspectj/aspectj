package ca.ubc.cs.spl.aspectPatterns.examples.bridge.java;

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
 * Prints capitalized text and uses double crosses ("#") to decorate.
 * Represents a <i>ConcreteImplementation</i> in the context of the Bridge
 * design pattern.
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 01/26/04
 *
 */

public class CrossCapitalImplementation implements ScreenImplementation {


        /**
         * Does a line feed (to <code>System.out</code>).
         */

        public void printLine() {
            System.out.println();
        }

        /**
         * Prints a double cross ("#") to <code>System.out</code>.
         */

        public void printDecor() {
            System.out.print("X");
        }

        /**
         * Prints the argument text in capitals to <code>System.out</code>.
         *
         * @param text the text to print
         */

        public void printText(String text) {
            System.out.print(text.toUpperCase());
        }
}
