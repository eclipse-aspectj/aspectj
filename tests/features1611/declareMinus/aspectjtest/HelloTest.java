package aspectjtest;


public class HelloTest {
public static void main(String []argv) throws Exception {
		System.out.println( MyEntity.class.getDeclaredField("myField").getAnnotations().length);
// should be B
		System.out.println(MyEntity.class.getDeclaredField("myField").getAnnotations()[0].annotationType());
	}
}
