import org.aspectj.testing.Tester;

public aspect Driver {

  static boolean point, line, circle;

  public static void main(String[] args) { test(); }

  public static void test() {
    Point p  = new Point();
    Line l   = new Line();
    Circle c = new Circle();

    Tester.check(point, "point");
    Tester.check(line, "line");
    Tester.check(circle, "circle");
  }

   before (): target(Point) && execution(new(..))  {
      point = true;
  }

   before (): target(Line) && execution(new(..)) {
      line = true; 
  }

  // the * thrown in just for fun
   before (): target(*) && target(Circle) && execution(new()) {
      circle = true;
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

class Line {
  int _x1, _y1, _x2, _y2;

  Line() {}

  void set (int x1, int y1, int x2, int y2) {
    _x1 = x1; _y1 = y1; _x2 = x2; _y2 = y2;
  }

  void setX1 (int x1) { _x1 = x1; }
  void setY1 (int y1) { _y1 = y1; }
  void setX2 (int x2) { _x2 = x2; }
  void setY2 (int y2) { _y2 = y2; }

  int getX1() { return _x1; }
  int getY1() { return _y1; }
  int getX2() { return _x2; }
  int getY2() { return _y2; }
}

class Circle {
  int _x = 0;
  int _y = 0;
  int _r = 0;

  Circle() {}

  void set (int x, int y, int r) {
    _x = x; _y = y; _r = r;
  }

  void setX (int x) { _x = x; }
  void setY (int y) { _y = y; }
  void setR (int r) { _r = r; }

  int getX() { return _x; }
  int getY() { return _y; }
  int getR() { return _r; }
}
