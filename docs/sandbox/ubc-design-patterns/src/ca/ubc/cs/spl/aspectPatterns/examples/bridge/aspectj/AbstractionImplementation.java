package ca.ubc.cs.spl.aspectPatterns.examples.bridge.aspectj;

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
 * Represents the <i>Abstraction</i> in the scenario. <code>Screen</code>
 * provides two methods to draw/print: <code>drawText(String)</code> and
 * <code>drawTextBox(String)</code>. The method implementations are provided
 * by this aspect.
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 01/26/04
 *
 */

public aspect AbstractionImplementation {

    /**
     * stores the actual <i>Implementor</i> to use
     */

    private ScreenImplementation Screen.implementor;

    /**
     * Draws or prints a text to an output device determined by the
     * current <i>Implementor</i>.
     *
     * @param text The text to be drawn/printed
     */

    public void Screen.drawText(String text) {
        implementor.printText(text);
        implementor.printLine();
    }

    /**
     * Draws or prints a text in a box to an output device determined
     * by the current <i>Implementor</i>.
     *
     * @param text The text to be drawn/printed
     */

    public void Screen.drawTextBox(String text) {

        int length = text.length();

        for(int i=0; i<length+4; i++) {
            implementor.printDecor();
        }

        implementor.printLine();
        implementor.printDecor();
        implementor.printText(" "+text+" ");
        implementor.printDecor();
        implementor.printLine();

        for(int i=0; i<length+4; i++) {
            implementor.printDecor();
        }

        implementor.printLine();
    }

    /**
     * Sets the current <i>Implementor</i>.
     *
     * @param implementor The new implementor
     */

    public void Screen.setImplementor(ScreenImplementation implementor) {
        this.implementor = implementor;
    }

}
