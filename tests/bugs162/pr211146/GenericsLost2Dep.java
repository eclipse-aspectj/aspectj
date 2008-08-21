import java.util.*;
import java.lang.reflect.*;

public class GenericsLost2Dep {
	public static void main(String[] args) {
		new GenericsLost2().getStrings().add("abc");
	}
}