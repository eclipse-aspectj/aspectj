
package figures;

class Point {
    private int x = 0, y = 0;

    Point(int x, int y) {
	    super();
	    this.x = x;
	    this.y = y;
    }

    int getX() {
        return x;
    }

    int getY() {
        return y;
    }

    void setX(int x) {
        this.x = x;
    }

    void setY(int y) {
        this.y = y;
    }

    void moveBy(int dx, int dy) {
	    setX(getX() + dx);
	    setY(getY() + dy);
    }
}
