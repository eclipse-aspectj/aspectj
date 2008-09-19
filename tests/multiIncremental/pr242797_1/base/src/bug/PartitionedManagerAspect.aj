package bug;

import java.util.List;

public aspect PartitionedManagerAspect {
	
	public Class<? extends Partitioned> PartitionedManager.getPartitionedType(){
		return (Class<? extends Partitioned>) getManagedType();
	}
	
	public List<? extends Partitioned> PartitionedManager.removeWeakPartitionMatches(List<? extends Partitioned> partitioned){
		return removeWeakPartitionMatches(partitioned, getDefaultPartitionOrder());
	}
	
	public List<? extends Partitioned> PartitionedManager.removeWeakPartitionMatches(
				List<? extends Partitioned> partitioned, List<String> partitionOrder){
		return new PartitionedPruner().prune(partitioned, partitionOrder);
	}
	
	public List<? extends Partitioned> PartitionedManager.getBestInDefaultPartitions(){
		return getBestInPartitions(getDefaultPartitionOrder());
	}
	
	public List<? extends Partitioned> PartitionedManager.getBestInPartitions(List<String> partitionOrder){
		List<? extends Partitioned> results = getAllInPartitions(partitionOrder);
		results = removeWeakPartitionMatches(results, partitionOrder);
		return results;
	}
	
	public List<? extends Partitioned> PartitionedManager.getAllInPartition(String partitionId){
		String queryString = QueryUtils.select(getPartitionedType(), "partitioned") +
			" WHERE partitioned.partitionId = :partitionId";
		Query query = createQuery(queryString);
		query.setParameter("partitionId", partitionId);
		return query.getResultList();
	}
	
	public List<? extends Partitioned> PartitionedManager.getAllInPartitions(List<String> partitionOrder){
		String queryString = QueryUtils.select(getPartitionedType(), "partitioned") +
		" WHERE partitioned.partitionId IN (:partitionOrder)";
		Query query = createQuery(queryString);
		query.setParameter("partitionORder", partitionOrder);
		return query.getResultList();
	}

}
