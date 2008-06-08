
abstract aspect GenAsp<T> {
    public abstract T transform(T x);
    T around() : execution(T *(*)) {return transform(proceed());}
}

aspect IntAsp extends GenAsp<Integer> {
	
    public Integer transform(Integer x) {return x;} // identity transformation
}

public class IntAspTest {
    static Integer mylength(String x) {return x.length();}

    public static void main(String[] args) {
    	try {
    		System.out.println(mylength(""));
    	} catch (StackOverflowError soe) {
    		
    	}
    }
}

