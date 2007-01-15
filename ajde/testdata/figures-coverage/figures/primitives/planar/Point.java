
package figures.primitives.planar;

import figures.*;
import java.util.Collection;

public class Point implements FigureElement {

    static int xx = -1;
    private int x;
    private int y;
    transient int currVal = 0;
    public static String name;  

    { 
	y = -1;
    }

    static {
	xx = -10;
    }

    int c; int b; int a; 
    {
        x = 0;
        y = 0;
    }

    static {
        Point.name = "2-Dimensional Point";
    }

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    int getCurrVal() {
	return currVal;
    }

    /**
     * @see Figure#moves
     */
    public int getX() { return x; }

    public int getY() { return y; }

    public void setX(int x) { this.x = x; }

    public void setY(int x) { this.y = x; }

    public void incrXY(int dx, int dy) {
        setX(getX() + dx);
        setY(getY() + dy);
    }
    public void check(int dx, int dy) 
	throws ArithmeticException, PointBoundsException {
	if (dx < 0 || dy < 0) throw new PointBoundsException();
    }
}

class PointBoundsException extends Exception { }

class BoundedPoint extends Point { 
    public BoundedPoint(int x, int y) { super(x, y); }
}

class StrictlyBoundedPoint extends BoundedPoint { 
    public StrictlyBoundedPoint(int x, int y) { super(x, y); }
}


