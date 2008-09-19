/*
 * Created on Jul 23, 2008
 */
package bug;

public interface Partitioned extends NaturallyComparable{
	
	public String getOwnerId();
	
	public String getPartitionId();

}
