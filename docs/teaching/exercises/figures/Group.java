/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Common Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     PARC     initial implementation
 * ******************************************************************/


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

    public void setIdentifier(String identifier) {
        _identifier = identifier;
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

