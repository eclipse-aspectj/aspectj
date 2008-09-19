/*
 * Created on Jul 25, 2008
 */
package bug;

import java.util.List;

public interface Manager {
	
	public Class<?> getManagedType();
	
	public <T> T find(Object id);
	
	public <T> T update(T object);
	
	public <T> T save(T object);
	
	public <T> T remove(T object);

	public List<?> getAny();

	public List<?> getAll();

	public Query createQuery(String queryString);
	
	public EntityManager getEntityManager();
	
}