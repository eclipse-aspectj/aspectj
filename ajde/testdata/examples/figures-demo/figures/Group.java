/*
Copyright (c) 2002 Palo Alto Research Center Incorporated. All Rights Reserved.
 */

package figures;

import java.util.*;
import java.awt.*;
import java.awt.geom.*;

public class Group implements FigureElement {
    private Collection _members;
    private String _identifier;

    public Group(FigureElement first) {
        this._members = new ArrayList();
        add(first);
    }

    public void add(FigureElement fe) {
        _members.add(fe);
    }

    public Iterator members() {
        return _members.iterator();
    }

    public void move(int dx, int dy) {
        for (Iterator i = _members.iterator(); i.hasNext(); ) {
            FigureElement fe = (FigureElement)i.next();
            fe.move(dx, dy);
        }
    }

    public void resetIdentifier(String identifier) {
        resetIdentifier(identifier);
    }

    public String toString() {
        if (_identifier != null) {
            return _identifier;
        }

        StringBuffer buf = new StringBuffer("Group(");
        for (Iterator i = _members.iterator(); i.hasNext(); ) {
            buf.append(i.next().toString());
            if (i.hasNext()) {
                buf.append(", ");
            }
        }
        buf.append(")");
        return buf.toString();
    }

    public Rectangle getBounds() {
        Rectangle previous = null;
        for (Iterator i = _members.iterator(); i.hasNext(); ) {
            FigureElement fe = (FigureElement)i.next();
            Rectangle rect = fe.getBounds();
            if (previous != null) {
                previous = previous.union(rect);
            } else {
                previous = rect;
            }
        }
        return previous;
    }

    public boolean contains(Point2D p) {
        for (Iterator i = _members.iterator(); i.hasNext(); ) {
            FigureElement fe = (FigureElement)i.next();
            if (fe.contains(p)) return true;
        }
        return false;
    }

    public void paint(Graphics2D g2) {
        for (Iterator i = _members.iterator(); i.hasNext(); ) {
            FigureElement fe = (FigureElement)i.next();
            fe.paint(g2);
        }
    }

    public int size() {
        return _members.size();
    }
}

