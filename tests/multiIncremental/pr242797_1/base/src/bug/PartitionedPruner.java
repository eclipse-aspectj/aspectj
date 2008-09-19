/*
 * Created on Aug 22, 2008
 */
package bug;

public class PartitionedPruner extends NaturallyComparablePruner<Partitioned, String>{

	@Override
    public String getOrdering(Partitioned object) {
	    return object.getPartitionId();
    }

}
