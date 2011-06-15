//package info.unterstein.hagen.moderne.ea6.a3;

/**
 * An extension of the generic cache for the concrete use case of caching the
 * {@link DataGenerator}.
 * 
 * @author <a href="mailto:unterstein@me.com">Johannes Unterstein</a>
 */
public aspect DataGeneratorCacheAspect extends CacheAspect<Integer> {

    public pointcut cachePoint(Object key) : call(Integer
DataGenerator.getData(Integer)) && args(key);
}
