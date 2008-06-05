import java.lang.reflect.Type;

public class Main {
	
	public static void main(String[]argv) throws Exception {
		Class c = Class.forName("a.b.Adapter$1");
		Type[] ts = c.getGenericInterfaces();
		for (int i = 0; i < ts.length; i++) {
			Type type = ts[i];
			System.out.println(ts[i]);
		}
	}
}