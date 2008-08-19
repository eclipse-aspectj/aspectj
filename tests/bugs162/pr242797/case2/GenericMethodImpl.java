import java.util.LinkedList;
import java.util.List;

public class GenericMethodImpl implements GenericMethodInterface{
	
	public <T> List<T> getStuff(){
		return new LinkedList<T>();
	}

}
