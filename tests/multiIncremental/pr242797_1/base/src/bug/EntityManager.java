/*
 * Created on Sep 18, 2008
 */
package bug;


public class EntityManager {
	
	public Query createQuery(String query){
		return new Query();
	}
	
	public Object find(Class<?> type, Object id){
		return new Object();
	}
	
	public void merge(Object object){}
	
	public void persist(Object object){}
	
	public void remove(Object object){}

}
