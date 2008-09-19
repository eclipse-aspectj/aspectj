/*
 * Created on Aug 22, 2008
 */
package bug;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public abstract class NaturallyComparablePruner<T extends NaturallyComparable, O> {
	
	public List<? extends T> prune(List<? extends T> list, List<O> order){
		Map<Object, List<T>> sep = new HashMap<Object, List<T>>();
		for(T obj : list){
			Object id = obj.getNaturalId();
			List<T> matches = sep.get(id);
			if(matches == null){
				matches = new ArrayList<T>();
				sep.put(id, matches);
			}
			if(matches.size() == 0)
				matches.add(obj);
			else{
				T match = matches.get(0);
				O yours = getOrdering(match);
				O mine = getOrdering(obj);
				if(EqualityUtils.equal(mine, yours))
					matches.add(obj);
				else{
					int yourIndex = order.indexOf(yours);
					int myIndex = order.indexOf(mine);
					if(myIndex < yourIndex){
						matches.clear();
						matches.add(obj);
					}
				}
			}
		}
		List<T> result = new ArrayList<T>();
		for(List<T> values : sep.values())
			result.addAll(values);
		return result;
	}
	
	public abstract O getOrdering(T object);

}
