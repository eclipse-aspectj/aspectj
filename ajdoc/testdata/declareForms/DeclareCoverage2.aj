package foo;

public aspect DeclareCoverage2 {

    pointcut illegalNewFigElt(): call(Point2.new(..)) && !withincode(* *.doIt(..));
    
    declare error: illegalNewFigElt(): "Illegal constructor call.";
    declare warning: call(* Point2.setX(..)): "Illegal call.";
    declare warning : execution(* Point2.setX(..)) : "blah";

    declare parents: Point2 implements java.io.Serializable;
    declare soft: SizeException2 : call(* Point2.getX());
	declare precedence: DeclareCoverage2, InterTypeDecCoverage2, *;
}

aspect InterTypeDecCoverage2 {}

/**
 * comment about class Point2
 */
class Point2 { 
	
	int x = 2;
	public void setX(int x) {
		this.x = x;
	}
	
	public int getX() {
		return x;
	}
}

class Line2 {
}

class SizeException2 extends Throwable { }

class Main2 {
	
	public static void main(String[] args) {
	}
	
	public void doIt() {
		Point2 p = new Point2();
		p.setX(3);
		p.getX();
	}
	
}
