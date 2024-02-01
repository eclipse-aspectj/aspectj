/*
Copyright (c) Xerox Corporation 1998-2002.  All rights reserved.

Use and copying of this software and preparation of derivative works based
upon this software are permitted.  Any distribution of this software or
derivative works must comply with all applicable United States export control
laws.

This software is made available AS IS, and Xerox Corporation makes no warranty
about the software, its performance or its conformity to any specification.
*/

package observer;
import java.awt.Frame;
import java.awt.Panel;
import java.awt.Container;
import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.BorderLayout;

/*
 * Display is the container class that holds all the views of the
 * colored number.
 * In this demo, it holds buttons.
 */

class Display extends Panel {

    protected Frame frame = new Frame("Subject/Observer Demo");

    Display() {
        frame.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {System.exit(0);}
            });

        frame.add(this, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
    }

    void addToFrame(Component c) {
        add(c);
        frame.pack();
    }
}
