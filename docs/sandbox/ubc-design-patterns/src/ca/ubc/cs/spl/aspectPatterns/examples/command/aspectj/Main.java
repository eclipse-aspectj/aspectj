package ca.ubc.cs.spl.aspectPatterns.examples.command.aspectj;

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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;  

import ca.ubc.cs.spl.aspectPatterns.patternLibrary.Command;

/**
 * Implements the driver for the command design pattern example.<p> 
 *
 * Intent: <i>Encapsulate a request as an object, thereby letting you 
 * parameterize clients with different requests, queue or log requests, 
 * and support undoable operations.</i><p>
 *
 * Participating objects are <code>Button</code>s as <i>Invoker</i>s,
 * and a <code>ButtonCommand</code> and <code>ButtonCommand2</code> as 
 * two <i>ConcreteCommand</i>s.
 *
 * This example creates a simple GUI with three buttons. Each button has a 
 * command associated with it that is executed when the button is pressed. 
 * Button1 and button3 have the same command, button2 has a different one.
 *
 * <p><i>This is the Java version.</i><p> 
 *
 * This version of the pattern lets the developer specify what should trigger 
 * a call to <code>executeCommand()</code>, without changing the <i>Invoker
 * </i> code.
 *
 * Neither <i>Commands</i> nor <i>Invoker</i> have to know of their 
 * involvement in the pattern and can actually act as both. 
 * <code>ButtonCommanding2</code> is an example of a <i>Command</i> that
 * is unaware of its role. In such cases, the concrete pattern instance
 * aspect assigns the role and defines the <i>Command</i>'s behavior.
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 02/06/04
 * 
 * @see Button
 * @see ButtonCommand
 * @see Buttoncommand2
 */

public class Main extends JFrame {
	
    /**
     * This example creates a simple GUI with three buttons. Each 
     * button has a <i>Command</i> associated with it that is executed when 
     * the button is pressed. Button1 and button3 have the same command, 
     * button2 has a different one.
     */

	public static void main(String[] args) {
    	Button button1 = new Button("Button1");
    	Button button2 = new Button("Button2");
    	Button button3 = new Button("Button3"); 
    	
    	Command com1 = new ButtonCommand();
    	Command com2 = new ButtonCommand2();
    	
		JPanel pane = new JPanel();
		pane.add(button1);  
		ButtonCommanding.aspectOf().setCommand(button1, com1);
		
		pane.add(button2);
		ButtonCommanding.aspectOf().setCommand(button2, com2);
		ButtonCommanding.aspectOf().setReceiver(com2, new Printer());
		
		pane.add(button3);
		ButtonCommanding.aspectOf().setCommand(button3, com1);
		
        JFrame frame = new JFrame("Command Pattern Example");		
		
		frame.getContentPane().add(pane);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {System.exit(0);}
		});  
		frame.pack();
		frame.setVisible(true);	}
}
