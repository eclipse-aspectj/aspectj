
import java.io.*;
import java.util.List;
 
interface I { } 

class Point { 
	int x;
	static int sx;

	{ 
		System.out.println(""); 
	}

	{ x = 0; }
	static { sx = 1; }
	
	public Point() { }
	
	public int getX() { 
		return x;	
	}
	
	public void setX(int x) { 
		this.x = x; 
	}
	 
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

aspect AdvisesRelationshipCoverage {
	pointcut methodExecutionP(): execution(void Point.setX(int));
	before(): methodExecutionP() { }
  
	pointcut constructorExecutionP(): execution(Point.new());
	before(): constructorExecutionP() { }

	pointcut callMethodP(): call(* Point.setX(int));
	before(): callMethodP() { }

	pointcut callConstructorP(): call(Point.new());
	before(): callConstructorP() { }

	pointcut getP(): get(int *.*);
	before(): getP() { }

	pointcut setP(): set(int *.*) && !set(int *.xxx);
	before(): setP() { }

	pointcut initializationP(): initialization(Point.new(..));
	before(): initializationP() { }

	pointcut staticinitializationP(): staticinitialization(Point);
	before(): staticinitializationP() { }

	pointcut handlerP(): handler(IOException);
	before(): handlerP() { }

//    before(): within(*) && execution(* Point.setX(..)) { }
//    before(): within(*) && execution(Point.new()) { }
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
	before(int i): call(* *.mumble()) && named() && namedWithOneArg(i) { }	
	before(int i): named() && call(* *.mumble()) && namedWithOneArg(i) { }	
	
	before(): call(* *.mumble()) { }
}
  
abstract aspect AbstractAspect {
	abstract pointcut abPtct();	
}
  
aspect InterTypeDecCoverage {
    public int Point.xxx = 0;
    public int Point.check(int i, Line l) { return 1 + i; }
}

aspect DeclareCoverage {

    pointcut illegalNewFigElt(): call(Point.new(..)) && !withincode(* *.doIt(..));

    declare error: illegalNewFigElt(): "Illegal constructor call.";
    declare warning: call(* Point.setX(..)): "Illegal call.";

    declare parents: Point extends java.io.Serializable;
    declare parents: Point+ implements java.util.Observable;
	declare parents: Point && Line implements java.util.Observable;
    declare soft: SizeException : call(* Point.getX());
	declare precedence: AdviceCoverage, InterTypeDecCoverage, *;
//	public Line.new(String s) {  }
}

class SizeException extends Exception { } 

aspect AdviceCoverage {

}

abstract class ModifiersCoverage {
	private int a;
	protected int b;
	public int c;
	int d;

	static int staticA;
	final int finalA = 0;
	
	abstract void abstractM();
}

aspect Pointcuts {
    pointcut a(): call(Point.new(..));   
}

aspect PointcutUsage {
   
    pointcut usesA(): Pointcuts.a() && within(Point);
    
    pointcut usesUsesA(): usesA();
    
    after(): usesUsesA() { }
}





