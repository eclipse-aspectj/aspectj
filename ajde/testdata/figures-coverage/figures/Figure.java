
package figures;

import figures.primitives.planar.Point;

import java.awt.Canvas;

aspect Figure {
    //pointcut sendSuccess(): cflow(setX()) && !handler(Exception);

    public String Point.getName() {
        return Point.name;
    } 

    public int figures.primitives.planar.Point.DEFAULT_X = 0; 

    public pointcut constructions(): call(Point.new(int, int)) || call(SolidPoint.new(int, int, int));

    public pointcut moves(FigureElement fe): target(fe) &&
        (call(String Point.getName()) ||
	 call(void FigureElement.incrXY(int, int)) ||
         call(void Point.setX(int)) ||
         call(void Point.setY(int)) ||
         call(void SolidPoint.setZ(int)));
    
    pointcut mainExecution(): 
	execution(int main(*));

    pointcut runtimeHandlers(): mainExecution() 
	|| handler(RuntimeException);

    public pointcut mumble(): runtimeHandlers();

    before(int newValue): set(int *.*) && args(newValue) {   }

    before(): get(int *.*) { }

    before(): constructions() {
	    System.out.println("> before construction, jp: " + thisJoinPoint.getSignature());
    } 

    before(FigureElement fe): moves(fe) {
	    System.out.println("> about to move FigureElement at X-coord: ");
	}

    after(): initialization(Point.new(..)) || staticinitialization(Point) {
        System.out.println("> Point initialized");
    }

    // should be around
    after(): mumble() {
        System.err.println(">> in after advice...");
        //proceed();
    }

    after(FigureElement fe): target(fe) &&
        (call(void FigureElement.incrXY(int, int)) ||
         call(void Point.setX(int)) ||
         call(void Point.setY(int)) ||
         call(void SolidPoint.setZ(int))) {
        System.out.println("> yo.");
    }

    after(FigureElement fe):
	target(fe) &&
	(call(void FigureElement.incrXY(int, int)) ||
	 call(void Line.setP1(Point))              ||
	 call(void Line.setP2(Point))              ||
	 call(void Point.setX(int))                ||
	 call(void Point.setY(int))) { }

    declare parents: Point extends java.io.Serializable;

    declare parents: Point implements java.util.Observable;

	// AMC - this next line doesn't make sense!! Can these tests ever
	// have been run???
    //declare soft: Point: call(* *(..));
}

aspect Checks {
    pointcut illegalNewFigElt():  call(FigureElement+.new(..))  &&
	                          !withincode(* Main.main(..));

//	pointcut illegalNewFigElt(): execution(FigureElement+.new(..));

    declare error: illegalNewFigElt():
	    "Illegal figure element constructor call.";

    declare warning: illegalNewFigElt():
	    "Illegal figure element constructor call.";
}
