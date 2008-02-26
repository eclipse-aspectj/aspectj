import java.util.*;

public aspect StringAspect {
	public Collection<String> StringClass.getStrings() {
		return new ArrayList<String>();
	}
}
