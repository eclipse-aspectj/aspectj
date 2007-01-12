
package figures;

import figures.primitives.planar.Point;
import figures.primitives.solid.SolidPoint;

class Main {

    private static Point startPoint;

    public static void main(String[] args) {
        try {
            System.out.println("> starting...");

            startPoint = makeStartPoint();
            //startPoint.setX(3); new Point(0, 0);
//	        SolidPoint sp1 = new SolidPoint(1, 3, 3);

//	        sp1.setZ(1);
//	        p1.incrXY(3, 3);
        } catch (RuntimeException re) {
            re.printStackTrace();
        }
        System.out.println("> finished.");
    }

    /** @deprecated     use something else */
    public static Point makeStartPoint() {
	 //return new Point(1, 2);
         return null;
    }

    /** This should produce a deprecation warning with JDK > 1.2 */
    static class TestGUI extends javax.swing.JFrame {
        TestGUI() {
           this.disable();
        }
    }

    /** This should produce a porting-deprecation warning. */
    //static pointcut mainExecution(): execution(void main(*));
}

privileged aspect Test {
    pointcut testptct(): call(* *.*(..));

    before(Point p, int newval): target(p) && set(int Point.xx) && args(newval) {
        System.err.println("> new value of x is: " + p.x + ", setting to: " + newval);
    }

	before(int newValue): set(int Point.*) && args(newValue) {
	    if (newValue < 0) {
	        throw new IllegalArgumentException("too small");
	    } 
	}
}
