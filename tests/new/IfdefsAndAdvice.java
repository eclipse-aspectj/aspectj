/* We would like to use some performance benchmarking as a way to determine
    if ajc is or is not generating the code for dynamic join points in all 
    cases except for m4().  However, HotSpot is too smart for us today, and
    manages to optimize this away.  Since the point of this code is that
    such an optimization is possible, I'm not sure that we can write a test case
    for this...

   So, this test case doesn't do all that good a job.
*/

import org.aspectj.testing.Tester;

public class IfdefsAndAdvice {
    public static void main(String[] args) {
	double t1 = timeIt(1);
	double t2 = timeIt(2);
	double t3 = timeIt(3);
	double t4 = timeIt(4);

	//System.out.println(t1 + ":" + t2 + ":" + t3 + ":" + t4); 
    }

    public static double timeIt(int m) {
	callIt(m);
	callIt(m);

	final int N = 1000;

	long startTime = System.currentTimeMillis();
	for (int i = 0; i < N; i++) { callIt(m); }
	long stopTime = System.currentTimeMillis();

	return (stopTime - startTime)/1000.0;
    }

    public static void callIt(int m) {
	switch(m) {
	case 1: m1(1,2,3,4,5,6,7,8,9,10);
	case 2: m2(1,2,3,4,5,6,7,8,9,10);
	case 3: m3(1,2,3,4,5,6,7,8,9,10);
	case 4: m4(1,2,3,4,5,6,7,8,9,10);
	}
    }


    public static int m1(int i1, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9, int i10) {
	return i1+i2+i3+i4+i5+i6+i7+i8+i9+i10;
    }
    public static int m2(int i1, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9, int i10) {
	return i1+i2+i3+i4+i5+i6+i7+i8+i9+i10;
    }
    public static int m3(int i1, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9, int i10) {
	return i1+i2+i3+i4+i5+i6+i7+i8+i9+i10;
    }
    public static int m4(int i1, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9, int i10) {
	return i1+i2+i3+i4+i5+i6+i7+i8+i9+i10;
    }
}

aspect A {
    static final boolean DEBUG = false;

    before (): execution(int m1(..)) {
	if (DEBUG) {
	    Object args = thisJoinPoint.getThis();
	} else {
	    String sig = thisJoinPoint.toString();
	}
    }

    before (): execution(int m2(..)) {
	if (DEBUG) {
	    Object args = thisJoinPoint.getThis();
	} 
    }

    before (): execution(int m3(..)) {
	if (!DEBUG) {
	    String sig = thisJoinPoint.toString();
	} else {
	    Object args = thisJoinPoint.getThis();
	}
    }

    before (): execution(int m4(..)) {
	if (!DEBUG) {
	    Object args = thisJoinPoint.getThis();
	} else {
	    String sig = thisJoinPoint.toString();
	}
    }
}
