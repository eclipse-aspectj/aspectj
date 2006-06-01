/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     PARC     initial implementation
 * ******************************************************************/


package figures.gui;

import figures.Point;
import figures.Line;
import figures.FigureElement;
import figures.Group;


import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.border.*;

public class FigurePanel extends JComponent {

    ButtonsPanel bp = new ButtonsPanel();
    FigureSurface fs = new FigureSurface();
    ConsolePanel cp = new ConsolePanel();


    public FigurePanel() {
        setLayout(new BorderLayout());
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(fs);
        panel.add(bp);
        JSplitPane sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT, panel, cp);
        sp.setPreferredSize(new Dimension(500, 400));
        sp.setDividerLocation(250);
        add(BorderLayout.CENTER, sp);
    }

    class ButtonsPanel extends JPanel {
        JLabel msgs = new JLabel("click to add a point or line");
        public ButtonsPanel() {
            setLayout(new FlowLayout(FlowLayout.LEFT));
            add(new JButton(new AbstractAction("Main") {
                    public void actionPerformed(ActionEvent e) {
                        Main.main(new String[]{});
                        fs.repaint();
                    }
                }));
            add(msgs);
        }

        public void log(String msg) {
            msgs.setText(msg);
        }
    }

    static class ConsolePanel extends JPanel {

        JTextArea text = new JTextArea();

        public ConsolePanel() {
            super(new BorderLayout());
            text.setFont(StyleContext.getDefaultStyleContext().getFont("SansSerif", Font.PLAIN, 10));
            JScrollPane scroller = new JScrollPane(text);
            scroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            add(BorderLayout.CENTER, scroller);
        }

        public void println(String msg) {
            text.append(msg + '\n');
        }
    }

    final static Color BACKGROUND = Color.white;

    static class FigureSurface extends JPanel implements MouseListener, MouseMotionListener {
        Group canvas;

        public FigureSurface() {
            canvas = new Group(new Point(250, 250));
            addMouseMotionListener(this);
            addMouseListener(this);
            setPreferredSize(new Dimension(500,500));
        }

        private Point addPoint(int x, int y) {
            Point p = new Point(x, y);
            canvas.add(p);
            repaint();
            return p;
        }

        private Line addLine(Point p1, Point p2) {
            if (Math.abs(p1.getX()-p2.getX()) < 5 ||
                Math.abs(p1.getY()-p2.getY()) < 5) {
                return null;
            }

            Line line = null;
            if (p1 != null && p2 != null) {
                line = new Line(p1, p2);
                canvas.add(line);
            }
            repaint();
            return line;
        }

        public void paint(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setPaint(BACKGROUND);
            g2.fill(new Rectangle2D.Float(0f, 0f, (float)g2.getClipBounds().width, (float)g2.getClipBounds().height));
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            canvas.paint(g2);
        }


        int lastX, lastY;
        int pressX, pressY;

        FigureElement first = null;
        Point point1 = null;

        public void mousePressed(MouseEvent e){
            int x = e.getX(), y = e.getY();
            pressX = lastX = x; pressY = lastY = y;
            first = findFigureElement(x, y);
            if (first == null) {
                point1 = addPoint(x, y);
            }
        }

        public void mouseDragged(MouseEvent e) {
            int x = e.getX(), y = e.getY(), dx = lastX-x, dy = lastY-y;
            lastX = x;
            lastY = y;
            if (first == null) {
                Line line = addLine(point1, new Point(x, y));
                if (line != null) {
                    canvas.add(line.getP2());
                    first = line.getP2();
                    canvas.add(line);
                }
            } else {
                first.move(-dx, -dy);
            }
            repaint();
        }

        public void mouseReleased(MouseEvent e){
            mouseDragged(e);
            first = null;
            point1 = null;
        }


        public void mouseMoved(MouseEvent e){}
        public void mouseClicked(MouseEvent e){}
        public void mouseExited(MouseEvent e){}
        public void mouseEntered(MouseEvent e){}

        private FigureElement findFigureElement(int x, int y) {
            Point2D p = new Point2D.Float((float)x, (float)y);
            for (Iterator i = canvas.members(); i.hasNext(); ) {
                FigureElement fe = (FigureElement)i.next();
                if (fe.contains(p)) return fe;
            }
            return null;
        }
    }
}
