import java.util.LinkedList;
import java.util.List;

public class GenericMethodImpl implements GenericMethodInterface{
	
	@Override
	public <T extends Type1> List<T> getStuff() {
		return new LinkedList<T>();
	}

}
