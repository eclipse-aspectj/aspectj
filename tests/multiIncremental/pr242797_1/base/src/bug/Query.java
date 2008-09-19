/*
 * Created on Sep 18, 2008
 */
package bug;

import java.util.ArrayList;
import java.util.List;


public class Query {
	
	public List getResultList(){
		return new ArrayList<Object>();
	}
	
	public Query setParameter(String name, Object value){
		return this;
	}

}
