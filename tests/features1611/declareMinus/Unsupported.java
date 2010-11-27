import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@interface Anno {
	String foo();
}

@Anno(foo="anc")
aspect X {

	//declare @method: int i: -@Anno;
	declare @method: int i(..): -@Anno;
	declare @type: X: -@Anno;
    declare @field: int i: -@Anno(foo="abc");	
	
	public static void main(String[] args) throws Exception {
		if (X.class.getDeclaredField("i").getAnnotation(Anno.class)==null) {
			System.out.println("not there");
		} else {
			System.out.println("failed");
		}
	}
}