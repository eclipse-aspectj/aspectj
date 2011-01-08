import java.lang.reflect.Field;
import java.util.ArrayList;

public class MyList extends ArrayList<Integer> {
	public static void main(String []argv) throws Exception {
		Field f = MyList.class.getDeclaredField("serialVersionUID");
		f.setAccessible(true);
		System.out.println("ser="+f.getLong(new MyList()));
	}
}