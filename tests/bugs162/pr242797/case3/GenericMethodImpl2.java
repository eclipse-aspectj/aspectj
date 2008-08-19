import java.util.List;


/*
 * Created on Aug 19, 2008
 */

public class GenericMethodImpl2 extends GenericMethodImpl<Type2> {

	@Override
    public <T extends Type2> List<T> getStuff() {
	    return super.getStuff();
    }

}
