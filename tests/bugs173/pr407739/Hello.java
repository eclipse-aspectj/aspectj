public class Hello {

	@MyAnnotation(dummy1 = "alma")
	private String dummy;

    public static void main(String []argv) throws Exception {
      System.out.print(Hello.class.getDeclaredField("dummy").getDeclaredAnnotations()[0]);
    }
}
