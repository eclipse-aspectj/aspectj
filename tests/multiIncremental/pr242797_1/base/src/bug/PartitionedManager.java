/*
 * Created on Jul 25, 2008
 */
package bug;

import java.util.List;

public interface PartitionedManager extends Manager{

	public Class<? extends Partitioned> getPartitionedType();
	
	public List<? extends Partitioned> getAllInPartitions(List<String> partitionOrder);
	
	public List<? extends Partitioned> getAllInPartition(String partitionId);
	
	public List<? extends Partitioned> getBestInPartitions(List<String> partitionOrder);
	
	public List<? extends Partitioned> getBestInDefaultPartitions();
	
	public List<? extends Partitioned> removeWeakPartitionMatches(List<? extends Partitioned> partitioned);
	
	public List<? extends Partitioned> removeWeakPartitionMatches(List<? extends Partitioned> partitioned, List<String> partitionOrder);
	
	public DuplicateStrategy getPartitionedDuplicateStrategy();

	public List<String> getDefaultPartitionOrder();

}