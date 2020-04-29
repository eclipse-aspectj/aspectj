package foo;

/**
 * For synchronization we synchronize each static method mark as @Synchronized
 * by a ReentrantLock.
 *
 * @author <a href="boehm@javatux.de">oliver</a>
 * @since 0.8
 */
import org.aspectj.lang.annotation.SuppressAjWarnings;

public aspect SynchronizedStaticAspect pertypewithin(@Synchronized *) {

/*
	private static final Logger log = LogManager.getLogger(SynchronizedStaticAspect.class);
	private Lock classLock = new ReentrantLock();
	protected long timeout = 1800;
	protected TimeUnit unit = TimeUnit.SECONDS;
	
*/
	/**
	 * This advice is used to get the timeout value for the wrapped static
	 * methods.
	 * 
	 * @param t the annotation with the timeout value
	 */
	before(Synchronized t) :
			staticinitialization(@Synchronized *) && @annotation(t) {
/*
		this.timeout = t.timeout();
		this.unit = t.unit();
		if (log.isTraceEnabled()) {
			log.trace("lock timeout for "
					+ thisJoinPointStaticPart.getSignature().getDeclaringType().getSimpleName()
					+ " set to " + this.timeout + " " + this.unit);
		}
*/
	}


private Log log = new Log();
static class Log {
  public boolean isTraceEnabled() { return true;}
  public void trace(String s) {}
  public void error(String s) {}
  public void warn(String s,InterruptedException ie) {}

}
ClassLock classLock = new ClassLock();
static class ClassLock {
  public boolean tryLock(Object o, Object b) {
return true;
}
public void unlock(){}
}
String timeout="";
String unit="";
	/**
	 * This is the synchronization wrapper for the static methods which are
	 * synchronized by a ReentrantLock class.
	 * 
	 * @return the return value of the static method
	 */
@SuppressAjWarnings
	Object around() : SynchronizedAspect.synchronizedStaticMethods() {
		if (log.isTraceEnabled()) {
			log.trace("synchronizing " + thisJoinPointStaticPart.getSignature().toShortString() + "...");
		}
		try {
			if (classLock.tryLock(timeout, unit)) {
				if (log.isTraceEnabled()) {
					log.trace("lock granted for "
							+ thisJoinPointStaticPart.getSignature().toShortString());
				}
				try {
					return proceed();
				} finally {
					classLock.unlock();
					if (log.isTraceEnabled()) {
						log.trace("lock released for "
								+ thisJoinPointStaticPart.getSignature().toShortString());
					}
				}
			} else {
				String msg = "can't get " + classLock + " for "
						+ thisJoinPointStaticPart.getSignature().toShortString();
				log.error(msg);
				throw new RuntimeException(msg);
			}
		} catch (InterruptedException ie) {
			String msg = "interrupted: "
					+ thisJoinPoint.getSignature().toShortString();
			log.warn(msg, ie);
			throw new RuntimeException(msg, ie);
		}
	}

}
