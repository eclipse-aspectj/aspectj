/*
 * Created on Aug 22, 2008
 */
package bug;


public interface NaturallyComparable {
	
	public Object getNaturalId();
	
	public boolean naturallyEqual(NaturallyComparable to);

}
