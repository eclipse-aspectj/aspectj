
import java.io.Serializable;

import java.lang.reflect.*;

public class FinalFields implements Serializable {
   public static final Integer SUCCESS = new Integer(0);

	public static void main(String[] args) throws Exception {
		Class c = FinalFields.class;
		Field f = c.getDeclaredField("SUCCESS");
		int mods = f.getModifiers();
		System.out.println("modifers are: " + Modifier.toString(mods));
		if (!Modifier.isFinal(mods)) throw new RuntimeException("modifier should be final");
		if (!Modifier.isPublic(mods)) throw new RuntimeException("modifier should be public");
		if (!Modifier.isStatic(mods)) throw new RuntimeException("modifier should be static");
	}
}
