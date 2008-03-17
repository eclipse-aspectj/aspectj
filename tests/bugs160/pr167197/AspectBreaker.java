import java.util.ArrayList;

public class AspectBreaker extends ArrayList<ICounterValue[]> {
	private static final long serialVersionUID = 1L;
	
    public AspectBreaker() {
    }

    public boolean test(ICounterValue[] obj) {
        this.size();
        
        return false;    	
    }
}

interface ICounterValue {
	
}

aspect X {
  before(): call(* *(..)) {}
}
