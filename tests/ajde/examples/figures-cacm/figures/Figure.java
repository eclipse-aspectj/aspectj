
package figures;

import java.util.List;
import java.util.LinkedList;


class Figure {

    List elements = new LinkedList();

    Point makePoint(int x, int y) {
	Point p = new Point(x, y);
	elements.add(p);
	return p;
    }

    Line makeLine(Point p1, Point p2) {
	Line l = new Line(p1, p2);
	elements.add(l);
	return l;
    }
}
