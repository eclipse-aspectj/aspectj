
package figures;

class Line {
    private Point p1, p2;

    Line(Point p1, Point p2) {
	super();
	this.p1 = p1;
	this.p2 = p2;
    }

    Point getP1() { return p1; }
    Point getP2() { return p2; }

    void setP1(Point p1) { this.p1 = p1; }
    void setP2(Point p2) { this.p2 = p2; }

    void moveBy(int dx, int dy) {
	getP1().moveBy(dx, dy);
	getP2().moveBy(dx, dy);
    }
}
