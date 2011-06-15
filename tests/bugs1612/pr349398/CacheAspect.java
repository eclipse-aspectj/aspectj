//package info.unterstein.hagen.moderne.ea6.a3;

import java.util.HashMap;

/**
 * Enables a more complex and generic caching aspect which can be extended to
be
 * used in several use cases.
 * 
 * @author <a href="mailto:unterstein@me.com">Johannes Unterstein</a>
 * @param <k>
 *            the class of the keys
 * @param <V>
 *            the class of the cached values
 */
public abstract aspect CacheAspect<V> {
    private HashMap<Object, V> cache;

    public abstract pointcut cachePoint(Object key);

    V around(Object key) : cachePoint(key) {
        if (this.cache == null) {
            this.cache = new HashMap<Object, V>();
        }
        V result;
        if (this.cache.containsKey(key)) {
            result = this.cache.get(key);
        } else {
            result = proceed(key);
            this.cache.put(key, result);
        }
Object o = this.cache;
        return result;
    }
}
