import java.io.*;
import org.aspectj.testing.Tester;

public class Driver {
    public static void main(String[] args) { test(); }

    public static void test() {
        Point p = new Point();
        p.setX(3);
    }
}

class Point {
    int _x = 0;
    int _y = 0;

    Point() {}

    void set (int x, int y) {
        _x = x; _y = y;
    }

    void setX (int x) { _x = x; }
    void setY (int y) { _y = y; }

    int getX() { return _x; }
    int getY() { return _y; }
}

aspect Trace {
    static int oldvalue;

     before(Point p, int newvalue): target(p) && args(newvalue) &&
                                          (call(void setX(int)) ||
                                           call(void setY(int))) {
        oldvalue = p.getX();
    }
     after(Point p, int newvalue): target(p) && args(newvalue) &&
                                         (call(void setX(int)) ||
                                          call(void setY(int))) {
        
        Tester.checkEqual(oldvalue,0, "oldvalue");
        Tester.checkEqual(newvalue,3, "newvalue");
    }
}
