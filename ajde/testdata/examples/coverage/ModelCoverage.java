
import java.io.*;

class Point { 

	int x;
	static int sx;

	{ x = 0; }
	static { sx = 1; }
	
	public Point() { }
	
	public int getX() { return x;	}
	
	public void setX(int x) { this.x = x; }
	
	public int changeX(int x) { 
		this.x = x;
		return x;
	}
	
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

class SubPoint extends Point { }

class Line { }

aspect AdvisesRelationCoverage {
    before(): execution(*..*.new(..)) { }
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

aspect AdviceNamingCoverage {
	pointcut named(): call(* *.mumble());
	pointcut namedWithOneArg(int i): call(int Point.changeX(int)) && args(i);
	pointcut namedWithArgs(int i, int j): set(int Point.x) && args(i, j);

	after(): named() { }	
	after(int i, int j) returning: namedWithArgs(i, j) { }
	after() throwing: named() { }
	after(): named() { } 
	
	before(): named() { }
	
	int around(int i): namedWithOneArg(i) { return i;}
	int around(int i) throws SizeException: namedWithOneArg(i) { return proceed(i); }
	
	before(): named() { }	
	before(int i): call(* XXX.mumble()) && named() && namedWithOneArg(i) { }	
	before(int i): named() && call(* *.mumble()) && namedWithOneArg(i) { }	
	
	before(): call(* *.mumble()) { }
}
  
abstract aspect AbstractAspect {
	abstract pointcut abPtct();	
}
  
aspect InterTypeDecCoverage {

    pointcut illegalNewFigElt(): call(Point.new(..)) && !withincode(* *.doIt(..));

    declare error: illegalNewFigElt(): "Illegal constructor call.";
    declare warning: illegalNewFigElt(): "Illegal constructor call.";

    declare parents: Point extends java.io.Serializable;
    declare parents: Point+ implements java.util.Observable;
	declare parents: Point && Line implements java.util.Observable;
    declare soft: SizeException : call(* Point.getX());
	declare precedence: AdviceCoverage, InterTypeDecCoverage, *;

	public int Point.xxx = 0;
    public int Point.check(int i, Line l) { return 1 + i; }
//	public Line.new(String s) {  }
}

class SizeException extends Exception { } 

aspect AdviceCoverage {

}





