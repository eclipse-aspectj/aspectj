import java.util.List;

public aspect ErasureMatching {
	
	declare warning : execution(static Object Utils.first(List)) : "static generic method match";
	
	declare warning : execution(Number Utils.max(Number, Number)) : "instance generic method match";
	
	declare warning : execution(public List G.getAllDataItems()) : "method in generic type match";
	
	declare warning : set(Object G.myData) : "field in generic type match";
	
}

class Utils {
            
    /** static generic method */
    static <T> T first(List<T> ts) { return null; }
            
    /** instance generic method */
    <T extends Number> T max(T t1, T t2) { return t1; }
                
}
          
class G<T> {
           
	// field with parameterized type
	T myData = null;
 
 	// method with parameterized return type
     public List<T> getAllDataItems() { return null; }
      
}