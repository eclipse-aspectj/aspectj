
package figures;

aspect DisplayUpdating {

  pointcut moves(): call(void FigureElement.moveBy(int, int)) ||
                    call(void Point.setX(int))                ||
                    call(void Point.setY(int))                ||
                    call(void Line.setP1(Point))              ||
                    call(void Line.setP2(Point));

  after(): moves() {
    Display.needsRepaint();
  }
}
