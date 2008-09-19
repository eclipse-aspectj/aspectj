/*
 * Created on Aug 18, 2008
 */
package bug;

public class QueryUtils {
	
	public static String select(Class<?> from, String alias){
		return select(from, alias, alias);
	}
	
	public static String select(Class<?> from, String alias, String target){
		return "SELECT " + target + " FROM " + from.getName() + " " + alias + " ";
	}

}
