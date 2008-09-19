/*
 * Created on Sep 18, 2008
 */
package bug;

import java.util.List;


public class FailingManager implements PartitionedManager {

	public Class<Object> getManagedType(){
		return Object.class;
	}

    public List<String> getDefaultPartitionOrder() {
	    return null;
    }

    public DuplicateStrategy getPartitionedDuplicateStrategy() {
	    return null;
    }
	
}
