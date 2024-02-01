/*

Copyright (c) Xerox Corporation 1998-2002.  All rights reserved.

Use and copying of this software and preparation of derivative works based
upon this software are permitted.  Any distribution of this software or
derivative works must comply with all applicable United States export control
laws.

This software is made available AS IS, and Xerox Corporation makes no warranty
about the software, its performance or its conformity to any specification.

|<---            this code is formatted to fit into 80 columns             --->|
|<---            this code is formatted to fit into 80 columns             --->|
|<---            this code is formatted to fit into 80 columns             --->|


SWFrame.java
Part of the Spacewar system.

*/

package spacewar;

import java.awt.Frame;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.MenuShortcut;
import java.awt.Dimension;
import java.awt.Insets;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

class SWFrame extends Frame implements ActionListener {
    private Game     game;
    private Display  display;
    private Menu     menu;

    Game     getGame()      { return game; }
    Display  getDisplay() { return display; }
    Menu     getMenu()      { return menu; }

    SWFrame(Game theGame, Display d) {
        super("Space War!");

        game = theGame;

        display = d;
        add(display);

        // create menu
        menu = new Menu("Game");
        MenuItem item1 = new MenuItem("Add Robot",   new MenuShortcut('a'));
        MenuItem item2 = new MenuItem("Reset Ships", new MenuShortcut('r'));
        MenuItem item3 = new MenuItem("Quit",        new MenuShortcut('q'));
        item1.setActionCommand("Add Robot");
        item2.setActionCommand("Reset Ships");
        item3.setActionCommand("Quit");
        menu.add(item1);
        menu.add(item2);
        menu.add(item3);
        menu.addActionListener(this);

        setMenuBar(new MenuBar());
        getMenuBar().add(menu);

        Dimension screenSize = new Dimension(500, 500);
        setSize(screenSize);
        setVisible(true);
        toFront();

        Insets inset = getInsets();
        int displayWidth  = screenSize.width - inset.left - inset.right;
        int displayHeight = screenSize.height - inset.top - inset.bottom;
        display.setSize(displayWidth, displayHeight);
    }

    public void actionPerformed(ActionEvent e) {
        String s = e.getActionCommand();
        if (s.equals("Add Robot")) {
            getGame().addRobot();
        }
        else if (s.equals("Reset Ships")) {
            getGame().resetShips();
        }
        else if (s.equals("Quit")) {
            getGame().quit();
        }
    }
}
