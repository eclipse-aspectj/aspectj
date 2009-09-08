package answers;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import figures.*;
import java.awt.Rectangle;

@Aspect
public class Answer2h {
	@Pointcut("call(public void figures.FigureElement+.move" +
			"()) && target(fe)")
	void movingFigureElement(Point fe) {}

	@Around("movingFigureElement(fe)")
	public void checkIfBoundsMovedSame(ProceedingJoinPoint thisJoinPoint,
			Point fe) throws Throwable {		
/*
	@Pointcut("call(public void figures.FigureElement+.move" +
			"(int, int)) && target(fe) && args(dx, dy)")
	void movingFigureElement(FigureElement fe, int dx, int dy) {}

	@Around("movingFigureElement(fe, dx, dy)")
	public void checkIfBoundsMovedSame(ProceedingJoinPoint thisJoinPoint,
			FigureElement fe, int dx, int dy) throws Throwable {		
*/
		Rectangle rectangleBefore = new Rectangle(fe.getBounds());
		//thisJoinPoint.proceed(new Object[]{fe, dx, dy});		
		thisJoinPoint.proceed(new Object[]{fe});		
//		rectangleBefore.translate(dx, dy);
		if(!rectangleBefore.equals(fe.getBounds()))
			throw new IllegalStateException("move() invariant violation");

		
		// IF THE THREE LINES BELOW ARE UN-COMMENTED, THE EXCEPTION
		// ISN'T THROWN!?
		// Note: The three lines can be located anywhere inside the advice. 
//		for(Object o: thisJoinPoint.getArgs()) {
//			System.out.print(o+" ");
//		}
	}
}
