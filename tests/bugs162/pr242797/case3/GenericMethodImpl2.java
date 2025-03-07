import java.util.List;

public class GenericMethodImpl2 extends GenericMethodImpl {

	@Override
    public <T extends Type1> List<T> getStuff() {
	   return super.getStuff();
    }

}

