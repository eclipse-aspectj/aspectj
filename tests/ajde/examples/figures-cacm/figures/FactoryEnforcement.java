
package figures;

aspect FactoryEnforcement {

    pointcut illegalNewFigElt():  call(FigureElement+.new(..)) &&
	                         !withincode(* Figure.make*(..));

    declare error: illegalNewFigElt():
	"Illegal figure element constructor call.";

}
