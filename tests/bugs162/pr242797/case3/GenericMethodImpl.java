import java.util.LinkedList;
import java.util.List;

public class GenericMethodImpl<D extends Type1> implements GenericMethodInterface{
	
	public <T extends D> List<T> getStuff(){
		return new LinkedList<T>();
	}

}
