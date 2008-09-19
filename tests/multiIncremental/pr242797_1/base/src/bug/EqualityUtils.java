/*
 * Created on Aug 8, 2008
 */
package bug;

public class EqualityUtils {
	
	public static boolean equal(Object obj1, Object obj2){
		if(obj1 == null){
			if(obj2 == null)
				return true;
			return false;
		} else if(obj2 == null){
			return false;
		} else{
			return obj1.equals(obj2);
		}
	}

}
