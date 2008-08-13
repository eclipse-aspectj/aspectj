public interface Partitioned<A> {
	public String getPartitionId();
}
 
aspect PartitionedI {
	public String Partitioned<A>.getPartitionId() {
		return null;
	}
}