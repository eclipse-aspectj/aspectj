package testing;

public class ResourceManager {

	public Resource lookupResource(String resourceId){
		return new Resource(resourceId);
	}
}
