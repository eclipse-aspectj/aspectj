
public class PR102746 {
	public static void main(String[] args) {
		if (!("hello".equals(lib.Lib.hello()))) {
			throw new Error("no hello");
		}
	}
}