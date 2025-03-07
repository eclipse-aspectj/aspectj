import java.util.ArrayList;
import java.util.List;

public aspect GenericMethodAspect {
	
	public <T extends Type1> List<T> GenericMethodInterface.getStuff(){
		return new ArrayList<T>();
	}

}
