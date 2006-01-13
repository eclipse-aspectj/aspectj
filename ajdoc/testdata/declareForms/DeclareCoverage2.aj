package foo;

public aspect DeclareCoverage2 {

    pointcut illegalNewFigElt(): call(Point.new(..)) && !withincode(* *.doIt(..));
    
    declare error: illegalNewFigElt(): "Illegal constructor call.";
    declare warning: call(* Point.setX(..)): "Illegal call.";

    declare parents: Point extends java.io.Serializable;
	declare parents: Line implements java.util.Observable;
    declare soft: SizeException : call(* Point.getX());
	declare precedence: DeclareCoverage2, InterTypeDecCoverage, *;
}

aspect InterTypeDecCoverage {}

class Point { 
	
	int x = 2;
	public void setX(int x) {
		this.x = x;
	}
	
	public int getX() {
		return x;
	}
}

class Line {
}

class SizeException extends Throwable { }

class Main {
	
	public static void main(String[] args) {
	}
	
	public void doIt() {
		Point p = new Point();
		p.setX(3);
		p.getX();
	}
	
}
