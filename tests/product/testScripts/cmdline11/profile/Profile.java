
package profile;

import org.aspectj.lang.*;

/**
 * Profile execution time of join points
 * by making a concrete aspect which defines <tt>targets()</tt>,
 * if not <code>register(JoinPoint)</code>
 * and <code>signal(Object, long, long)</code>.
 */
public abstract aspect Profile {

    /** 
     * Identify join points to profile.
     * Those within the lexical extent of Profile
     * or its subtypes will be excluded from profiling.
     */
    protected abstract pointcut targets();

    Object around() : targets() && !within(Profile+) {
        final Object key = register(thisJoinPoint);
        final long startTime = System.currentTimeMillis();
        try {
            return proceed();
        } finally {
            signal(key, startTime, System.currentTimeMillis());
        }
    }
    protected Object register(JoinPoint jp) {
        return Thread.currentThread().getName() + " -- " + jp;
    }
    protected void signal(Object key, long start, long end) {
        long duration = end - start;
        String tag = (duration == 0 ? "none" : duration < 100 ? "fast" : "slow");
        //System.err.println(duration + " " + start + " - " + end + ": " + key);
        System.err.println(tag + ": " + key);
    }
}
