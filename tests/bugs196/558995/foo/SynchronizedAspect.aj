package foo;

import org.aspectj.lang.annotation.SuppressAjWarnings;

public aspect SynchronizedAspect perthis(@this(Synchronized)) {
	
//	private static final Logger log = LogManager.getLogger(SynchronizedAspect.class);
//	private Lock objectLock = new ReentrantLock();
//	private static long timeout = 1800;
//	private static TimeUnit unit = TimeUnit.SECONDS;
//	private static boolean timeoutInitialized = false;
	
//	@SuppressWarnings({"rawtypes"})
//	private final static synchronized void initTimeout(Class cl) {
//		timeout = SynchronizedStaticAspect.aspectOf(cl).timeout;
//		unit = SynchronizedStaticAspect.aspectOf(cl).unit;
//		timeoutInitialized = true;
//		log.trace("timeout inialized with " + timeout + " " + unit);
//	}
	
	pointcut synchronizedMethods() :
		execution(@Synchronized !synchronized !static * *..*.*(..))
		;
	
	pointcut synchronizedStaticMethods() :
		execution(@Synchronized !synchronized static * *..*.*(..))
		;
	
	pointcut ignoredSynchronized() :
		execution(@Synchronized * *..*.*(..))
		&& !synchronizedMethods()
		&& !synchronizedStaticMethods()
		;
	
	declare warning : ignoredSynchronized() :
		"@Synchronized is ignored here";
	
	declare warning :
	        (synchronizedStaticMethods() || synchronizedMethods())
	        && !@within(Synchronized) :
	    "@Synchronized is ignored here because @Synchronized for class is missing";

	/**
	 * Uses the Lock class of Java 5 to put a synchronization wrapper around
	 * a method. Advantage of this Lock class is the posibility to use a
	 * timeout to avoid dead locks.
	 * 
	 * @return the return value of the wrapped method
	 */
	@SuppressAjWarnings({"adviceDidNotMatch"})
	Object around() : synchronizedMethods() && @within(Synchronized) {
return proceed();
/*
		if (!timeoutInitialized) {
			initTimeout(thisJoinPointStaticPart.getSignature().getDeclaringType());
		}
		if (log.isTraceEnabled()) {
			log.trace("synchronizing " + thisJoinPoint.getSignature().toShortString() + "...");
		}
		try {
			if (objectLock.tryLock(timeout, unit)) {
				if (log.isTraceEnabled()) {
					log.trace("lock granted for "
							+ thisJoinPoint.getSignature().toShortString());
				}
				try {
					return proceed();
				} finally {
					objectLock.unlock();
					if (log.isTraceEnabled()) {
						log.trace("lock released for "
								+ thisJoinPoint.getSignature().toShortString());
					}
				}
			} else {
				String msg = "can't get " + objectLock + " for "
						+ thisJoinPoint.getSignature().toShortString()
						+ " after " + timeout + " " + unit;
				log.error(msg);
				throw new RuntimeException(msg);
			}
		} catch (InterruptedException ie) {
			String msg = "interrupted: "
					+ thisJoinPoint.getSignature().toShortString();
			log.warn(msg, ie);
			throw new RuntimeException(msg, ie);
		}
*/
	}
	
}
