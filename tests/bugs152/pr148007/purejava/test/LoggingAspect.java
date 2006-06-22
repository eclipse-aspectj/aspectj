package test;

import org.aspectj.lang.NoAspectBoundException;

public class LoggingAspect {
	
	private static LoggingAspect ajc$perSingletonInstance;
	private static Throwable ajc$initFailureCause;
	
	static {
		try {
			ajc$postClinit();
		}
		catch (Throwable t) {
			ajc$initFailureCause = t;
		}
	}
	
	public static LoggingAspect aspectOf() {
		if (ajc$perSingletonInstance == null) {
			throw new NoAspectBoundException("test_LoggingAspect",ajc$initFailureCause);
		}
		return ajc$perSingletonInstance;
	}
	
    public void ajc$afterReturning$test_LoggingAspect$1$188fbb36()  {
    }
    
    private static void ajc$postClinit() {
    	ajc$perSingletonInstance = new LoggingAspect();
    }
}
