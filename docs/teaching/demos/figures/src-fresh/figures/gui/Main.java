/*
Copyright (c) 2001-2002 Palo Alto Research Center Incorporated. All Rights Reserved.
 */

package figures.gui;

import javax.swing.*;
import figures.support.Log;
import figures.Point;

public class Main {
    static FigurePanel panel;
 
    public static void main(String[] args) {
        JFrame figureFrame = new JFrame("Figure Editor");
        panel = new FigurePanel();
        figureFrame.setContentPane(panel);
        figureFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        figureFrame.pack();
        figureFrame.setVisible(true);
         
//        Point p = new Point(0, 0);
//        p.setX(-10);
    }
}
