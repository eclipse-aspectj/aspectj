
import java.io.*;

class Point { 

	int x;
	static int sx;

	{ x = 0; }
	static { sx = 1; }
	
	public Point() { }
	
	public int getX() { return x;	}
	
	public void setX(int x) { this.x = x; }
	
	void doIt() { 
		try {
			File f = new File(".");
			f.getCanonicalPath();
		} catch (IOException ioe) {
			System.err.println("!");	
		}	
		setX(10);
		new Point();
	}
} 

aspect AdvisesRelationCoverage {
    before(): get(int *.*) { }
    before(): set(int *.*) { }
    before(): initialization(Point.new(..)) { }
    before(): staticinitialization(Point) { }
    before(): handler(IOException) { }
    before(): call(* Point.setX(int)) { }
    before(): call(Point.new()) { }
    before(): within(*) && execution(* Point.setX(..)) { }
    before(): within(*) && execution(Point.new()) { }
}

aspect InterTypeDecCoverage {

    pointcut illegalNewFigElt(): call(Point.new(..)) && !withincode(* *.doIt(..));

    declare error: illegalNewFigElt():
	    "Illegal figure element constructor call.";

    declare warning: illegalNewFigElt():
	    "Illegal figure element constructor call.";

    declare parents: Point extends java.io.Serializable;

    declare parents: Point implements java.util.Observable;

    //declare soft: Point: call(* *(..));

    public String Point.getName() { return "xxx"; }

    public int Point.xxx = 0; 
}

aspect AdviceCoverage {

}





