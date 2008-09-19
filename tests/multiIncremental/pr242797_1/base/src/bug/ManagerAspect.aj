package bug;

import java.util.List;

public aspect ManagerAspect {

	public List<?> Manager.getAny(){
		return getEntityManager().createQuery("SELECT obj FROM " + 
				getManagedType().getName() + " obj ").getResultList();
	}
	
	public List<?> Manager.getAll(){
		List<?> values = null;
		if(this instanceof LocalizedManager){
			LocalizedManager manager = (LocalizedManager)this;
			values = manager.getAllInDefaultLocales();
		}
		else if(this instanceof PartitionedManager){
			PartitionedManager manager = (PartitionedManager)this;
			values = manager.getBestInDefaultPartitions();
		}
		else{
			values = getAny();
		}
		return values;
	}
	
	public <T> T Manager.find(Object id){
		return (T) getEntityManager().find(getManagedType(), id);
	}
	
	public <T> T Manager.save(T object){
		getEntityManager().persist(object);
		return object;
	}
	
	public <T> T Manager.update(T object){
		getEntityManager().merge(object);
		return object;
	}
	
	public <T> T Manager.remove(T object){
		getEntityManager().remove(object);
		return object;
	}
	
	public Query Manager.createQuery(String queryString){
		EntityManager em = getEntityManager();
		Query query = em.createQuery(queryString);
		return query;
	}
	
	public EntityManager Manager.getEntityManager() {
		return H2Lookup.em();
	}
	
}
