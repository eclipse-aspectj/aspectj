package ca.ubc.cs.spl.aspectPatterns.examples.factoryMethod.aspectj;

/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 *
 * This file is part of the design patterns project at UBC
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
 * The Original Code is ca.ubc.cs.spl.aspectPatterns.
 * 
 * For more details and the latest version of this code, please see:
 * http://www.cs.ubc.ca/labs/spl/projects/aodps.html
 *
 * Contributor(s):   
 */

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JComponent; 
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;  
import java.awt.Point;

/**
 * Provides a default implementation for the <i>anOperation()</i>
 * method <code>showFrame()</code>. The implementation is attached to the 
 * <code>GUIComponentCreator</code> interface. With this approach, 
 * <i>GUIComponentCreator</i> does not have to be an abstract class.
 * 
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 02/11/04
 * 
 * @see GUIComponentCreator
 */ 

public aspect CreatorImplementation {
    
    /** 
     * the position for the next frame to be created (on the screen)
     */
     
    private static Point lastFrameLocation = new Point(0, 0);

    /** 
     * Creates a <code>JFrame</code>, puts the <code>JComponent</code> that
     * is created by the factory method into it and displays the frame. This
     * Method also provides a <code>WindowListener</code>. 
     */
     
    public final void GUIComponentCreator.showFrame() {
        JFrame frame = new JFrame(getTitle());
        
   		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {System.exit(0);}
		});
		    
		JPanel panel = new JPanel();
	
		panel.add(createComponent());
		
		frame.getContentPane().add(panel);
		frame.pack();    
		frame.setLocation(lastFrameLocation);
		lastFrameLocation.translate(75, 75);
		frame.setVisible(true);  
    }    
}