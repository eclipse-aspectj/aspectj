package test;

/**
 * Understands . . .
 *
 * @author Randy Stearns
 */
public class BooleanUnitTest  {

	public static void main(String[] args) {
		new BooleanUnitTest().test1();
	}
	
    public void test1() {
        assertEquals("1a WRONG!", false, invert1a());
        assertEquals("1b WRONG!", true, invert1b());
        assertEquals("2 WRONG!", false, invert2());
        assertEquals("3 WRONG!", true, invert3());
        assertEquals("4 WRONG!", true, invert4());
        assertEquals("5 WRONG!", false, invert5());
    }
    
    private void assertEquals(String msg, boolean a, boolean b) {
    	if (a != b) {
    		throw new RuntimeException(msg);
    	}
    }

    private boolean invert1a() {
        return ! true;
    }

    private boolean invert1b() {
        return ! false;
    }

    private boolean invert2() {
    	boolean ret = false;
    	try {
	        ret = ! isTrue();
    	}
    	catch (RuntimeException t) {
	   		LoggingAspect.aspectOf().ajc$afterReturning$test_LoggingAspect$1$188fbb36();
	   		throw t;
    	}
   		LoggingAspect.aspectOf().ajc$afterReturning$test_LoggingAspect$1$188fbb36();
   		return ret;
    }

    private boolean invert3() {
        return ! isFalse();
    }

    private boolean invert4() {
        boolean temp = isFalse();
        return ! temp;
    }

    private boolean invert5() {
        boolean temp = isTrue();
        return ! temp;
    }

    private boolean isTrue() {
        return true;
    }

    private boolean isFalse() {
        return false;
    }
}
