import org.aspectj.testing.Tester;
import org.aspectj.lang.*;
import org.aspectj.lang.reflect.*;
import java.util.*;
import org.aspectj.examples.Point;

public class Strings {
    public static void main(String[] args) {
	Point p = new Point(1,1);
	p.x += 10;
	p.move(20, 20);

	try {
	    throw new UnsupportedOperationException("test");
	} catch (UnsupportedOperationException e) {
	}

    }
}

aspect JoinPoints {
    static before(): within(Strings) || instanceof(Point) && !receptions(new(..)) {
	System.out.println("tjp-short  : " + thisJoinPoint.toShortString());
	System.out.println("tjp-default: " + thisJoinPoint);
	System.out.println("tjp-long   : " + thisJoinPoint.toLongString());
	System.out.println();
	System.out.println("sig-short  : " + thisJoinPoint.getSignature().toShortString());
	System.out.println("sig-default: " + thisJoinPoint.getSignature());
	System.out.println("sig-long   : " + thisJoinPoint.getSignature().toLongString());
	System.out.println("--------------------------------------------------------------------------");
    }
}
