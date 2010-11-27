import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@interface Anno {
	String foo();
}

public aspect WithValues {
	@Anno(foo="anc")
	int i;
	
	declare @field: int i: -@Anno;
	
	public static void main(String[] args) throws Exception {
		if (WithValues.class.getDeclaredField("i").getAnnotation(Anno.class)==null) {
			System.out.println("not there");
		} else {
			System.out.println("failed");
		}
	}
}