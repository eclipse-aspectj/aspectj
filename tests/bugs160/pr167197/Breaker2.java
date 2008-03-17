import java.util.ArrayList;

class B1<T> extends ArrayList<T[]> {
	public boolean test(T[] obj) {
	  return false;	
	}
	
}

public class Breaker2 extends B1<ICounterValue[]> {
	private static final long serialVersionUID = 1L;
	
    public Breaker2() {
    }

    public boolean test(ICounterValue[] obj) {
        this.size();
        
        return false;    	
    }
    
    public static void main(String[]argv) {
    	new Breaker2().test(new ICounterValue[]{});
    }
}

interface ICounterValue {
	
}

aspect X {
  before(): call(* *(..)) {}
}
