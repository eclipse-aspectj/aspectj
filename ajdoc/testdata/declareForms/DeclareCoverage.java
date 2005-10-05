
package foo;

import java.io.*;
import java.util.List;
 
public aspect DeclareCoverage {

	void foo() { }
	
    pointcut illegalNewFigElt(): call(Point.new(..)) && !withincode(* *.doIt(..));

    declare error: illegalNewFigElt(): "Illegal constructor call.";
    declare warning: call(* Point.setX(..)): "Illegal call.";

    declare parents: Point extends java.io.Serializable;
	declare parents: Point && Line implements java.util.Observable;
    declare soft: SizeException : call(* Point.getX());
	declare precedence: DeclareCoverage, InterTypeDecCoverage, *;
}

aspect InterTypeDecCoverage {
	
	void foo() { } 
	
    public int Point.xxx = 0;
    public int Point.check(int i, int j) { return 1; }
}

class Point { 

}

class Line { 
	
}

class SizeException extends Throwable { } 

