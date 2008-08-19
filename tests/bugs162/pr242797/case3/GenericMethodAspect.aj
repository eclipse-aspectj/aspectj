import java.util.ArrayList;
import java.util.List;

public aspect GenericMethodAspect {
	
	public <T> List<T> GenericMethodInterface.getStuff(){
		return new ArrayList<T>();
	}

}
