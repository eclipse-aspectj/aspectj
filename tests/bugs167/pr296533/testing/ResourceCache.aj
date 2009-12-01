package testing;

public aspect ResourceCache extends AbstractCache<String,Resource> {

	public pointcut cachePoint(String key):
		args(key) &&
		execution(public Resource ResourceManager.lookupResource(String));


}
