import java.util.*;
public class Code2 {
	public static void main(String []argv) {
		foo(new ArrayList<String>());
	}

	public static void foo(ArrayList<String> als) {
		var aly = als;
		System.out.println(aly.getClass());
	}
}
